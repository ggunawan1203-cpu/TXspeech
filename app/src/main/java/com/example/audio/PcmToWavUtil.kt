package com.example.audio

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object PcmToWavUtil {

    /**
     * Checks if data starts with "RIFF" (WAV) or ID3/0xFFFB (MP3).
     * If not, wraps raw PCM 16-bit 24kHz mono with standard 44-byte WAV header.
     */
    fun ensureWavOrCompressed(data: ByteArray, sampleRate: Int = 24000, channels: Int = 1): ByteArray {
        if (data.size < 12) return data

        // Check for RIFF header
        if (data[0] == 'R'.code.toByte() && data[1] == 'I'.code.toByte() &&
            data[2] == 'F'.code.toByte() && data[3] == 'F'.code.toByte()
        ) {
            return data
        }

        // Check for ID3 or MP3 sync word
        if ((data[0] == 'I'.code.toByte() && data[1] == 'D'.code.toByte() && data[2] == '3'.code.toByte()) ||
            (data[0] == 0xFF.toByte() && (data[1].toInt() and 0xE0) == 0xE0)
        ) {
            return data
        }

        // Add WAV header
        return pcmToWav(data, sampleRate, channels, 16)
    }

    private fun pcmToWav(pcmData: ByteArray, sampleRate: Int, channels: Int, bitsPerSample: Int): ByteArray {
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8

        val header = ByteBuffer.allocate(44).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            // RIFF header
            put('R'.code.toByte())
            put('I'.code.toByte())
            put('F'.code.toByte())
            put('F'.code.toByte())
            putInt(totalDataLen)
            put('W'.code.toByte())
            put('A'.code.toByte())
            put('V'.code.toByte())
            put('E'.code.toByte())

            // fmt chunk
            put('f'.code.toByte())
            put('m'.code.toByte())
            put('t'.code.toByte())
            put(' '.code.toByte())
            putInt(16) // Subchunk1Size for PCM
            putShort(1.toShort()) // AudioFormat (1 = PCM)
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign.toShort())
            putShort(bitsPerSample.toShort())

            // data chunk
            put('d'.code.toByte())
            put('a'.code.toByte())
            put('t'.code.toByte())
            put('a'.code.toByte())
            putInt(totalAudioLen)
        }.array()

        val output = ByteArrayOutputStream(header.size + pcmData.size)
        output.write(header)
        output.write(pcmData)
        return output.toByteArray()
    }
}
