package com.zclei.hitrise.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TrainingMusicSynthTest {
    @Test
    fun everyTrainingBpmHasAnAutomaticBackground() {
        for (bpm in TrainingMusicSynth.MIN_BPM..TrainingMusicSynth.MAX_BPM) {
            val backing = TrainingMusicSynth.backingFor(bpm, autoMusic = true, championAvailable = true)
            assertEquals(
                if (bpm in 232..256) TrainingMusicSynth.Backing.Champion else TrainingMusicSynth.Backing.Adaptive,
                backing,
            )
        }
        assertEquals(
            TrainingMusicSynth.Backing.None,
            TrainingMusicSynth.backingFor(180, autoMusic = false, championAvailable = true),
        )
    }

    @Test
    fun audibleKickFollowsSelectedBpmAcrossRange() {
        for (bpm in intArrayOf(180, 240, 300, 350, 400)) {
            val beatMs = 60_000.0 / bpm
            val samples = ShortArray(((beatMs * 5 + 100) * TrainingMusicSynth.SAMPLE_RATE / 1_000).toInt())
            TrainingMusicSynth.render(
                samples, 0L, bpm, includeGroove = true,
                backing = TrainingMusicSynth.Backing.None, championSamples = null,
            )
            for (beat in 1..4) {
                val target = beat * beatMs
                val onBeat = rms(samples, target + 35, target + 75)
                val beforeBeat = rms(samples, target - 35, target - 10)
                assertTrue("Kick did not follow $bpm BPM at beat $beat", onBeat > beforeBeat * 1.3)
            }
        }
    }

    @Test
    fun beatScoringWindowsDoNotOverlapAtHighTempo() {
        for (bpm in TrainingMusicSynth.MIN_BPM..TrainingMusicSynth.MAX_BPM) {
            val windows = TrainingMusicSynth.beatWindows(bpm)
            assertTrue(windows.perfectMs < windows.goodMs)
            assertTrue("Scoring windows overlap at $bpm BPM", windows.goodMs * 2 < 60_000.0 / bpm)
        }
        assertEquals(TrainingMusicSynth.BeatWindows(73, 133), TrainingMusicSynth.beatWindows(180))
        assertEquals(TrainingMusicSynth.BeatWindows(33, 60), TrainingMusicSynth.beatWindows(400))
    }

    @Test
    fun renderingIsContinuousAcrossAudioBuffers() {
        for (bpm in intArrayOf(180, 400)) {
            for (backing in arrayOf(TrainingMusicSynth.Backing.None, TrainingMusicSynth.Backing.Adaptive)) {
                val whole = ShortArray(8_192)
                TrainingMusicSynth.render(whole, 0L, bpm, true, backing, null)
                val first = ShortArray(4_096)
                val second = ShortArray(4_096)
                TrainingMusicSynth.render(first, 0L, bpm, true, backing, null)
                TrainingMusicSynth.render(second, 4_096L, bpm, true, backing, null)
                assertArrayEquals(whole, first + second)
            }
        }
    }

    @Test
    fun bundledChampionRushCanBeDecodedForSynchronizedPlayback() {
        val wav = File("src/main/assets/music/01_htr_music_champion_rush.wav")
        val samples = TrainingMusicSynth.decodeChampionWav(wav.readBytes())
        assertTrue("Champion Rush WAV could not be decoded", samples != null && samples.isNotEmpty())
        assertEquals(347_016, samples?.size)
    }

    private fun rms(samples: ShortArray, startMs: Double, endMs: Double): Double {
        val start = (startMs * TrainingMusicSynth.SAMPLE_RATE / 1_000).toInt().coerceIn(0, samples.size)
        val end = (endMs * TrainingMusicSynth.SAMPLE_RATE / 1_000).toInt().coerceIn(start, samples.size)
        return kotlin.math.sqrt((start until end).map { samples[it].toDouble() * samples[it] }.average())
    }
}
