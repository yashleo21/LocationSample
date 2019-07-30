package com.emre1s.firstktapp

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import java.io.IOException
import java.util.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class FetchAddressIntentService : IntentService(FetchAddressIntentService::class.simpleName) {


  private var bus: RxBus = RxBus
  private lateinit var receiver: ResultReceiver

  override fun onHandleIntent(intent: Intent?) {
    intent ?: return

    val geocoder = Geocoder(this, Locale.getDefault())
    receiver = intent.getParcelableExtra(Constants.RECEIVER)
    var errorMessage = ""

    var location = intent.getParcelableExtra<Location>(Constants.LOCATION_DATA_EXTRA)
    Log.d("Emre1s", "SERVICE LAT: ${location.latitude} SERVICE LONG: ${location.longitude}")
    var addresses: List<Address> = emptyList()

    try {
      addresses = geocoder.getFromLocation(
              location.latitude,
              location.longitude,
              1
      )
    } catch (ioException: IOException) {
      errorMessage = "Service not available"
      Log.d("Emre1s", "IOE")
    } catch (illegalArguementsException: IllegalArgumentException) {
      errorMessage = "Invalid latitude and longitude"
      Log.d("Emre1s", "LATLNG")
    }

    if (addresses.isEmpty()) {
      errorMessage = "No address found"
      Log.d("Emre1s", "NOADDR")
      deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage)
    } else {
      val address = addresses[0]
      val addressFragments = with(address) {
        (0..maxAddressLineIndex).map { getAddressLine(it) }
      }
      Log.d("Emre1s", addressFragments.joinToString(separator = "\n"))
      deliverResultToReceiver(Constants.SUCCESS_RESULT,
              addressFragments.joinToString(separator = "\n"))
    }
  }

  private fun deliverResultToReceiver(resultCode: Int, message: String) {
    val bundle = Bundle().apply { putString(Constants.RESULT_DATA_KEY, message) }
    //bus.publish(EventOne(resultCode = resultCode, bundle = bundle))
    receiver.send(resultCode, bundle)
  }

}

object Constants {
  const val SUCCESS_RESULT = 0
  const val FAILURE_RESULT = 1
  const val PACKAGE_NAME = "com.emre1s.firstktapp"
  const val RECEIVER = "$PACKAGE_NAME.RECEIVER"
  const val RESULT_DATA_KEY = "${PACKAGE_NAME}.RESULT_DATA_KEY"
  const val LOCATION_DATA_EXTRA = "${PACKAGE_NAME}.LOCATION_DATA_EXTRA"
}