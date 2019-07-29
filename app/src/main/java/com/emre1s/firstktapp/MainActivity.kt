package com.emre1s.firstktapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationSettingsStates
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var myLocationListener: MyLocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle("Location Module")

        val addressLogAdapter = AddressLogAdapter()
        val layoutManager = LinearLayoutManager(this)
//        layoutManager.stackFromEnd=true
        rv_location_log.layoutManager = layoutManager
        rv_location_log.adapter = addressLogAdapter
        val dividerItemDecoration= DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        rv_location_log.addItemDecoration(dividerItemDecoration)

        myLocationListener = MyLocationListener(this, lifecycle, { location ->
            tv_latitude.text = location.latitude.toString()
            tv_longitude.text = location.longitude.toString()
            //addressLogAdapter.locationLogList.add(LocationLog(location.latitude.toString(), location.longitude.toString(), ""))
            addressLogAdapter.notifyDataSetChanged()
            Log.d("Emre1s", "Update UI ${location.latitude}  ${location.longitude}")
        }, {str, loc ->
            Log.d("Emre1s", "addresscallback LAT ${loc.latitude} LONG: ${loc.longitude}")
            addressLogAdapter.locationLogList.add(LocationLog(loc.latitude.toString(), loc.longitude.toString(), str))
            addressLogAdapter.notifyDataSetChanged()})

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

//    fun updateUI(text: String) {
//        tv_address.text = text
//    }

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
        }
        super.onActivityResult(requestCode, resultCode, data)

    }
}
