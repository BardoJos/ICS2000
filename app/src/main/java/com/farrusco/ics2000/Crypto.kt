package com.chaquo.myapplication
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Crypto {

    private val blockSize = 16

    fun pad(s: String): String {
        val padding = blockSize - s.length % blockSize
        return s + (padding.toChar().toString().repeat(padding))
    }

    fun decrypt(string: String, aes: String): String {
        val base = Base64.getDecoder().decode(string)
        val iv = base.copyOfRange(0, 16)
        val inp = base.copyOfRange(16, base.size)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(aes.hexToByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return String(cipher.doFinal(inp))
    }

    fun encrypt(string: String, aes: String): ByteArray {
        val base = pad(string).toByteArray()
        val iv = ByteArray(16)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(aes.hexToByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        return iv + cipher.doFinal(base)
    }

    fun String.hexToByteArray(): ByteArray {
        return this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

}