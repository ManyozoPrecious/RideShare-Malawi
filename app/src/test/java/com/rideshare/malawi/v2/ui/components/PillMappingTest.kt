package com.rideshare.malawi.v2.ui.components

import com.rideshare.malawi.v2.data.RequestStatus
import com.rideshare.malawi.v2.data.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class PillMappingTest {

    @Test
    fun verificationStatus_toPill_returnsCorrectLabelAndTone() {
        val notSubmitted = VerificationStatus.NOT_SUBMITTED.toPill()
        assertEquals("Not verified", notSubmitted.label)
        assertEquals(PillTone.Neutral, notSubmitted.tone)

        val pending = VerificationStatus.PENDING.toPill()
        assertEquals("Pending review", pending.label)
        assertEquals(PillTone.Pending, pending.tone)

        val verified = VerificationStatus.VERIFIED.toPill()
        assertEquals("Verified", verified.label)
        assertEquals(PillTone.Positive, verified.tone)
    }

    @Test
    fun requestStatus_toPill_returnsCorrectLabelAndTone() {
        val pending = RequestStatus.PENDING.toPill()
        assertEquals("Pending", pending.label)
        assertEquals(PillTone.Pending, pending.tone)

        val accepted = RequestStatus.ACCEPTED.toPill()
        assertEquals("Accepted", accepted.label)
        assertEquals(PillTone.Positive, accepted.tone)

        val rejected = RequestStatus.REJECTED.toPill()
        assertEquals("Declined", rejected.label)
        assertEquals(PillTone.Negative, rejected.tone)
    }
}
