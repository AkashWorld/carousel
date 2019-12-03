package server

import java.security.MessageDigest

class ServerAuthentication {
    private var hashedPassword: String? = null;

    fun setServerPassword(password: String) {
        val digest = MessageDigest.getInstance("SHA-256")
        val encodedHash = digest.digest(password.toByteArray())
        this.hashedPassword = bytesToHex(encodedHash)
    }

    fun verifyPassword(password: String?): Boolean {
        if(this.hashedPassword == null) return true;
        return hashedPassword == password
    }
}

fun bytesToHex(hash: ByteArray): String {
    val hexString = StringBuffer()
    for ((index, _) in hash.withIndex()) {
        val hex: String = Integer.toHexString(0xff and hash[index].toInt())
        if (hex.length == 1) hexString.append('0')
        hexString.append(hex)
    }
    return hexString.toString()
}
