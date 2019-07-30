package com.emre1s.firstktapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.LocationSettingsStates


class MainActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var myLocationListener: MyLocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Location Module"

        myLocationListener = MyLocationListener(this, lifecycle)
        myLocationListener.enable()
    }

    override fun onPause() {
        super.onPause()
        Log.d("Emre1s", "ON PAUSE CALLED")
        myLocationListener.stop()
    }

    override fun onResume() {
        super.onResume()
        Log.d("Emre1s", "ON RESUME CALLED")
       // myLocationListener.enable()
        myLocationListener.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.find_location -> myLocationListener.findLocation()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val states = LocationSettingsStates.fromIntent(intent)
        when (requestCode) {
            myLocationListener.REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.d("Emre1s", "RESULT_OK CALLED")
                        myLocationListener.startLocationUpdates()
                    }
                }
            }
            myLocationListener.LOCATION_PERMISSIONS_MISSING -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.d("Emre1s", "RESULT_OK from location permissions missing CALLED")
                        myLocationListener.startLocationUpdates()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }
}
