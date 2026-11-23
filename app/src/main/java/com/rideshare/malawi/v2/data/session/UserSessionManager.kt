package com.rideshare.malawi.v2.data.session

import android.content.Context
import android.content.SharedPreferences
import com.rideshare.malawi.v2.data.User
import com.rideshare.malawi.v2.data.UserRole
import com.rideshare.malawi.v2.data.VerificationStatus

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveUserSession(user: User) {
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_NAME, user.name)
            .putString(KEY_PHONE, user.phone)
            .putString(KEY_PASSWORD, user.password)
            .putString(KEY_ROLE, user.role.name)
            .putFloat(KEY_RATING, user.rating)
            .putString(KEY_VEHICLE_MODEL, user.vehicleModel)
            .putString(KEY_VEHICLE_REG, user.vehicleReg)
            .putString(KEY_DOCUMENT_URI, user.documentUri)
            .putString(KEY_VERIFICATION, user.verification.name)
            .apply()
    }

    fun restoreUserSession(): User? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val name = prefs.getString(KEY_NAME, "") ?: ""
        val phone = prefs.getString(KEY_PHONE, "") ?: ""
        val password = prefs.getString(KEY_PASSWORD, "") ?: ""
        val roleStr = prefs.getString(KEY_ROLE, UserRole.PASSENGER.name) ?: UserRole.PASSENGER.name
        val rating = prefs.getFloat(KEY_RATING, 4.7f)
        val vehicleModel = prefs.getString(KEY_VEHICLE_MODEL, "") ?: ""
        val vehicleReg = prefs.getString(KEY_VEHICLE_REG, "") ?: ""
        val documentUri = prefs.getString(KEY_DOCUMENT_URI, null)
        val verificationStr = prefs.getString(KEY_VERIFICATION, VerificationStatus.NOT_SUBMITTED.name) ?: VerificationStatus.NOT_SUBMITTED.name

        return try {
            User(
                id = id,
                name = name,
                phone = phone,
                password = password,
                role = UserRole.valueOf(roleStr),
                rating = rating,
                vehicleModel = vehicleModel,
                vehicleReg = vehicleReg,
                documentUri = documentUri,
                verification = VerificationStatus.valueOf(verificationStr)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "rideshare_user_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "user_name"
        private const val KEY_PHONE = "user_phone"
        private const val KEY_PASSWORD = "user_password"
        private const val KEY_ROLE = "user_role"
        private const val KEY_RATING = "user_rating"
        private const val KEY_VEHICLE_MODEL = "user_vehicle_model"
        private const val KEY_VEHICLE_REG = "user_vehicle_reg"
        private const val KEY_DOCUMENT_URI = "user_document_uri"
        private const val KEY_VERIFICATION = "user_verification"
    }
}
