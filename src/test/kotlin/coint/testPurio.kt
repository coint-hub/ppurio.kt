package coint

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TestPpurio {
    @Test
    fun testSend() {
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

        // 일반 적인 성공 파싱 확인
        assertEquals(
            SendResult("ok|sms|01012345678|1", SendResultState.OK, SendResultType.SMS, "01012345678", 1),
            parseSendResult("ok|sms|01012345678|1")
        )

        // state 파싱 확인
        assertEquals(SendResultState.INVALID_MEMBER, parseSendResult("invalid_member|sms|id|1").state)
        // 잘못된 state 파싱 확인
        assertEquals(SendResultState.INVALID, parseSendResult("잘못된 상태값|sms|id|1").state)

        // type 파싱 확인
        assertEquals(SendResultType.LMS, parseSendResult("ok|lms|id|1").type)
        // 잘못된 type 파싱 확인
        assertEquals(SendResultState.INVALID, parseSendResult("ok|잘못된 타입|id|1").state)

        // 메시지 아이디 파싱 확인
        assertEquals("메시지 아이디", parseSendResult("ok|lms|메시지 아이디|1").messageId)

        // 발송 획수 확인
        assertEquals(1, parseSendResult("ok|lms|id|1").count)
        assertEquals(2, parseSendResult("ok|lms|id|2").count)
    }
}