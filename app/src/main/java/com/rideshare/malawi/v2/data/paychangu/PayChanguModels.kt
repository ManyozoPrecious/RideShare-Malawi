package com.rideshare.malawi.v2.data.paychangu

enum class MobileMoneyOperator(val displayName: String, val refId: String) {
    AIRTEL_MONEY("Airtel Money", "airtel"),
    TNM_MPAMBA("TNM Mpamba", "tnm")
}

data class PayChanguChargeRequest(
    val amount: Int,
    val currency: String = "MWK",
    val operator: MobileMoneyOperator,
    val phoneNumber: String,
    val email: String = "passenger@rideshare.mw",
    val txRef: String,
    val firstName: String = "Passenger",
    val lastName: String = "User"
)

data class PayChanguResponse(
    val status: String,
    val message: String,
    val txRef: String
)
