package com.zhuomo.glasspomodoro.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class BingImage(
    val url: String,
    val title: String,
    val copyright: String,
    val fullUrl: String
)

class BingWallpaperFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchToday(region: String = "zh-CN"): BingImage? = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=$region"
            val request = Request.Builder().url(apiUrl).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val image = json.getJSONArray("images").getJSONObject(0)
            val url = image.getString("url")
            val title = image.optString("title", "")
            val copyright = image.optString("copyright", "")
            // Build full URL
            val baseUrl = if (url.startsWith("http")) url else "https://www.bing.com$url"
            val fullUrl = baseUrl.replace("_1920x1080.jpg", "_UHD.jpg")
                .replace("_1366x768.jpg", "_UHD.jpg")
            
            BingImage(url = url, title = title, copyright = copyright, fullUrl = fullUrl)
        } catch (e: Exception) {
            null
        }
    }
}
