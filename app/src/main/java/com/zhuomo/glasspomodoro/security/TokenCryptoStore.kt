package com.zhuomo.glasspomodoro.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * v2.0 模块E：GitHub Token 加密存储
 *
 * 使用 Android Keystore System 生成不可导出的 AES-256 密钥，
 * 以 GCM 模式加密 Token 后存入 SharedPreferences。
 * - 密钥存于 TEE/SE（硬件安全单元），应用进程无法导出
 * - 密文与 IV 分离存储，杜绝明文泄露
 * - 卸载应用或设备恢复出厂设置后密钥自动失效
 */
class TokenCryptoStore(private val context: Context) {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "glasspomodoro_github_token_v2"
        private const val PREFS_NAME = "secure_github_token"
        private const val PREF_CIPHER_TEXT = "token_ciphertext"
        private const val PREF_IV = "token_iv"
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    /** 加密并保存 Token（AES-256-GCM，密钥在 Keystore 中） */
    fun saveToken(token: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(PREF_CIPHER_TEXT, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(PREF_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    /** 解密读取 Token；密钥失效或数据损坏时返回 null */
    fun loadToken(): String? = try {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cipherText = prefs.getString(PREF_CIPHER_TEXT, null) ?: return null
        val iv = prefs.getString(PREF_IV, null) ?: return null
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP))
        )
        String(cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP)), Charsets.UTF_8)
    } catch (_: Exception) {
        null
    }

    /** 清除存储的 Token（退出登录） */
    fun clearToken() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }

    /** 是否已配置 Token */
    fun hasToken(): Boolean = loadToken()?.isNotBlank() == true
}
