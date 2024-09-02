
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Crypto {

    const val blockSize = 16

    fun decrypt(string: String, aes: String): String {
        val base = Base64.getDecoder().decode(string)
        val iv = base.copyOfRange(0, 16)
        val inp = base.copyOfRange(16, base.size)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(aes.hexToByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return String(cipher.doFinal(inp))
    }

    fun encrypt(string: String, aes: String): String {

        val padding = blockSize - string.length % blockSize
        val base = (string + (padding.toChar().toString().repeat(0))).toByteArray()

        val iv = ByteArray(16)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(aes.hexToByteArray(), "AES")

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        return Bytes.byteArrayToHex(iv ) + Bytes.byteArrayToHex( cipher.doFinal(base))

    }

    fun String.hexToByteArray(): ByteArray {
        return this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
