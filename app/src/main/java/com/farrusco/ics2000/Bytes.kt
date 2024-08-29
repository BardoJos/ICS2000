
import kotlin.math.pow

val MAX_UINT_16 = (2.0).pow(16.0) - 1.0

fun insertInt32(arr: ByteArray, num: Int, start: Int) {
    arr[start] = (num and 0xFF).toByte()
    arr[start + 1] = (num shr 8 and 0xFF).toByte()
    arr[start + 2] = (num shr 16 and 0xFF).toByte()
    arr[start + 3] = (num shr 24 and 0xFF).toByte()
}

fun insertInt16(arr: ByteArray, num: Int, start: Int) {
    arr[start] = (num and 0xFF).toByte()
    arr[start + 1] = (num shr 8 and 0xFF).toByte()
}

fun insertBytes(arr: ByteArray, inp: ByteArray, start: Int) {
    for (i in inp.indices) {
        arr[i + start] = inp[i]
    }
}

fun byteToInt2(byte1: Byte, byte2: Byte): Int {
    return (byte1.toInt() and 0xFF or (byte2.toInt() and 0xFF shl 8)) and 0xFFFF
}

fun byteToInt4(byte1: Byte, byte2: Byte, byte3: Byte, byte4: Byte): Int {
    return (byte1.toInt() and 0xFF or (byte2.toInt() and 0xFF shl 8) or (byte3.toInt() and 0xFF shl 16) or (byte4.toInt() and 0xFF shl 24)) and 0xFFFF
}
