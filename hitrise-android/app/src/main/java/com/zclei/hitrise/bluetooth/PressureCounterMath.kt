package com.zclei.hitrise.bluetooth

private const val PRESSURE_COUNTER_MODULUS = 256
private const val MAX_PLAUSIBLE_WRAPPED_ADVANCE = 64

internal fun pressureCountDelta(
    previousRawCount: Int?,
    currentRawCount: Int,
    firstPacketIsActiveHit: Boolean,
): Int {
    if (previousRawCount == null) {
        return if (firstPacketIsActiveHit) 1 else 0
    }
    if (currentRawCount >= previousRawCount) {
        return currentRawCount - previousRawCount
    }

    // data5 is an unsigned byte. A busy UI frame can coalesce enough impacts that the
    // first value observed after 255 is already greater than 15, so use modulo arithmetic
    // instead of a narrow 240/15 boundary. The cap still rejects a genuine counter reset.
    val wrappedAdvance = currentRawCount + PRESSURE_COUNTER_MODULUS - previousRawCount
    return wrappedAdvance.takeIf { it in 1..MAX_PLAUSIBLE_WRAPPED_ADVANCE } ?: 0
}
