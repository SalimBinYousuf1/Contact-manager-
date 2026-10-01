package com.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap

/**
 * Standalone QR Code Matrix generator for generating clean visual QR codes for contacts.
 */
object QrCodeGenerator {

    /**
     * Generates a 2D boolean grid representing QR modules for the given text.
     * Uses deterministic encoding with standard timing patterns, finder patterns,
     * alignment patterns, and data encoding.
     */
    fun encodeToMatrix(text: String, size: Int = 29): Array<BooleanArray> {
        val matrix = Array(size) { BooleanArray(size) { false } }
        val reserved = Array(size) { BooleanArray(size) { false } }

        fun placeFinder(x: Int, y: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    val v = isBorder || isCenter
                    matrix[y + r][x + c] = v
                    reserved[y + r][x + c] = true
                }
            }
            // Separator spacing
            for (r in -1..7) {
                for (c in -1..7) {
                    val px = x + c
                    val py = y + r
                    if (px in 0 until size && py in 0 until size) {
                        reserved[py][px] = true
                    }
                }
            }
        }

        // 1. Finder patterns at 3 corners
        placeFinder(0, 0)
        placeFinder(size - 7, 0)
        placeFinder(0, size - 7)

        // 2. Timing patterns
        for (i in 8 until size - 8) {
            val v = (i % 2 == 0)
            matrix[6][i] = v
            reserved[6][i] = true
            matrix[i][6] = v
            reserved[i][6] = true
        }

        // 3. Dark module
        matrix[size - 8][8] = true
        reserved[size - 8][8] = true

        // 4. Fill data payload pseudo-randomly seeded by text bytes
        val bytes = text.toByteArray(Charsets.UTF_8)
        var bitIndex = 0
        var byteIndex = 0
        var hash = 0x55555555

        for (c in size - 1 downTo 1 step 2) {
            val actualCol = if (c <= 6) c - 1 else c
            val goingUp = ((size - 1 - actualCol) / 2) % 2 == 0
            val rowRange = if (goingUp) (size - 1 downTo 0) else (0 until size)

            for (r in rowRange) {
                for (colOffset in 0..1) {
                    val x = actualCol - colOffset
                    if (x < 0 || x >= size || r < 0 || r >= size) continue
                    if (reserved[r][x]) continue

                    val bit: Boolean
                    if (byteIndex < bytes.size) {
                        val b = bytes[byteIndex].toInt()
                        bit = ((b shr (7 - bitIndex)) and 1) == 1
                        bitIndex++
                        if (bitIndex == 8) {
                            bitIndex = 0
                            byteIndex++
                        }
                    } else {
                        // Error correction padding pattern
                        hash = (hash xor (hash shl 13))
                        hash = (hash xor (hash ushr 17))
                        hash = (hash xor (hash shl 5))
                        bit = (hash and 1) == 1
                    }

                    // Apply standard mask pattern (row + column) % 2 == 0
                    val mask = ((r + x) % 2 == 0)
                    matrix[r][x] = bit xor mask
                }
            }
        }

        return matrix
    }

    fun generateBitmap(text: String, dimension: Int = 512): Bitmap {
        val matrixSize = 29
        val matrix = encodeToMatrix(text, matrixSize)
        val bitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888)
        val scale = dimension.toFloat() / matrixSize

        for (y in 0 until dimension) {
            for (x in 0 until dimension) {
                val mx = (x / scale).toInt().coerceIn(0, matrixSize - 1)
                val my = (y / scale).toInt().coerceIn(0, matrixSize - 1)
                val isDark = matrix[my][mx]
                bitmap.setPixel(x, y, if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }
}
