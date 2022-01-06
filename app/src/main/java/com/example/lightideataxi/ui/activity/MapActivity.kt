package com.example.lightideataxi.ui.activity

import activityViewBinding
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View.inflate
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.lightideataxi.R
import com.example.lightideataxi.common.GPS_REQUEST_CODE
import com.example.lightideataxi.common.LOCATION_PERMISSION_REQUEST_CODE
import com.example.lightideataxi.databinding.ActivityMapBinding
import com.example.lightideataxi.model.CustomerModel
import com.example.lightideataxi.util.bitmapDescriptorFromVector
import com.example.lightideataxi.util.showToast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.addCircle
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import kotlinx.coroutines.tasks.await

class MapActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var lastLocation: Location
    var customerModel: CustomerModel? = null

    // 2
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    private val binding by activityViewBinding(ActivityMapBinding::inflate)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestPermission()
        customerModel = intent.getParcelableExtra("customerModel")

    }

    private fun requestPermission() {
        if ((ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            createLocationRequest()
        }
    }

    private fun addMarkers(googleMap: GoogleMap) {
        val myPlace = LatLng(customerModel!!.lat.toDouble(), customerModel!!.log.toDouble())
        googleMap.addMarker {
            position(myPlace)
            title(customerModel!!.name)
            icon(
                bitmapDescriptorFromVector(R.drawable.ic_user_pin)
            )
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 16F))
        googleMap.uiSettings.isZoomControlsEnabled = true

    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(googleMap: GoogleMap) {
        // 1
        val location: Location? = fusedLocationClient.lastLocation.await()
        googleMap.isMyLocationEnabled = true
        Log.d("helloworld", location.toString())
        if (location != null) {
            lastLocation = location
            Log.d("helloworld", lastLocation.toString())
            val currentLatLng = LatLng(location.latitude, location.longitude)
            googleMap.addMarker {
                position(currentLatLng)
                title("Home")
                icon(
                    bitmapDescriptorFromVector(R.drawable.ic_taxi)
                )
            }
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16F))

        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                ) {
                    lifecycleScope.launchWhenCreated {
                        val googleMap = mapFragment.awaitMap()
                        googleMap.awaitMapLoad()
                        addMarkers(googleMap)
                        getCurrentLocation(googleMap)
                    }
                } else {
                    showToast("You need to grant location permission to use location access")
                }
            }
        }
    }


    private fun createLocationRequest() {
        Log.d("helloworld", "createRequest")

        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            Log.d("helloworld", "createRequestSuccess")

            locationUpdateState = true
            lifecycleScope.launchWhenCreated {
                val googleMap = mapFragment.awaitMap()
                googleMap.awaitMapLoad()
                addMarkers(googleMap)
                getCurrentLocation(googleMap)
            }
        }
        task.addOnFailureListener { e ->
            Log.d("helloworld", "createRequestFail")

            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this,
                        GPS_REQUEST_CODE
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GPS_REQUEST_CODE) {
            Log.d("resultCode", resultCode.toString())
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                lifecycleScope.launchWhenCreated {
                    val googleMap = mapFragment.awaitMap()
                    googleMap.awaitMapLoad()
                    addMarkers(googleMap)
                    getCurrentLocation(googleMap)
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                createLocationRequest()
            }
        }
    }


}