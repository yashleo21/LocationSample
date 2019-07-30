package com.emre1s.firstktapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.emre1s.firstktapp.databinding.ActivityMainBinding
import com.emre1s.firstktapp.room.LocationLog
import com.emre1s.firstktapp.room.LocationViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener

class MyLocationListener (
    private val context: Context,
    private val lifecycle: Lifecycle) {

    private var fusedLocationClient : FusedLocationProviderClient
    private var task: Task<LocationSettingsResponse>
    private var client: SettingsClient
    private var builder: LocationSettingsRequest.Builder

    private var locationCallback: LocationCallback
    private var locationRequest: LocationRequest?

    var allPermissionsGranted: Boolean = false
    val REQUEST_CHECK_SETTINGS = 2
    val LOCATION_PERMISSIONS_MISSING = 3

    private var lastKnownLocation: Location? = null

    private val bus = RxBus

    private var enabled = false
    private val locationViewModel: LocationViewModel

    private val binding: ActivityMainBinding
    private val addressLogAdapter: AddressLogAdapter


    init {
        binding = DataBindingUtil.setContentView(context as Activity, R.layout.activity_main)
        addressLogAdapter = AddressLogAdapter()
        binding.setVariable(BR.locationAdapter, addressLogAdapter)
        binding.rvLocationLog.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        locationViewModel = ViewModelProviders.of(context as MainActivity).get(LocationViewModel::class.java)
        locationViewModel.deleteAll()

        locationViewModel.allLocations.observe(context, Observer { locationLogList ->
            binding.locationAdapter?.locationLogList?.clear()
            locationLogList.forEach {
                binding.locationAdapter?.locationLogList?.add(it)
                binding.locationAdapter?.notifyDataSetChanged()
                Log.d("Emre1s locv", it.latitude)
            }
        })


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = createLocationRequest()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    binding.tvLatitude.text = location.latitude.toString()
                    binding.tvLongitude.text = location.longitude.toString()
                    lastKnownLocation = location
                    startIntentService()
                    Log.d("Emre1s NEW", "New latitude is : " +
                            "${location.latitude} and new longitude is ${location.longitude}")
                }
            }
        }

        builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)

        client = LocationServices.getSettingsClient(context)
        task = client.checkLocationSettings(builder.build())

        //checkPermissions(client, builder)

//        bus.listen(EventOne::class.java)
//            .subscribe {
//                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
//                    addressCallback(it)
//                }
//                Log.d("Emre1s", "Event one triggered ${it.resultCode}")}
    }

    fun start() {
        if (enabled) {
            task.addOnSuccessListener {
                startLocationUpdates() } //getCurrentLocation()
            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        Log.d("Emre1s", "This part of code called the REQUEST")
                        exception.startResolutionForResult(context as MainActivity, LOCATION_PERMISSIONS_MISSING)
                    } catch (sendEx: IntentSender.SendIntentException) {

                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null )
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun enable() {
        enabled = true
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Log.d("Emre1s", "Connect if not connected")
        }
    }

    fun stop() {
        enabled = false
        Log.d("Emre1s", "Disconnect")
        stopLocationUpdates()
    }

    private fun checkPermissions(
        client: SettingsClient,
        builder: LocationSettingsRequest.Builder
    ) {
        task = client.checkLocationSettings(builder.build())
        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    it.startResolutionForResult(context as Activity, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            }
        }

        Dexter.withActivity(context as MainActivity)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : BaseMultiplePermissionsListener() {

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                    super.onPermissionRationaleShouldBeShown(permissions, token)
                }
            }).check()
    }

    internal inner class AddressResultReceiver(handler: Handler): android.os.ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            val addressOutput = resultData?.getString(Constants.RESULT_DATA_KEY) ?: ""
            Log.d("Emre1s", addressOutput)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
               // addressCallback(addressOutput, lastKnownLocation ?: Location("abc"))
                locationViewModel.insert(LocationLog(0,lastKnownLocation?.latitude.toString(), lastKnownLocation?.longitude.toString(),
                    addressOutput))
            }
            //tv_address.text = addressOutput
        }
    }

    fun createLocationRequest(): LocationRequest? {

        return LocationRequest.create()?.apply {
            interval = 5000  //5s
            fastestInterval = 5000 //5s
            smallestDisplacement = 100f //100m
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

//    @Throws(SecurityException::class)
//    fun getCurrentLocation() {
//        if (allPermissionsGranted) {
//            fusedLocationClient!!.lastLocation
//                .addOnSuccessListener { location: Location? ->
////                    lastKnownLocation = location
////                    startIntentService()
//                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
//                        //callback(location ?: Location("abc"))
//                    }
//                    Log.d("Emre1s", location?.latitude.toString() + " Emre1s long : "
//                            + location?.longitude.toString())
//                }
//        } else {
//            checkPermissions(client = client, builder = builder)
//        }
//
//    }

    private fun startIntentService() {

        val intent = Intent(context, FetchAddressIntentService::class.java).apply {
            putExtra(Constants.RECEIVER, AddressResultReceiver(handler = Handler()))
            putExtra(Constants.LOCATION_DATA_EXTRA, lastKnownLocation)
        }
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            context.startService(intent)
        }
    }

    fun findLocation() {
        Toast.makeText(context, "Location updated!", Toast.LENGTH_SHORT).show()
        startLocationUpdates()
       // getCurrentLocation()
    }
}