package com.emre1s.firstktapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import java.lang.ClassCastException

class ObserveMain {

}
class MyLocationListener (
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val callback: (Location) -> Unit,
    private val addressCallback: (String) -> Unit
    ) {

    private var fusedLocationClient : FusedLocationProviderClient
    private var task: Task<LocationSettingsResponse>
    private var client: SettingsClient
    private var builder: LocationSettingsRequest.Builder

    var allPermissionsGranted: Boolean = false

    val REQUEST_CHECK_SETTINGS = 2

    private var lastKnownLocation: Location? = null

    private val bus = RxBus

    private var enabled = false

    init {
        val observer = ObserveMain()
//        while (fusedLocationClient == null) {
//            fusedLocationClient = observer.createFusedLocationClient(context)
//        }
       fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest: LocationRequest? = createLocationRequest()

        builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)

        client = LocationServices.getSettingsClient(context)
        task = client.checkLocationSettings(builder.build())

        checkPermissions(client, builder)

//        bus.listen(EventOne::class.java)
//            .subscribe {
//                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
//                    addressCallback(it)
//                }
//                Log.d("Emre1s", "Event one triggered ${it.resultCode}")}
    }

    fun start() {
        if (enabled) {
            task.addOnSuccessListener { getCurrentLocation() }
            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        exception.startResolutionForResult(context as MainActivity, REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {

                    }
                }
            }
        }
    }

    fun enable() {
        enabled = true
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Log.d("Emre1s", "Connect if not connected")
        }
    }

    fun stop() {
        Log.d("Emre1s", "Disconnect")
        //task.addOnSuccessListener {  }
        //task.addOnFailureListener {  }
    }

    private fun checkPermissions(
        client: SettingsClient,
        builder: LocationSettingsRequest.Builder
    ) {
//        var multiplePermissionsListener = DialogOnAnyDeniedMultiplePermissionsListener.Builder
//            .withContext(context)
//            .withTitle("Location/GPS Permission")
//            .withMessage("GPS permission is required to find your current location")
//            .withButtonText(android.R.string.ok)
//            .build()

        Dexter.withActivity(context as MainActivity)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : BaseMultiplePermissionsListener() {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    super.onPermissionsChecked(report)
                    if (report?.areAllPermissionsGranted() == true) {
                        allPermissionsGranted = true
                        task = client.checkLocationSettings(builder.build())
                        task.addOnSuccessListener { getCurrentLocation() }
                        task.addOnFailureListener { exception ->
                            if (exception is ResolvableApiException) {
                                try {
                                    exception.startResolutionForResult(context, REQUEST_CHECK_SETTINGS)
                                } catch (sendEx: IntentSender.SendIntentException) {

                                }
                            }
                        }
                    }
                }

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
                addressCallback(addressOutput)
            }
            //tv_address.text = addressOutput
        }
    }

    fun createLocationRequest(): LocationRequest? {

        return LocationRequest.create()?.apply {
            interval = 1000
            fastestInterval = 5000
            smallestDisplacement = 100f
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    @Throws(SecurityException::class)
    fun getCurrentLocation() {
        if (allPermissionsGranted) {
            fusedLocationClient!!.lastLocation
                .addOnSuccessListener { location: Location? ->
                    lastKnownLocation = location
//                    startIntentService()
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        callback(location ?: Location("abc"))
                    }
                    Log.d("Emre1s", location?.latitude.toString() + " Emre1s long : "
                            + location?.longitude.toString())
                }
        } else {
            checkPermissions(client = client, builder = builder)
        }

    }

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
        getCurrentLocation()
    }
}