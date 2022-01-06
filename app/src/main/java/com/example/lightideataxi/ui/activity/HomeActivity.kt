package com.example.lightideataxi.ui.activity

import activityViewBinding
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.lightideataxi.R
import com.example.lightideataxi.adapter.CustomAutoCompleteAdapter
import com.example.lightideataxi.adapter.CustomerAdapter
import com.example.lightideataxi.model.CustomerModel
import com.example.lightideataxi.util.getUserSignedInData
import com.example.lightideataxi.util.isUserSignedIn
import com.example.lightideataxi.util.kodeinViewModel
import com.example.lightideataxi.util.showToast
import com.example.lightideataxi.viewmodel.HomeViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import java.util.*
import kotlin.collections.ArrayList
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.lightideataxi.common.GPS_REQUEST_CODE
import com.example.lightideataxi.common.LOCATION_PERMISSION_REQUEST_CODE
import com.example.lightideataxi.databinding.ActivityHomeBinding
import com.example.lightideataxi.util.SortPlaces
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import kotlinx.coroutines.tasks.await


class HomeActivity : AppCompatActivity(), DIAware {
    override val di by closestDI()
    private val binding by activityViewBinding(ActivityHomeBinding::inflate)
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val homeViewModel: HomeViewModel by kodeinViewModel()
    private var customerNames = ArrayList<String>()
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest

    // private lateinit var mapFragment: SupportMapFragment
    private val originalList = ArrayList<CustomerModel>()
    private val sortedList = ArrayList<CustomerModel>()
    private lateinit var customerAdapter: CustomerAdapter
    private lateinit var customAutoCompleteAdapter: CustomAutoCompleteAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var sort = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.include.tlbToolbar)
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        /*mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment*/

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        customerAdapter = CustomerAdapter(this)
        binding.rcvHome.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.rcvHome.adapter = customerAdapter
        handleHeaderView()
        settingDrawerToggle()
        handleHeaderView()
        observeCustomers()
        binding.include.imgSort.setOnClickListener {
            Log.d("helloworld", "click")
            requestPermission()

        }

        binding.include.ctvSearch.doAfterTextChanged {
            if (sort == 0) {
                handleFilterOriginal()
            } else if (sort == 1) {
                handleFilterSort()
            }
        }
    }

    private fun reArrangeData() {
        if (sort == 0) {
            sortedList.clear()
            observeSortCustomers()
            sort = 1
        } else if (sort == 1) {
            originalList.clear()
            observeCustomers()
            sort = 0
        }
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
            Log.d("helloworld", "not grant")

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            Log.d("helloworld", "grant")

            createLocationRequest()
        }
    }

    @SuppressLint("MissingPermission")
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
            lifecycleScope.launchWhenCreated {
                /*  val googleMap = mapFragment.awaitMap()
                  googleMap.awaitMapLoad()
                  googleMap.isMyLocationEnabled = true*/
                val location: Location? = fusedLocationClient.lastLocation.await()
                Log.d("helloworld", location.toString())
                if (location != null) {
                    lastLocation = location
                    reArrangeData()
                } else {
                    showToast("You need to get current location in Map first")
                }
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


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("helloworld", "onRequestPermission")

        when (requestCode) {

            LOCATION_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.

                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                ) {
                    Log.d("helloworld", "onRequestPermissionSuccess")
                    createLocationRequest()
                } else {
                    showToast("You need to grant location permission to use location access")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("helloworld", "onActivityResult")

        if (requestCode == GPS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("helloworld", "onActivityResultOK")

                lifecycleScope.launchWhenCreated {
                    val location: Location? = fusedLocationClient.lastLocation.await()
                    Log.d("helloworld", location.toString())
                    if (location != null) {
                        lastLocation = location
                        reArrangeData()
                    } else {
                        showToast("You need to get current location in Map first")
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                createLocationRequest()
            }
        }
    }

    private fun handleFilterOriginal() {
        if (binding.include.ctvSearch.text.isNotEmpty()) {
            if (originalList.isNotEmpty()) {
                val resultList = originalList.filter {
                    it.name.toLowerCase(Locale.getDefault())
                        .contains(
                            binding.include.ctvSearch.text.toString()
                                .toLowerCase(Locale.getDefault())
                        )
                }
                if (resultList.isNotEmpty()) {
                    binding.rcvHome.visibility = View.VISIBLE
                    binding.txtNoResults.visibility = View.GONE
                    customerAdapter.submitList(resultList)
                } else {
                    binding.rcvHome.visibility = View.GONE
                    binding.txtNoResults.visibility = View.VISIBLE
                }

                Log.d("resultasdf", resultList.toString())
            }
        } else {
            customerAdapter.submitList(originalList)
        }
    }

    private fun handleFilterSort() {
        if (binding.include.ctvSearch.text.isNotEmpty()) {

            if (sortedList.isNotEmpty()) {
                val resultList = sortedList.filter {
                    it.name.toLowerCase(Locale.getDefault())
                        .contains(
                            binding.include.ctvSearch.text.toString()
                                .toLowerCase(Locale.getDefault())
                        )
                }
                if (resultList.isNotEmpty()) {
                    binding.rcvHome.visibility = View.VISIBLE
                    binding.txtNoResults.visibility = View.GONE
                    customerAdapter.submitList(resultList)
                } else {
                    binding.rcvHome.visibility = View.GONE
                    binding.txtNoResults.visibility = View.VISIBLE
                }

                Log.d("resultasdf", resultList.toString())
            }
        } else {
            customerAdapter.submitList(sortedList)
        }
    }

    private fun observeCustomers() {
        homeViewModel.getCustomerListLiveData().observe(this, {
            Log.d("itasdf", it.toString())
            originalList.addAll(it)
            customerAdapter.submitList(originalList)
            customerNames.clear()
            originalList.forEach { cModel ->
                customerNames.add(cModel.name)
            }
            customAutoCompleteAdapter =
                CustomAutoCompleteAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    customerNames
                )
            binding.include.ctvSearch.setAdapter(customAutoCompleteAdapter)
        })
    }

    private fun observeSortCustomers() {
        homeViewModel.getCustomerListLiveData().observe(this, {
            sortedList.addAll(it)
            val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            //sort the list, give the Comparator the current location
            Collections.sort(sortedList, SortPlaces(currentLatLng))
            Log.d("itasdf", sortedList.toString())

            customerAdapter.submitList(sortedList)
            customerNames.clear()
            sortedList.forEach { cModel ->
                customerNames.add(cModel.name)
            }
            customAutoCompleteAdapter =
                CustomAutoCompleteAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    customerNames
                )
            binding.include.ctvSearch.setAdapter(customAutoCompleteAdapter)
        })
    }

    private fun handleHeaderView() {
        val headerView = binding.ngvHome.getHeaderView(0)
        if (this.isUserSignedIn()) {
            val account = this.getUserSignedInData()
            account?.let {
                it.photoUrl?.let { url ->
                    Glide.with(this).load(url)
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .into(headerView.findViewById(R.id.imgProfilePicture))
                }
                headerView.findViewById<TextView>(R.id.txtAccountName).text = it.givenName
                headerView.findViewById<TextView>(R.id.txtAccountGmail).text = it.email
            }
            headerView.findViewById<TextView>(R.id.txtLogout).setOnClickListener {
                signOut()
            }
        }
    }

    private fun settingDrawerToggle() {
        //setting the drawer toggle
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            toggle = ActionBarDrawerToggle(
                this, binding.drlHome, binding.include.tlbToolbar, R.string.open, R.string.close
            )

            binding.drlHome.addDrawerListener(toggle)
            toggle.syncState()
        }
    }

    fun signOut() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(
                this
            ) { showToast("Successfully Logged Out") }
        this.finish()
    }
}