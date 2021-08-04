package coint

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request


private const val URL = "https://message.ppurio.com/api/send_utf8_json.php"

/**
 * @param userid (필수) 뿌리오 가입 아이디
 * @param callback (필수) 승인된 발신번호 (숫자만)
 * @param phone (필수) 수신번호
 * @param msg (필수) 메시지 내용
 */
fun send(userid: String, callback: String, phone: String, msg: String): SendResult {
    return send(URL, userid, callback, phone, msg)
}

private val client = OkHttpClient()
private val gson = GsonBuilder().create();

internal fun send(url: String, userid: String, callback: String, phone: String, msg: String): SendResult {
    val form = FormBody.Builder()
        .add("userid", userid)
        .add("callback", callback)
        .add("phone", phone)
        .add("msg", msg)
        .build()
    val request = Request.Builder().url(url).post(form).build()
    return client.newCall(request).execute().use { response ->
        parseSendResult(response.body?.string() ?: "")
    }
}

enum class SendResultType {
    SMS, LMS, MMS
}

enum class SendResultState {
    OK, // 성공
    INVALID_MEMBER, // 연동서비스 이용불가 아이디
    UNDER_MAINTENANCE, // 발송요청 시간에 서버점검 인 경우
    ALLOW_HTTPS_ONLY, // http 요청인 경우
    INVALID_IP, // 등록된 IP가 아닌 경우
    INVALID_MSG, // 메시지 내용 오류
    INVALID_NAMES, // 이름 오류
    INVALID_SUBJECT, // 제목 오류
    INVALID_SENDTIME, // 예약발송 시간에 오류 (10분이후 부터 다음해말까지 가능)
    INVALID_SENDTIME_MAINTENANCE, // 예약 발송시간내에 서버점검 인 경우
    INVALID_PHONE, // 수신번호 오류
    INVALID_MSG_OVER_MAX, // 메시지 내용 byte 초과
    INVALID_CALLBACK, // 발신번호 상태 오류
    ONCE_LIMIT_OVER, // 1회 최대 발송건수 초과
    DAILY_LIMIT_OVER, // 1일 최대 발송건수 초과
    NOT_ENOUGH_POINT, // 발송금액 부족
    OVER_USE_LIMIT, // 한달 사용금액을 초과한 경우
    SERVER_ERROR, // 기타 서버 오류
    INVALID_MSGID, // 발송 msgid 오류
    MASTER_NOT_EXIST, // 취소할 발송 msgid가 없는 경우
    NOT_UPDATE_TIME, // 예약시간이 1분이내여서 취소가 불가능한 경우
    ING_MASTER, // 이미 발송중인 경우
    INVALID; // 처리불가능한 결과

    override fun toString(): String {
        val explain = when (this) {
            OK -> "성공"
            INVALID_MEMBER -> "연동서비스 이용불가 아이디"
            UNDER_MAINTENANCE -> "발송요청 시간에 서버점검 인 경우"
            ALLOW_HTTPS_ONLY -> "http 요청인 경우"
            INVALID_IP -> "등록된 IP가 아닌 경우"
            INVALID_MSG -> "메시지 내용 오류"
            INVALID_NAMES -> "이름 오류"
            INVALID_SUBJECT -> "제목 오류"
            INVALID_SENDTIME -> "예약발송 시간에 오류 (10분이후 부터 다음해말까지 가능)"
            INVALID_SENDTIME_MAINTENANCE -> "예약 발송시간내에 서버점검 인 경우"
            INVALID_PHONE -> "수신번호 오류"
            INVALID_MSG_OVER_MAX -> "메시지 내용 byte 초과"
            INVALID_CALLBACK -> "발신번호 상태 오류"
            ONCE_LIMIT_OVER -> "1회 최대 발송건수 초과"
            DAILY_LIMIT_OVER -> "1일 최대 발송건수 초과"
            NOT_ENOUGH_POINT -> "발송금액 부족"
            OVER_USE_LIMIT -> "한달 사용금액을 초과한 경우"
            SERVER_ERROR -> "기타 서버 오류"
            INVALID_MSGID -> "발송 msgid 오류"
            MASTER_NOT_EXIST -> "취소할 발송 msgid가 없는 경우"
            NOT_UPDATE_TIME -> "예약시간이 1분이내여서 취소가 불가능한 경우"
            ING_MASTER -> "이미 발송중인 경우"
            INVALID -> "INVALID"
        }
        return "${this.name}(${explain})"
    }
}

data class SendResult(
    val raw: String,
    val state: SendResultState,
    val type: SendResultType,
    val messageId: String,
    val count: Int
) {
    val success: Boolean
        get() = state == SendResultState.OK
}

internal fun parseSendResult(raw: String): SendResult {
    val json =
        try {
            gson.fromJson(raw, JsonObject::class.java)
        } catch (e: JsonParseException) {
            null
        }
            ?: return invalidSendResult(raw)

    val state = SendResultState.values()
        .find { it.name == json.get("result")?.asString?.uppercase() }
        ?: SendResultState.INVALID
    val type = SendResultType.values().find { it.name == json.get("type")?.asString?.uppercase() }
        ?: SendResultType.SMS
    val messageId = json.get("msgid")?.asString ?: ""
    val count = json.get("ok_cnt")?.asInt ?: 0
    return SendResult(raw, state, type, messageId, count)
}

internal fun invalidSendResult(raw: String) = SendResult(raw, SendResultState.INVALID, SendResultType.SMS, "", 0)


fun main(args: Array<String>) {
    val result = send(userid = args[0], callback = args[1], phone = args[2], msg = args[3])
    println("result = $result")
}