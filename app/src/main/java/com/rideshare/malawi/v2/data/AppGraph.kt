package com.rideshare.malawi.v2.data

import android.content.Context
import com.rideshare.malawi.v2.data.session.UserSessionManager

object AppGraph {
    private var sessionManager: UserSessionManager? = null

    var repository: RideRepository = FirestoreRideRepository(sessionManager = sessionManager)

    fun initialize(context: Context) {
        val manager = UserSessionManager(context.applicationContext)
        sessionManager = manager
        if (repository is FirestoreRideRepository) {
            repository = FirestoreRideRepository(sessionManager = manager)
        } else if (repository is InMemoryRideRepository) {
            repository = InMemoryRideRepository(sessionManager = manager)
        }
    }
}
