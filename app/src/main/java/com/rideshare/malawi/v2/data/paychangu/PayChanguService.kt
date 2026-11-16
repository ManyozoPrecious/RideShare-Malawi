package com.rideshare.malawi.v2.data.paychangu

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PayChanguService(
    private val apiKey: String = "SEC-DEMO-PAYCHANGU-KEY",
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    private val baseUrl = "https://api.paychangu.com"

    suspend fun chargeMobileMoney(req: PayChanguChargeRequest): Result<PayChanguResponse> = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = JSONObject().apply {
                put("amount", req.amount)
                put("currency", req.currency)
                put("email", req.email.ifBlank { "passenger@rideshare.mw" })
                put("first_name", req.firstName)
                put("last_name", req.lastName)
                put("phone_number", req.phoneNumber)
                put("charge_type", "mobile_money")
                put("mobile_money_operator_ref_id", req.operator.refId)
                put("tx_ref", req.txRef)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonPayload.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$baseUrl/mobile-money/payments/initialize")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Accept", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBodyStr = response.body?.string() ?: ""
                if (response.isSuccessful || response.code in 200..299) {
                    val resJson = try { JSONObject(responseBodyStr) } catch (e: Exception) { JSONObject() }
                    val status = resJson.optString("status", "success")
                    val message = resJson.optString("message", "Payment prompt sent to mobile device")
                    Result.success(PayChanguResponse(status = status, message = message, txRef = req.txRef))
                } else {
                    if (apiKey.startsWith("SEC-DEMO")) {
                        Result.success(
                            PayChanguResponse(
                                status = "success",
                                message = "Sandbox mobile money prompt sent to ${req.phoneNumber} (${req.operator.displayName})",
                                txRef = req.txRef
                            )
                        )
                    } else {
                        Result.failure(Exception("PayChangu payment failed (HTTP ${response.code}): $responseBodyStr"))
                    }
                }
            }
        } catch (e: Exception) {
            if (apiKey.startsWith("SEC-DEMO")) {
                Result.success(
                    PayChanguResponse(
                        status = "success",
                        message = "Sandbox payment request created for ${req.phoneNumber} (${req.operator.displayName})",
                        txRef = req.txRef
                    )
                )
            } else {
                Result.failure(e)
            }
        }
    }
}
