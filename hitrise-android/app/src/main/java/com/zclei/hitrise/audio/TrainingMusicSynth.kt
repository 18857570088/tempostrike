package com.zclei.hitrise.audio

import kotlin.math.PI
import kotlin.math.sin

/** One PCM timeline drives the audible beat and its BPM-matched backing music. */
internal object TrainingMusicSynth {
    const val SAMPLE_RATE = 22_050
    const val MIN_BPM = 180
    const val MAX_BPM = 400
    const val DEFAULT_BPM = MIN_BPM
    private const val CHAMPION_BPM = 122.0
    private val chordNotes = arrayOf(
        doubleArrayOf(220.00, 261.63, 329.63), // A minor
        doubleArrayOf(174.61, 220.00, 261.63), // F major
        doubleArrayOf(196.00, 261.63, 329.63), // C major
        doubleArrayOf(196.00, 246.94, 293.66), // G major
    )
    private val melodyNotes = arrayOf(
        doubleArrayOf(440.00, 523.25, 659.25, 523.25),
        doubleArrayOf(440.00, 523.25, 698.46, 523.25),
        doubleArrayOf(392.00, 523.25, 659.25, 523.25),
        doubleArrayOf(392.00, 493.88, 587.33, 493.88),
    )

    enum class Backing { None, Adaptive, Champion }

    data class BeatWindows(val perfectMs: Int, val goodMs: Int)

    fun beatWindows(bpm: Int): BeatWindows {
        val intervalMs = 60_000.0 / bpm.coerceIn(MIN_BPM, MAX_BPM)
        return BeatWindows(
            perfectMs = minOf(100, (intervalMs * 0.22).toInt()),
            goodMs = minOf(200, (intervalMs * 0.40).toInt()),
        )
    }

    /** Champion Rush's 116–128 BPM beat is played as a 232–256 BPM double-time backing. */
    fun championCompatibleBpm(bpm: Int): Boolean = bpm in 232..256

    fun backingFor(bpm: Int, autoMusic: Boolean, championAvailable: Boolean): Backing = when {
        !autoMusic -> Backing.None
        championCompatibleBpm(bpm) && championAvailable -> Backing.Champion
        else -> Backing.Adaptive
    }

    fun render(
        buffer: ShortArray,
        startFrame: Long,
        bpm: Int,
        includeGroove: Boolean,
        backing: Backing,
        championSamples: ShortArray?,
    ) {
        val beatSeconds = 60.0 / bpm.coerceIn(MIN_BPM, MAX_BPM)
        val halfBeatSeconds = beatSeconds / 2.0
        val championStep = bpm / (CHAMPION_BPM * 2.0)
        for (index in buffer.indices) {
            val frame = startFrame + index
            val t = frame.toDouble() / SAMPLE_RATE
            val beatElapsed = t % beatSeconds
            val halfBeatElapsed = t % halfBeatSeconds
            val beatIndex = (t / beatSeconds).toInt()
            val barBeat = beatIndex % 4
            val barIndex = (beatIndex / 4) % 4
            var sample = 0.0

            if (includeGroove) {

                val snareWindow = minOf(0.13, beatSeconds * 0.6)
                if ((barBeat == 1 || barBeat == 3) && beatElapsed < snareWindow) {
                    val snareEnv = 1.0 - beatElapsed / snareWindow
                    val noiseSeed = ((frame * 1_103_515_245L + 12_345L).and(0x7fffffffL))
                    val noise = noiseSeed / 1_073_741_824.0 - 1.0
                    val snareTone = sin(2.0 * PI * 185.0 * t) * 0.45 + noise * 0.55
                    sample += snareTone * snareEnv * 0.34
                }
                val hatWindow = minOf(0.045, halfBeatSeconds * 0.6)
                if (halfBeatElapsed < hatWindow) {
                    val hatEnv = 1.0 - halfBeatElapsed / hatWindow
                    sample += sin(2.0 * PI * 5_200.0 * t) * hatEnv * 0.13
                }

                val leadWindow = minOf(0.16, beatSeconds * 0.7)
                if (barBeat == 3 && beatElapsed < leadWindow) {
                    val leadEnv = 1.0 - beatElapsed / leadWindow
                    sample += sin(2.0 * PI * 880.0 * t) * leadEnv * 0.06
                }
            }

            when (backing) {
                Backing.None -> Unit
                Backing.Champion -> {
                    if (championSamples != null && championSamples.isNotEmpty()) {
                        val sourcePosition = (frame * championStep) % championSamples.size
                        val leftIndex = sourcePosition.toInt()
                        val rightIndex = (leftIndex + 1) % championSamples.size
                        val fraction = sourcePosition - leftIndex
                        val sourceSample = championSamples[leftIndex] * (1.0 - fraction) +
                            championSamples[rightIndex] * fraction
                        sample += sourceSample / 32768.0 * 0.42
                    }
                }
                Backing.Adaptive -> {
                    val barSeconds = beatSeconds * 4.0
                    val barElapsed = t % barSeconds
                    val barFade = minOf(1.0, barElapsed / 0.07, (barSeconds - barElapsed) / 0.07)
                    val chord = chordNotes[barIndex]
                    val padGain = when {
                        bpm < 240 -> 0.056
                        bpm < 340 -> 0.045
                        else -> 0.034
                    }
                    for (note in chord) {
                        sample += (sin(2.0 * PI * note * t) + 0.18 * sin(4.0 * PI * note * t)) *
                            padGain * barFade / chord.size
                    }
                    val noteSeconds = when {
                        bpm < 240 -> beatSeconds * 2.0
                        bpm < 340 -> beatSeconds
                        else -> halfBeatSeconds
                    }
                    val noteIndex = (t / noteSeconds).toInt()
                    val noteElapsed = t % noteSeconds
                    val noteEnvelope = (1.0 - noteElapsed / noteSeconds).coerceIn(0.0, 1.0)
                    val melodyNote = melodyNotes[barIndex][noteIndex % 4]
                    sample += (sin(2.0 * PI * melodyNote * t) +
                        0.12 * sin(4.0 * PI * melodyNote * t)) * noteEnvelope * 0.105
                }
            }
            buffer[index] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.82).toInt().toShort()
        }
    }

    /** The bundled Champion Rush recording is mono, 16-bit PCM WAV at SAMPLE_RATE. */
    fun decodeChampionWav(bytes: ByteArray): ShortArray? {
        if (bytes.size < 44 || ascii(bytes, 0, 4) != "RIFF" || ascii(bytes, 8, 4) != "WAVE") return null
        var offset = 12
        var formatIsSupported = false
        var sampleDataOffset = -1
        var sampleDataSize = 0
        while (offset + 8 <= bytes.size) {
            val size = readInt(bytes, offset + 4)
            if (size < 0 || offset + 8L + size > bytes.size) return null
            when (ascii(bytes, offset, 4)) {
                "fmt " -> {
                    if (size >= 16) {
                        formatIsSupported = readShort(bytes, offset + 8) == 1 &&
                            readShort(bytes, offset + 10) == 1 &&
                            readInt(bytes, offset + 12) == SAMPLE_RATE &&
                            readShort(bytes, offset + 22) == 16
                    }
                }
                "data" -> {
                    sampleDataOffset = offset + 8
                    sampleDataSize = size
                }
            }
            offset += 8 + size + (size and 1)
        }
        if (!formatIsSupported || sampleDataOffset < 0 || sampleDataSize < 2) return null
        return ShortArray(sampleDataSize / 2) { index -> readShort(bytes, sampleDataOffset + index * 2).toShort() }
    }

    private fun ascii(bytes: ByteArray, offset: Int, count: Int): String =
        String(bytes, offset, count, Charsets.US_ASCII)

    private fun readShort(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xff) or ((bytes[offset + 1].toInt() and 0xff) shl 8)

    private fun readInt(bytes: ByteArray, offset: Int): Int =
        readShort(bytes, offset) or (readShort(bytes, offset + 2) shl 16)
}
