package com.rideshare.malawi.v2.data

object AppGraph {
    val repository: RideRepository by lazy { InMemoryRideRepository() }
}
