package com.zhuomo.glasspomodoro.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * v2.0 模块E：GitHub API 客户端
 *
 * - 验证 Token（GET /user）
 * - 配置备份/恢复（Gist API，需 gist scope）
 */
data class GitHubUser(
    val login: String,
    val name: String,
    val avatarUrl: String,
    val publicRepos: Int
)

class GitHubApiClient {

    companion object {
        const val DEFAULT_GIST_FILENAME = "glasspomodoro-config.json"
        const val DEFAULT_GIST_DESCRIPTION = "GlassPomodoro v2.0 配置备份"
        const val TOKEN_HELP_URL = "https://github.com/settings/tokens/new?scopes=gist&description=GlassPomodoro"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    private fun authRequest(url: String, token: String): Request.Builder =
        Request.Builder().url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")

    /** 验证 Token 并返回用户信息；无效/网络失败返回 null */
    suspend fun verifyToken(token: String): GitHubUser? = withContext(Dispatchers.IO) {
        try {
            val request = authRequest("https://api.github.com/user", token).build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val json = JSONObject(resp.body?.string() ?: "{}")
                GitHubUser(
                    login = json.optString("login", ""),
                    name = json.optString("name", ""),
                    avatarUrl = json.optString("avatar_url", ""),
                    publicRepos = json.optInt("public_repos", 0)
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 将配置备份到 Gist（不存在则创建，存在则更新）
     * @return Gist ID；失败返回 null
     */
    suspend fun backupConfigToGist(
        token: String,
        gistId: String?,
        fileName: String = DEFAULT_GIST_FILENAME,
        content: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject()
                .put("description", DEFAULT_GIST_DESCRIPTION)
                .put("public", false)
                .put("files", JSONObject().put(fileName, JSONObject().put("content", content)))

            val request = if (gistId.isNullOrBlank()) {
                authRequest("https://api.github.com/gists", token)
                    .post(body.toString().toRequestBody(jsonMedia)).build()
            } else {
                authRequest("https://api.github.com/gists/$gistId", token)
                    .patch(body.toString().toRequestBody(jsonMedia)).build()
            }

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                JSONObject(resp.body?.string() ?: "{}").optString("id", "").ifBlank { null }
            }
        } catch (_: Exception) {
            null
        }
    }

    /** 读取 Gist 中备份的配置内容 */
    suspend fun fetchConfigFromGist(token: String, gistId: String, fileName: String = DEFAULT_GIST_FILENAME): String? =
        withContext(Dispatchers.IO) {
            try {
                val request = authRequest("https://api.github.com/gists/$gistId", token).build()
                client.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext null
                    val json = JSONObject(resp.body?.string() ?: "{}")
                    json.optJSONObject("files")?.optJSONObject(fileName)?.optString("content", null)
                }
            } catch (_: Exception) {
                null
            }
        }

    /** 列出当前用户的所有 Gist（用于查找配置备份） */
    suspend fun listGists(token: String, fileName: String = DEFAULT_GIST_FILENAME): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val request = authRequest("https://api.github.com/gists?per_page=100", token).build()
                client.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext emptyList()
                    val arr = org.json.JSONArray(resp.body?.string() ?: "[]")
                    buildList {
                        for (i in 0 until arr.length()) {
                            val gist = arr.getJSONObject(i)
                            if (gist.optJSONObject("files")?.has(fileName) == true) {
                                add(gist.optString("id", ""))
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
}
