package com.zclei.hitrise.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Test

class PressureCounterMathTest {
    @Test
    fun sessionUsesDifferenceFromExistingCumulativeBaseline() {
        assertEquals(1, pressureCountDelta(previousRawCount = 47, currentRawCount = 48, firstPacketIsActiveHit = false))
        assertEquals(3, pressureCountDelta(previousRawCount = 48, currentRawCount = 51, firstPacketIsActiveHit = false))
        assertEquals(25, pressureCountDelta(previousRawCount = 100, currentRawCount = 125, firstPacketIsActiveHit = false))
    }

    @Test
    fun firstActivePacketCountsOneWithoutImportingHistoricalTotal() {
        assertEquals(1, pressureCountDelta(previousRawCount = null, currentRawCount = 83, firstPacketIsActiveHit = true))
        assertEquals(0, pressureCountDelta(previousRawCount = null, currentRawCount = 83, firstPacketIsActiveHit = false))
    }

    @Test
    fun counterWrapCountsOnlyNewHits() {
        assertEquals(1, pressureCountDelta(previousRawCount = 255, currentRawCount = 0, firstPacketIsActiveHit = false))
        assertEquals(3, pressureCountDelta(previousRawCount = 254, currentRawCount = 1, firstPacketIsActiveHit = false))
        assertEquals(26, pressureCountDelta(previousRawCount = 250, currentRawCount = 20, firstPacketIsActiveHit = false))
    }

    @Test
    fun unexpectedBackwardJumpDoesNotImportOldCount() {
        assertEquals(0, pressureCountDelta(previousRawCount = 120, currentRawCount = 3, firstPacketIsActiveHit = false))
    }
}
