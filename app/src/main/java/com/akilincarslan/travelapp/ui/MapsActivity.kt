package com.akilincarslan.travelapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.akilincarslan.travelapp.BuildConfig
import com.akilincarslan.travelapp.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.akilincarslan.travelapp.databinding.ActivityMapsBinding
import com.akilincarslan.travelapp.utils.stylePolygon
import com.akilincarslan.travelapp.utils.stylePolyline
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import timber.log.Timber
import kotlin.properties.Delegates

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private var likelyPlaceNames: Array<String?> = arrayOfNulls(0)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLocationPermission()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // basicMarkers()

        updateLocationUI()
        getDeviceLocation()
    }

    private fun basicMarkers() {
        //        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        val polyline1 = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .add(
                    LatLng(-35.016, 143.321),
                    LatLng(-34.747, 145.592),
                    LatLng(-34.364, 147.891),
                    LatLng(-33.501, 150.217),
                    LatLng(-32.306, 149.248),
                    LatLng(-32.491, 147.309)
                )
        )
        polyline1.tag = "Polyline1"
        stylePolyline(polyline1)

        val polygon1 = mMap.addPolygon(
            PolygonOptions()
                .clickable(true)
                .add(
                    LatLng(-27.457, 153.040),
                    LatLng(-33.852, 151.211),
                    LatLng(-37.813, 144.962),
                    LatLng(-34.928, 138.599)
                )
        )
        polygon1.tag = "Polygon1"
        stylePolygon(polygon1)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.684, 133.903), 4f))

        mMap.setOnPolylineClickListener { polyline ->
            // Flip from solid stroke to dotted stroke pattern.
        }
        mMap.setOnPolygonClickListener {

        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                Log.d("MapActivity", "Granted")
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            }
        } catch (e: SecurityException) {

        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task->
                if (task.isSuccessful) {
                lastKnownLocation = task.result
                    lastKnownLocation?.let {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastKnownLocation!!.latitude,
                            lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()
                        ))
                    }
                }
                }
            }
        } catch (e: SecurityException) {

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.current_place_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.get_place -> {
                showCurrentPlace()
                Timber.d("Get place clicked")
            }
        }
        return super.onOptionsItemSelected(item)
    }
    @SuppressLint("MissingPermission")
    private fun showCurrentPlace() {
        if (locationPermissionGranted) {
            val placeFields = listOf(Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG)
            val request = FindCurrentPlaceRequest.newInstance(placeFields)
            val placeResult = placesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener { task->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlaces = task.result
                    val count = if (likelyPlaces !=null && likelyPlaces.placeLikelihoods.size <5) {
                        likelyPlaces.placeLikelihoods.size
                    } else {
                        5
                    }
                    var i=0
                    likelyPlaceNames = arrayOfNulls(count)
                    for (placeLikelihood in likelyPlaces?.placeLikelihoods ?: emptyList()) {
                        Timber.d("Name is ${placeLikelihood.place.name}")
                        likelyPlaceNames[i] = placeLikelihood.place.name
                        if (i> count-1) {
                            break
                        }
                    }
                    Timber.d("LikelyPlaces $likelyPlaceNames")

                } else {
                    Timber.e("Exeption ${task.exception}")
                }
            }
        }
    }

    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101
        private const val DEFAULT_ZOOM=5
    }

}