package com.bloodpressure.app.data.remote

import com.bloodpressure.app.domain.model.BloodPressureRecord
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

sealed class FeishuResult {
    object Success : FeishuResult()
    data class Error(val message: String) : FeishuResult()
}

@Singleton
class FeishuService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private var cachedToken: String? = null
    private var tokenExpireTime: Long = 0

    suspend fun testConnection(
        appId: String,
        appSecret: String,
        tableToken: String
    ): FeishuResult = withContext(Dispatchers.IO) {
        if (appId.isBlank() || appSecret.isBlank() || tableToken.isBlank()) {
            return@withContext FeishuResult.Error("配置不能为空")
        }

        val tokenResult = getToken(appId, appSecret)
        when (tokenResult) {
            null -> return@withContext FeishuResult.Error("获取Token失败")
            else -> {
                val testResult = testTableWrite(tokenResult, tableToken)
                testResult
            }
        }
    }

    private suspend fun getToken(appId: String, appSecret: String): String? {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedToken
        }

        val requestBody = JsonObject().apply {
            addProperty("app_id", appId)
            addProperty("app_secret", appSecret)
        }

        val request = Request.Builder()
            .url("https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            val json = gson.fromJson(body, JsonObject::class.java)

            if (json.get("code")?.asInt == 0) {
                val token = json.get("tenant_access_token")?.asString
                val expire = json.get("expire")?.asLong ?: 0
                tokenExpireTime = System.currentTimeMillis() + (expire - 300) * 1000
                cachedToken = token
                token
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun testTableWrite(token: String, tableToken: String): FeishuResult {
        val requestBody = JsonObject().apply {
            add("value", JsonArray().apply {
                add("测试")
                add("连接")
            })
        }

        val request = Request.Builder()
            .url("https://open.feishu.cn/open-apis/sheets/v4/spreadsheets/$tableToken/values/A1:append?valueInputOption=RAW")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            val json = gson.fromJson(body, JsonObject::class.java)

            if (json.get("code")?.asInt == 0) {
                FeishuResult.Success
            } else {
                FeishuResult.Error("表格访问失败: ${json.get("msg")?.asString}")
            }
        } catch (e: Exception) {
            FeishuResult.Error("连接异常: ${e.message}")
        }
    }

    suspend fun syncToTable(
        appId: String,
        appSecret: String,
        tableToken: String,
        record: BloodPressureRecord
    ): FeishuResult = withContext(Dispatchers.IO) {
        val token = getToken(appId, appSecret)
            ?: return@withContext FeishuResult.Error("获取Token失败")

        val values = JsonArray().apply {
            add(record.date.toString())
            add(if (record.period.name == "MORNING") "早上" else "晚上")
            add(record.systolic.toString())
            add(record.diastolic.toString())
            add(record.heartRate?.toString() ?: "")
            add(record.note ?: "")
            add(record.createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        }

        val requestBody = JsonObject().apply {
            add("value", values)
        }

        val request = Request.Builder()
            .url("https://open.feishu.cn/open-apis/sheets/v4/spreadsheets/$tableToken/values/A1:append?valueInputOption=RAW")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            val json = gson.fromJson(body, JsonObject::class.java)

            if (json.get("code")?.asInt == 0) {
                FeishuResult.Success
            } else {
                FeishuResult.Error("写入失败: ${json.get("msg")?.asString}")
            }
        } catch (e: Exception) {
            FeishuResult.Error("同步异常: ${e.message}")
        }
    }

    fun clearCache() {
        cachedToken = null
        tokenExpireTime = 0
    }
}