package coint

import com.google.gson.Gson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import java.nio.charset.Charset
import kotlin.test.assertEquals
import kotlin.test.assertFalse


class TestPpurio {
    @Test
    fun testSend(): Unit = mockWebServer { server ->
        val url = server.url("").toString()

        // 서버측의 잘못된 응답 테스트
        server.enqueue("잘못된 응답") {
            assertEquals(
                invalidSendResult("잘못된 응답"),
                send(url, "", "", "", "")
            )
        }


        // 입력 파라미터 확인
        server
            .enqueue("") {
                send(url, "value_userid", "value_callback", "value_phone", "value_msg")
            }.let { request ->
                assertEquals("value_userid", request["userid"])
                assertEquals("value_callback", request["callback"])
                assertEquals("value_phone", request["phone"])
                assertEquals("value_msg", request["msg"])
            }

        // 정상 응답 테스트
        server.enqueue("""{"result":"ok","type":"sms","msgid":"257052316","ok_cnt":1}""") {
            assertEquals(
                SendResult(
                    """{"result":"ok","type":"sms","msgid":"257052316","ok_cnt":1}""",
                    SendResultState.OK,
                    SendResultType.SMS,
                    "257052316",
                    1
                ),
                send(server.url("/").toString(), "", "", "", "")
            )
        }
    }

    @Test
    fun testParseSendResult() {
        // 공백인 결과 값 파싱
        assertEquals(
            SendResult("", SendResultState.INVALID, SendResultType.SMS, "", 0),
            parseSendResult("")
        )
        // 파싱이 불가능하면 당연히 실패다.
        assertFalse(parseSendResult("").success)


        // state 파싱 확인
        assertEquals(
            SendResultState.INVALID_PHONE,
            parseSendResult("""{"result":"invalid_phone"}""").state
        )
        // 잘못된 state 파싱 확인
        assertEquals(SendResultState.INVALID, parseSendResult("""{"result":"잘못된 상태값"}""").state)

        // type 파싱 확인
        assertEquals(SendResultType.LMS, parseSendResult("""{"type": "lms"}""").type)
        // 잘못된 type 파싱 확인
        assertEquals(SendResultState.INVALID, parseSendResult(""""{"type": "잘못된 타입"}"""").state)

        // 메시지 아이디 파싱 확인
        assertEquals("메시지 아이디", parseSendResult("""{"msgid": "메시지 아이디"}""").messageId)

        // 발송 획수 확인
        assertEquals(1, parseSendResult("""{"ok_cnt": 1}""").count)
        assertEquals(2, parseSendResult("""{"ok_cnt": 2}""").count)

        // 일반 적인 성공 파싱 확인
        assertEquals(
            SendResult(
                """{"result":"ok","type":"sms","msgid":"257052316","ok_cnt":1}""",
                SendResultState.OK,
                SendResultType.SMS,
                "257052316",
                1
            ),
            parseSendResult("""{"result":"ok","type":"sms","msgid":"257052316","ok_cnt":1}""")
        )
    }
}

private val gson = Gson()

@Suppress("UNCHECKED_CAST")
private fun MockWebServer.enqueue(body: String, block: () -> Unit): Map<String, Any> {
    enqueue(MockResponse().setBody(body))
    block()
    return takeRequest().body.readString(Charset.defaultCharset())
        .split("&")
        .associate {
            val (name, value) = it.split("=")
            name to value
        }
}

private fun mockWebServer(block: (server: MockWebServer) -> Unit) {
    MockWebServer().use {
        it.start()
        block(it)
    }
}
