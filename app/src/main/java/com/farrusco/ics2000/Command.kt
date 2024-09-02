
class Command {

    private val _header = ByteArray(43)
    private var _data = ""

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
            Bytes.insertBytes(_header, arr, 3)
        }
    }

    fun setMagic() {
        val num = 653213
        Bytes.insertInt32(_header, num, 9)
    }

    fun setEntityId(entityid: Int) {
        Bytes.insertInt32(_header, entityid, 29)
    }

    fun setData(data: String, aes: String) {
        _data = Crypto.encrypt(data, aes)
    }

    fun getCommand(): String {
        Bytes.insertInt16(_header, _data.length/2, 41)
        return _header.joinToString("") { String.format("%02x", it) } + _data
    }
}
