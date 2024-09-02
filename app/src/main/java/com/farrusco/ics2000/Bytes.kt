
import kotlin.math.pow


object Bytes {

    val MAX_UINT_16 = (2.0).pow(16.0) - 1.0
    val MAX_VALUE_U_INT_32 = 2.0.pow(32.0).toLong() - 1L

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
    fun hexToByteArray(s: String): ByteArray {
        val length = s.length
        val array = ByteArray(length / 2)
        var n = 0
        while (true) {
            val n2 = n + 1
            if (n2 >= length) {
                break
            }
            array[n / 2] = ((s[n].digitToIntOrNull(16) ?: -1 shl 4) + s[n2].digitToIntOrNull(16)!!
                ?: -1).toByte()
            n += 2
        }
        return array
    }
    fun hexToByteArrayx(string: String): ByteArray {
        val ar =  string.chunked(2).map{ it.toInt(16)  }
        val ba = ByteArray(ar.size)
        ar.forEachIndexed { index, i -> ba[index] = i.toByte() }
        return ba
        //return this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    fun byteArrayToHex(ba: ByteArray): String{
        var rtn = ""
        ba.forEach {
            rtn +=  (0xff and it.toInt()).toString(16).padStart(2,'0')
        }
        return rtn
    }

    fun pad(s: String, blockSize:Int): String {
        val padding = blockSize - s.length % blockSize
        return s + (padding.toChar().toString().repeat(padding))
    }
}
