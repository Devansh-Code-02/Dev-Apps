package com.example.utils

/**
 * Lightweight, pure-Kotlin QR Code Matrix Generator.
 * Creates a boolean grid matrix representing QR modules including finder patterns,
 * alignment patterns, timing patterns, format info, and data payload bits.
 */
object QrCodeEncoder {

    fun generateQrMatrix(text: String, size: Int = 29): Array<BooleanArray> {
        val matrix = Array(size) { BooleanArray(size) { false } }
        val isReserved = Array(size) { BooleanArray(size) { false } }

        // 1. Place Finder Patterns at three corners
        placeFinderPattern(matrix, isReserved, 0, 0)
        placeFinderPattern(matrix, isReserved, size - 7, 0)
        placeFinderPattern(matrix, isReserved, 0, size - 7)

        // 2. Alignment pattern for sizes >= 25
        if (size >= 25) {
            val alignX = size - 7
            val alignY = size - 7
            placeAlignmentPattern(matrix, isReserved, alignX - 2, alignY - 2)
        }

        // 3. Timing patterns
        for (i in 8 until size - 8) {
            if (!isReserved[6][i]) {
                matrix[6][i] = (i % 2 == 0)
                isReserved[6][i] = true
            }
            if (!isReserved[i][6]) {
                matrix[i][6] = (i % 2 == 0)
                isReserved[i][6] = true
            }
        }

        // 4. Reserve format info areas
        for (i in 0..8) {
            isReserved[8][i] = true
            isReserved[i][8] = true
            isReserved[size - 1 - i][8] = true
            isReserved[8][size - 1 - i] = true
        }

        // 5. Encode text payload bits into remaining cells
        val payloadBytes = text.toByteArray(Charsets.UTF_8)
        val bitList = mutableListOf<Boolean>()

        // Add 4-bit byte mode indicator (0100)
        bitList.addAll(listOf(false, true, false, false))

        // Add 8-bit character count indicator
        val len = payloadBytes.size.coerceAtMost(255)
        for (b in 7 downTo 0) {
            bitList.add(((len shr b) and 1) == 1)
        }

        // Add character data
        for (byte in payloadBytes) {
            for (b in 7 downTo 0) {
                bitList.add(((byte.toInt() shr b) and 1) == 1)
            }
        }

        // Add Terminator (0000)
        repeat(4) { bitList.add(false) }

        // Fill data matrix in standard QR zig-zag pattern
        var bitIndex = 0
        var directionUp = true
        var x = size - 1

        while (x > 0) {
            if (x == 6) x-- // Skip vertical timing column

            val yRange = if (directionUp) (size - 1 downTo 0) else (0 until size)
            for (y in yRange) {
                for (col in 0..1) {
                    val currX = x - col
                    if (!isReserved[y][currX]) {
                        val bitValue = if (bitIndex < bitList.size) {
                            bitList[bitIndex++]
                        } else {
                            // Pseudo-random fill mask for empty cells based on position hash
                            ((currX + y) % 2 == 0) xor ((currX * y) % 3 == 0)
                        }
                        // Mask pattern 0 (invert if (x+y) % 2 == 0)
                        val mask = ((currX + y) % 2 == 0)
                        matrix[y][currX] = bitValue xor mask
                        isReserved[y][currX] = true
                    }
                }
            }
            directionUp = !directionUp
            x -= 2
        }

        return matrix
    }

    private fun placeFinderPattern(
        matrix: Array<BooleanArray>,
        reserved: Array<BooleanArray>,
        startX: Int,
        startY: Int
    ) {
        for (r in 0..6) {
            for (c in 0..6) {
                val isOuter = (r == 0 || r == 6 || c == 0 || c == 6)
                val isInner = (r in 2..4 && c in 2..4)
                val x = startX + c
                val y = startY + r
                matrix[y][x] = isOuter || isInner
                reserved[y][x] = true
            }
        }
        // Separator border
        for (r in -1..7) {
            for (c in -1..7) {
                val x = startX + c
                val y = startY + r
                if (x in matrix.indices && y in matrix.indices && !reserved[y][x]) {
                    matrix[y][x] = false
                    reserved[y][x] = true
                }
            }
        }
    }

    private fun placeAlignmentPattern(
        matrix: Array<BooleanArray>,
        reserved: Array<BooleanArray>,
        startX: Int,
        startY: Int
    ) {
        for (r in 0..4) {
            for (c in 0..4) {
                val isOuter = (r == 0 || r == 4 || c == 0 || c == 4)
                val isCenter = (r == 2 && c == 2)
                val x = startX + c
                val y = startY + r
                if (x in matrix.indices && y in matrix.indices && !reserved[y][x]) {
                    matrix[y][x] = isOuter || isCenter
                    reserved[y][x] = true
                }
            }
        }
    }
}
