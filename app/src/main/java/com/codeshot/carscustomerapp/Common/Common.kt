package com.codeshot.carscustomerapp.Common

import com.codeshot.carscustomerapp.Remote.FCMClient
import com.codeshot.carscustomerapp.Remote.GoogleMapApiClient
import com.codeshot.carscustomerapp.Remote.IFCMService
import com.codeshot.carscustomerapp.Remote.IGoogleAPI

object Common {
    private const val googleApiUrl = "https://maps.googleapis.com"
    private const val fcmURL = "https://fcm.googleapis.com/"
    @JvmStatic
    val googleAPI: IGoogleAPI
        get() = GoogleMapApiClient.getClient(googleApiUrl).create(IGoogleAPI::class.java)

    const val driversAvailable_tbl = "DriversAvailable"
    const val drivers_tbl = "Drivers"
    const val riders_tbl = "Riders"
    const val pickUpRequest_tbl = "PickUpRequests"
    const val requests_tbl = "Requests"
    const val token_tbl = "Tokens"
    @JvmField
    var lastRequestKey = ""
    //Price Values
    var base_fare = 2.55
    var time_rate = 0.35
    var distance_rate = 1.75
    var carsFee = 0.0
    var othersFee = 0.0

    fun getPrice(km: Double, min: Int): Double {
        return base_fare + time_rate * min + distance_rate * km - carsFee + othersFee
    }

    @JvmStatic
    val fCMService: IFCMService
        get() = FCMClient.getClient(fcmURL).create(IFCMService::class.java)

    @JvmStatic
    val iGoogleAPI: IGoogleAPI
        get() = GoogleMapApiClient.getClient(googleApiUrl).create(IGoogleAPI::class.java)
}