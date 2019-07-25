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
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var myLocationListener: MyLocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle("Location Module")

       // lifecycle.addObserver(ObserveMain())
        myLocationListener = MyLocationListener(this, lifecycle, { location ->
            tv_latitude.text = location.latitude.toString()
            tv_longitude.text = location.longitude.toString()
            Log.d("Emre1s", "Update UI ${location.latitude}  ${location.longitude}")
        }, {
            Log.d("Emre1s", "MAIN ACTIVITY: $it")
            updateUI(it)
        })
        myLocationListener.enable()
    }

    override fun onPause() {
        super.onPause()
        myLocationListener.stop()
    }

    override fun onResume() {
        super.onResume()
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

    fun updateUI(text: String) {
        tv_address.text = text
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val states = LocationSettingsStates.fromIntent(intent)
        when (requestCode) {
            myLocationListener.REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        myLocationListener.getCurrentLocation()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }
}
