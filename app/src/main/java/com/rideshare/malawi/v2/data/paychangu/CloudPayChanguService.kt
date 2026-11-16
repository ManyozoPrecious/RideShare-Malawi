package com.rideshare.malawi.v2.data.paychangu

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class CloudPayChanguService(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) {
    suspend fun createMobileMoneyPayment(
        rideId: String,
        phoneNumber: String,
        operator: MobileMoneyOperator
    ): Result<String> {
        return try {
            val data = mapOf(
                "rideId" to rideId,
                "phoneNumber" to phoneNumber,
                "operator" to operator.refId
            )
            val result = functions.getHttpsCallable("createMobileMoneyPayment")
                .call(data)
                .await()

            val resultMap = result.getData() as? Map<*, *>
            val chargeId = resultMap?.get("chargeId") as? String
                ?: return Result.failure(Exception("Cloud function did not return a valid charge ID."))

            Result.success(chargeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyMobileMoneyPayment(chargeId: String): Result<Boolean> {
        return try {
            val data = mapOf("chargeId" to chargeId)
            val result = functions.getHttpsCallable("verifyMobileMoneyPayment")
                .call(data)
                .await()

            val resultMap = result.getData() as? Map<*, *>
            val status = resultMap?.get("status") as? String
            val isSuccess = status == "successful"

            Result.success(isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
