package com.chaquo.myapplication
import java.nio.ByteBuffer

class Command {

    private val _header = ByteArray(43)
    private var _data = ByteArray(0)

    init {
        setFrame(1)
    }

    fun setFrame(num: Int) {
        if (num in 0..255) {
            _header[0] = num.toByte()
        }
    }

    fun setType(num: Int) {
        if (num in 0..255) {
            _header[2] = num.toByte()
        }
    }

    fun setMac(mac: String) {
        val arr = mac.replace(":", "").chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        if (arr.size == 6) {
            insertbytes(_header, arr, 3)
        }
    }

    fun setMagic() {
        val num = 653213
        insertint32(_header, num, 9)
    }

    fun setEntityId(entityid: Int) {
        insertint32(_header, entityid, 29)
    }

    fun setData(data: ByteArray, aes: String) {
        _data = Crypto.encrypt(data.toString(), aes)
    }

    fun getCommand(): String {
        insertint16(_header, _data.size, 41)
        return _header.joinToString("") { String.format("%02x", it) } + _data.joinToString("") { String.format("%02x", it) }
    }

    private fun insertbytes(header: ByteArray, bytes: ByteArray, offset: Int) {
        System.arraycopy(bytes, 0, header, offset, bytes.size)
    }

    private fun insertint32(header: ByteArray, value: Int, offset: Int) {
        val buffer = ByteBuffer.allocate(4).putInt(value)
        System.arraycopy(buffer.array(), 0, header, offset, 4)
    }

    private fun insertint16(header: ByteArray, value: Int, offset: Int) {
        val buffer = ByteBuffer.allocate(2).putShort(value.toShort())
        System.arraycopy(buffer.array(), 0, header, offset, 2)
    }

}


