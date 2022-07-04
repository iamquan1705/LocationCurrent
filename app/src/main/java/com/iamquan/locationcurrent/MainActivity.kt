package com.iamquan.locationcurrent

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.audiofx.Equalizer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.iamquan.locationcurrent.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSTION_LOCATION = 100
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        getCurrentLocations()
    }

    @SuppressLint("SetTextI18n")
    private fun getCurrentLocations() {
        if (checkPermisstion()) {
            if (isLocationEnable()) {

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requessPermisstion()
                    return
                }
                locationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Toast.makeText(this, "Null", Toast.LENGTH_SHORT).show()
                    } else {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        var addresses = geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                        if (addresses.isNotEmpty()) {
                            val getAddresses = addresses[0]
                            val city = getAddresses.locality
                            val state = getAddresses.adminArea
                            val country = getAddresses.countryName
                            binding.tvCity.text = "$state , $city , $country"
                        }
                        binding.apply {
                            tvLatutide.text = convertLatitude(location.latitude)
                            tvLongtutide.text = convertLatitude(location.longitude)
                        }
                    }
                }

            } else {
                Toast.makeText(this, "Turn on locaiton", Toast.LENGTH_SHORT).show()
                var intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requessPermisstion()
        }
    }

    private fun isLocationEnable(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    }

    private fun requessPermisstion() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSTION_LOCATION
        )
    }


    private fun checkPermisstion(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSTION_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, " has permisstion", Toast.LENGTH_SHORT).show()
                getCurrentLocations()
            } else {
                Toast.makeText(this, " No permisstion", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun convertLatitude(latitude: Double): String {
        val builder = StringBuilder()
        val latitudeDegrees = Location.convert(Math.abs(latitude), Location.FORMAT_SECONDS)
        val latitudeSplit = latitudeDegrees.split(":").toTypedArray()
        builder.append(latitudeSplit[0])
        builder.append("°")
        builder.append(latitudeSplit[1])
        builder.append("'")
        builder.append(latitudeSplit[2])
        builder.append("\"")
        if (latitude < 0) {
            builder.append(" S ")
        } else {
            builder.append(" N ")
        }
        return builder.toString()
    }

}