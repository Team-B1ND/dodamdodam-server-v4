package com.b1nd.dodamdodam.inapp.domain.app.service

import com.b1nd.dodamdodam.inapp.domain.app.command.CreateAppCommand
import com.b1nd.dodamdodam.inapp.domain.app.command.EditAppCommand
import com.b1nd.dodamdodam.inapp.domain.app.entity.AppEntity
import com.b1nd.dodamdodam.inapp.domain.app.entity.AppReleaseEntity
import com.b1nd.dodamdodam.inapp.domain.app.enumeration.AppStatusType
import com.b1nd.dodamdodam.inapp.domain.app.exception.AppAlreadyExistException
import com.b1nd.dodamdodam.inapp.domain.app.exception.AppDenyReasonRequiredException
import com.b1nd.dodamdodam.inapp.domain.app.exception.AppNotFoundException
import com.b1nd.dodamdodam.inapp.domain.app.exception.AppReleaseEnableNotAllowedException
import com.b1nd.dodamdodam.inapp.domain.app.exception.AppReleaseNotFoundException
import com.b1nd.dodamdodam.inapp.domain.app.exception.AppTeamMemberPermissionRequiredException
import com.b1nd.dodamdodam.inapp.domain.app.exception.AppTeamOwnerPermissionRequiredException
import com.b1nd.dodamdodam.inapp.domain.app.repository.AppQueryRepository
import com.b1nd.dodamdodam.inapp.domain.app.repository.AppReleaseQueryRepository
import com.b1nd.dodamdodam.inapp.domain.app.repository.AppReleaseRepository
import com.b1nd.dodamdodam.inapp.domain.app.repository.AppRepository
import com.b1nd.dodamdodam.inapp.infrastructure.kafka.producer.AppReleaseActivatedEventProducer
import com.b1nd.dodamdodam.inapp.domain.team.entity.TeamEntity
import com.b1nd.dodamdodam.inapp.domain.team.repository.TeamMemberRepository
import com.b1nd.dodamdodam.inapp.domain.team.repository.TeamRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class AppService(
    private val appRepository: AppRepository,
    private val appQueryRepository: AppQueryRepository,
    private val appReleaseRepository: AppReleaseRepository,
    private val appReleaseQueryRepository: AppReleaseQueryRepository,
    private val appReleaseActivatedEventProducer: AppReleaseActivatedEventProducer,
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
) {

    fun create(userId: UUID, command: CreateAppCommand): UUID {
        if (existByName(command.name)) throw AppAlreadyExistException()
        val team = getTeamWithMemberPermission(userId, command.teamId)
        val app = appRepository.save(command.toEntity(team))
        appReleaseRepository.save(
            AppReleaseEntity(
                app = app,
                enabled = false,
                releaseUrl = command.githubReleaseUrl,
                updatedUser = userId,
                status = AppStatusType.PENDING
            )
        )
        app.updateReleaseInfo(enabled = false, status = AppStatusType.PENDING)
        return app.publicId!!
    }

    fun createRelease(userId: UUID, appId: UUID, releaseUrl: String, memo: String?): UUID {
        val app = getAppWithMemberPermission(userId, appId)
        val release = appReleaseRepository.save(
            AppReleaseEntity(
                app = app,
                enabled = false,
                releaseUrl = releaseUrl,
                updatedUser = userId,
                memo = memo,
                status = AppStatusType.PENDING
            )
        )
        return release.publicId!!
    }

    fun updateReleaseStatus(userId: UUID, releaseId: UUID, status: AppStatusType, denyResult: String?) {
        if (status == AppStatusType.DENIED) requireDenyReason(denyResult)
        val release = getRelease(releaseId)
        release.updateStatus(status, denyResult, userId)
        if (status == AppStatusType.ALLOWED) {
            appReleaseRepository.findAllByAppAndEnabledIsTrue(release.app)
                .filter { it.id != release.id }
                .forEach { it.updateEnabled(false, userId) }
            release.updateEnabled(true, userId)
            appReleaseActivatedEventProducer.publishActivated(release)
        }
        release.app.updateReleaseInfo(enabled = release.enabled, status = release.status)
    }

    fun denyRelease(userId: UUID, releaseId: UUID, denyResult: String?) {
        updateReleaseStatus(userId, releaseId, AppStatusType.DENIED, denyResult)
    }

    fun toggleReleaseEnabled(userId: UUID, releaseId: UUID, enabled: Boolean) {
        val release = getReleaseWithOwnerPermission(userId, releaseId)
        if (enabled && release.status != AppStatusType.ALLOWED) {
            throw AppReleaseEnableNotAllowedException()
        }
        if (enabled) {
            appReleaseRepository.findAllByAppAndEnabledIsTrue(release.app)
                .filter { it.id != release.id }
                .forEach { it.updateEnabled(false, userId) }
        }
        release.updateEnabled(enabled, userId)
        release.app.updateReleaseInfo(enabled = release.enabled, status = release.status)
        if (enabled) {
            appReleaseActivatedEventProducer.publishActivated(release)
        }
    }

    fun getReleases(userId: UUID, appId: UUID, date: LocalDate?, keyword: String?, pageable: Pageable): Page<AppReleaseEntity> {
        val app = getAppWithOwnerPermission(userId, appId)
        return appReleaseQueryRepository.findReleases(app, date, keyword, pageable)
    }

    fun getAppDetail(appId: UUID): Pair<AppEntity, List<AppReleaseEntity>> {
        val app = getApp(appId)
        val releases = appReleaseRepository.findAllByAppOrderByCreatedAtDesc(app)
        return Pair(app, releases)
    }

    fun getAppsByTeam(userId: UUID, teamId: UUID): List<AppEntity> {
        val team = getTeamWithMemberPermission(userId, teamId)
        return appRepository.findAllByTeamOrderByIdDesc(team)
    }

    fun getActiveApps(pageable: Pageable): Page<AppEntity> =
        appQueryRepository.findActiveApps(pageable)

    fun getActiveAppsWithRelease(pageable: Pageable) =
        appQueryRepository.findActiveAppsWithRelease(pageable)

    fun getMyApps(userId: UUID): List<AppEntity> {
        val teams = teamMemberRepository.findAllByUser(userId)
            .map { it.team }
            .distinctBy { it.id }
        if (teams.isEmpty()) return emptyList()
        return appRepository.findAllByTeamInOrderByIdDesc(teams)
    }

    fun updateApp(userId: UUID, command: EditAppCommand) {
        val app = getAppWithMemberPermission(userId, command.appId)
        command.name?.let {
            if (it != app.name && existByName(it)) throw AppAlreadyExistException()
        }
        app.update(command.name, command.subtitle, command.description, command.iconUrl, command.darkIconUrl, command.inquiryMail)
    }

    fun deleteApp(userId: UUID, appId: UUID) {
        val app = getAppWithOwnerPermission(userId, appId)
        appRepository.delete(app)
    }

    fun existByName(name: String) =
        appRepository.existsByName(name)

    fun getApp(appId: UUID): AppEntity =
        appRepository.findByPublicId(appId)
            ?: throw AppNotFoundException()

    fun getRelease(releaseId: UUID): AppReleaseEntity =
        appReleaseRepository.findByPublicId(releaseId)
            ?: throw AppReleaseNotFoundException()

    private fun validateAppOwner(userId: UUID, app: AppEntity) {
        if (!teamMemberRepository.existsByUserAndTeamAndIsOwnerIsTrue(userId, app.team)) {
            throw AppTeamOwnerPermissionRequiredException()
        }
    }

    private fun getTeamWithMemberPermission(userId: UUID, teamId: UUID): TeamEntity {
        val team = teamRepository.findByPublicId(teamId)
            ?: throw AppTeamMemberPermissionRequiredException()
        if (!teamMemberRepository.existsByUserAndTeam(userId, team)) {
            throw AppTeamMemberPermissionRequiredException()
        }
        return team
    }

    private fun validateAppMember(userId: UUID, app: AppEntity) {
        if (!teamMemberRepository.existsByUserAndTeam(userId, app.team)) {
            throw AppTeamMemberPermissionRequiredException()
        }
    }

    private fun getAppWithMemberPermission(userId: UUID, appId: UUID): AppEntity =
        getApp(appId).also { validateAppMember(userId, it) }

    private fun getAppWithOwnerPermission(userId: UUID, appId: UUID): AppEntity =
        getApp(appId).also { validateAppOwner(userId, it) }

    private fun getReleaseWithOwnerPermission(userId: UUID, releaseId: UUID): AppReleaseEntity =
        getRelease(releaseId).also { validateAppOwner(userId, it.app) }

    private fun requireDenyReason(denyResult: String?) {
        if (denyResult.isNullOrBlank()) {
            throw AppDenyReasonRequiredException()
        }
    }
}
