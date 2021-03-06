package com.example.mobilneprojekt

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Base64.DEFAULT
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.mobilneprojekt.services.NewCarDTO
import com.example.mobilneprojekt.services.ServiceBuilder
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

const val UPDATE_INTERVAL = (10 * 1000).toLong()
const val FASTEST_INTERVAL: Long = 10000

class AddCarActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private var imageBitmap: Bitmap? = null
    private lateinit var token: String

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationManager: LocationManager? = null
    private var mLocationRequest: LocationRequest? = null
    private var locationManager: LocationManager? = null

    private var lat: Double? = null
    private var lng: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_car)

        imageBitmap = intent.extras?.get("data") as Bitmap?
        token = intent.getStringExtra("token")
        findViewById<ImageView>(R.id.carImage).apply {
            if (imageBitmap != null) {
                setImageBitmap(imageBitmap)
            }
        }

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkLocation()

    }

    fun onAddCar(view: View) {
        Log.v("click", "onAddCar pressed, id: ${view.id}")
        val name = findViewById<EditText>(R.id.editText).text.toString()
        val year = findViewById<EditText>(R.id.editText2).text.toString().toInt()
        val dmc = findViewById<EditText>(R.id.editText3).text.toString().toInt()
        val seats = findViewById<EditText>(R.id.editText4).text.toString().toInt()
        val mileage = findViewById<EditText>(R.id.editText5).text.toString().toInt()
        val price = findViewById<EditText>(R.id.editText6).text.toString().toFloat()
        val sec = findViewById<EditText>(R.id.editText7).text.toString().toFloat()

        val stream = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val b64Image = Base64.encodeToString(stream.toByteArray(), DEFAULT)

        val car = NewCarDTO(
            name,
            year,
            dmc,
            seats,
            mileage,
            b64Image,
            price,
            sec,
            if (lat != null) lat!! else 0.0,
            if (lng != null) lng!! else 0.0
        )
        val call = ServiceBuilder.getRentalService().addCar(token, car)

        val context = this
        call.enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("rest", "Failed to add car")
                Toast.makeText(context, "Failed to add car", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK, Intent().apply { putExtra("data", false) })
                finish()
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.v("rest", "Added car")
                setResult(Activity.RESULT_OK, Intent().apply { putExtra("data", true) })
                Toast.makeText(context, "Added car", Toast.LENGTH_SHORT).show()
                finish()
            }
        })

    }

    private val isLocationEnabled: Boolean
        get() {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }

    @Suppress("DEPRECATION")
    override fun onConnected(p0: Bundle?) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        startLocationUpdates()

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

        if (mLocation == null) {
            startLocationUpdates()
        }
        if (mLocation != null) {

        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionSuspended(i: Int) {
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
    }

    override fun onStart() {
        super.onStart()
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.disconnect()
        }
    }

    @Suppress("DEPRECATION")
    private fun startLocationUpdates() {
        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_INTERVAL)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClient,
            mLocationRequest, this
        )
    }

    override fun onLocationChanged(location: Location) {
        lat = location.latitude
        lng = location.longitude
    }

    private fun checkLocation(): Boolean {
        if (!isLocationEnabled)
            showAlert()
        return isLocationEnabled
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
            .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
            .setPositiveButton("Location Settings") { _, _ ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }
            .setNegativeButton("Cancel") { _, _ -> }
        dialog.show()
    }

}