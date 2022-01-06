package com.example.lightideataxi.util

import com.example.lightideataxi.model.CustomerModel
import com.google.android.gms.maps.model.LatLng

class SortPlaces(current: LatLng) : Comparator<CustomerModel> {
    var currentLoc: LatLng
    override fun compare(place1: CustomerModel, place2: CustomerModel): Int {
        val lat1: Double = place1.lat.toDouble()
        val lon1: Double = place1.log.toDouble()
        val lat2: Double = place2.lat.toDouble()
        val lon2: Double = place2.log.toDouble()
        val distanceToPlace1 = distance(currentLoc.latitude, currentLoc.longitude, lat1, lon1)
        val distanceToPlace2 = distance(currentLoc.latitude, currentLoc.longitude, lat2, lon2)
        return (distanceToPlace1 - distanceToPlace2).toInt()
    }

    fun distance(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double): Double {
        val radius = 6378137.0 // approximate Earth radius, *in meters*
        val deltaLat = toLat - fromLat
        val deltaLon = toLon - fromLon
        val angle = 2 * Math.asin(
            Math.sqrt(
                Math.pow(Math.sin(deltaLat / 2), 2.0) +
                        Math.cos(fromLat) * Math.cos(toLat) *
                        Math.pow(Math.sin(deltaLon / 2), 2.0)
            )
        )
        return radius * angle
    }

    init {
        currentLoc = current
    }
}