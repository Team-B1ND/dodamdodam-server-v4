package com.b1nd.dodamdodam.file.domain.enumeration

enum class FileType(
    val extensions: Set<String>,
    val mimePrefix: String,
    val supportsDimensionCheck: Boolean,
) {
    IMAGE(
        extensions = setOf("jpg", "jpeg", "png", "bmp", "webp", "tiff"),
        mimePrefix = "image/",
        supportsDimensionCheck = true,
    ),
    GIF(
        extensions = setOf("gif"),
        mimePrefix = "image/gif",
        supportsDimensionCheck = true,
    ),
    VIDEO(
        extensions = setOf("mp4", "avi", "mov", "mkv", "webm", "wmv", "flv"),
        mimePrefix = "video/",
        supportsDimensionCheck = false,
    ),
    AUDIO(
        extensions = setOf("mp3", "wav", "ogg", "flac", "aac", "wma"),
        mimePrefix = "audio/",
        supportsDimensionCheck = false,
    ),
    DOCUMENT(
        extensions = setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "hwp"),
        mimePrefix = "",
        supportsDimensionCheck = false,
    );

    companion object {
        private val contentTypes = mapOf(
            "jpg" to "image/jpeg",
            "jpeg" to "image/jpeg",
            "png" to "image/png",
            "bmp" to "image/bmp",
            "webp" to "image/webp",
            "tiff" to "image/tiff",
            "gif" to "image/gif",
            "mp4" to "video/mp4",
            "avi" to "video/x-msvideo",
            "mov" to "video/quicktime",
            "mkv" to "video/x-matroska",
            "webm" to "video/webm",
            "wmv" to "video/x-ms-wmv",
            "flv" to "video/x-flv",
            "mp3" to "audio/mpeg",
            "wav" to "audio/wav",
            "ogg" to "audio/ogg",
            "flac" to "audio/flac",
            "aac" to "audio/aac",
            "wma" to "audio/x-ms-wma",
            "pdf" to "application/pdf",
            "doc" to "application/msword",
            "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "xls" to "application/vnd.ms-excel",
            "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "ppt" to "application/vnd.ms-powerpoint",
            "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "txt" to "text/plain",
            "csv" to "text/csv",
            "hwp" to "application/x-hwp",
        )

        fun fromExtension(extension: String): FileType? {
            val ext = extension.lowercase()
            return entries.firstOrNull { ext in it.extensions }
        }

        fun contentTypeFromExtension(extension: String): String? =
            contentTypes[extension.lowercase()]
    }
}
