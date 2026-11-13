package com.b1nd.dodamdodam.neis.infrastructure.comcigan

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/** Synthetic page/data using the protocol observed on the student page on 2026-11-13. */
internal object ComciganFixtures {
    val mapper = ObjectMapper()
    private val key = ByteArray(32) { 0x11 }
    private val textKey = byteArrayOf(65, 66, 67)
    val script = """
        window.__HourRequestToken='test-token';
        function QAjaxSchool(sc) {return {url:'./900886?'+encodeURIComponent(btoa('83845l'+encodeURIComponent(sc)+'_enc5'))};}
        sc_data('83044_',sc,1,'0');
        원자료=Q자료(자료.자료188[학년][반][요일][교시]);
        일일자료=Q자료(자료.자료156[학년][반][요일][교시]);
        성명=Q성명(자료.자료117[th]);과목명=Q과목명(자료.자료136[sb]);
        일자설정하기(H시간표.자료116,r2);요일출력하기(QnmsbChange(자료.자료108));
        var r=Math.floor(학교명단[i][0]/6347)+2;
        var 키=String.fromCharCode(65,66,67);
        var key=(function(a){return a[2]+a[1]+a[3]+a[0];})(['1111111111111111','1111111111111111','1111111111111111','1111111111111111']);
    """.trimIndent()
    val html get() = "<html><script>${pack(pack(script))}</script></html>"

    fun pack(value: String): String {
        val encoded = value.mapIndexed { i, c -> "%04X".format(c.code xor ((83 + i * 13) and 255)) }.joinToString("")
        return "/* hour-obfuscated-v2 */(function(a,k){/* deliberately never executed */})([[0,\"$encoded\"],[1,\"\"]],83);"
    }

    fun text(value: String): String {
        val bytes = value.toByteArray(Charset.forName("EUC-KR"))
        return Base64.getEncoder().encodeToString(ByteArray(bytes.size) { (bytes[it].toInt() xor textKey[it % textKey.size].toInt()).toByte() })
    }

    fun response(value: Any): String {
        val iv = ByteArray(16) { 7 }
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        val encrypted = cipher.doFinal(mapper.writeValueAsBytes(value))
        val prefix = "enc5:${Base64.getEncoder().encodeToString(iv)}:${Base64.getEncoder().encodeToString(encrypted)}"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(key), "HmacSHA256"))
        return "$prefix:${Base64.getEncoder().encodeToString(mac.doFinal(prefix.toByteArray()))}\u0000"
    }

    fun schools() = mapOf("학교검색" to listOf(
        listOf(0, text("안내"), text("학교명 안내")),
        listOf(12694, text("대구"), text("다른학교"), text("unused"), text("11111")),
        listOf(12694, text("대구"), text("대구소프트웨어고등학교"), text("unused"), text("20999")),
    ))

    fun timetable(start: String = "2026-11-09"): ObjectNode {
        val daily = MutableList(4) { MutableList(5) { MutableList(6) { MutableList<Any>(9) { 0 } } } }
        val original = MutableList(4) { MutableList(5) { MutableList(6) { MutableList<Any>(9) { 0 } } } }
        daily[2][3][1][1] = ">120123"
        daily[2][3][1][2] = 1000120123 // grouped subject 120, three-digit teacher 123
        original[2][3][1][1] = 120123 // explicit change still applies when values match
        original[2][3][1][2] = 120001 // no explicit change marker
        daily[1][1][1][1] = 120123 // keep another class distinguishable
        val subjects = MutableList(121) { "" }.apply { this[120] = text("웹프") }
        val teachers = MutableList(124) { "" }.apply { this[123] = text("김경*") }
        return mapper.valueToTree(mapOf(
            "자료156" to daily, "자료188" to original, "자료136" to subjects, "자료117" to teachers,
            "자료108" to text(start), "분리" to 1000, "변경알림" to 1,
            "자료116" to listOf(listOf(1, text("26-11-09 ~ 26-11-14")), listOf(2, text("26-11-16 ~ 26-11-21"))),
        ))
    }
}
