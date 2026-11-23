package com.rideshare.malawi.v2.data.worker

import android.content.Context
import androidx.work.*
import com.rideshare.malawi.v2.data.AppGraph
import com.rideshare.malawi.v2.data.PaymentStatus
import com.rideshare.malawi.v2.data.paychangu.CloudPayChanguService
import java.util.concurrent.TimeUnit

class PaymentStatusWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val chargeId = inputData.getString(KEY_CHARGE_ID) ?: return Result.failure()
        val requestId = inputData.getString(KEY_REQUEST_ID) ?: return Result.failure()

        val cloudService = CloudPayChanguService()
        val verifyResult = cloudService.verifyMobileMoneyPayment(chargeId)

        return if (verifyResult.isSuccess && verifyResult.getOrNull() == true) {
            AppGraph.repository.updatePaymentStatus(requestId, PaymentStatus.SUCCESS)
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        const val KEY_CHARGE_ID = "charge_id"
        const val KEY_REQUEST_ID = "request_id"

        fun schedulePaymentSync(context: Context, chargeId: String, requestId: String) {
            val inputData = workDataOf(
                KEY_CHARGE_ID to chargeId,
                KEY_REQUEST_ID to requestId
            )
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<PaymentStatusWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "paychangu_verify_$chargeId",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }
}
