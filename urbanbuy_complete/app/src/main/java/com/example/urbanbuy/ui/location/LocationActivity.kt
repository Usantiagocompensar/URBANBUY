package com.example.urbanbuy.ui.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.urbanbuy.databinding.ActivityLocationBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class LocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationBinding
    private lateinit var map: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar OSM
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osm_prefs", MODE_PRIVATE)
        )

        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)

        checkLocationPermissions()

        // Zoom inicial
        map.controller.setZoom(16.0)
        map.controller.setCenter(GeoPoint(4.7110, -74.0721))

        // 🔵 Botón "Mi ubicación"
        val btnMyLocation: ImageButton = findViewById(com.example.urbanbuy.R.id.btnMyLocation)
        btnMyLocation.setOnClickListener {
            if (this::myLocationOverlay.isInitialized && myLocationOverlay.myLocation != null) {
                map.controller.animateTo(myLocationOverlay.myLocation)
                myLocationOverlay.enableFollowLocation()
            }
        }
    }

    private fun checkLocationPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        } else {
            enableMyLocation()
        }
    }

    private fun enableMyLocation() {
        myLocationOverlay = MyLocationNewOverlay(
            GpsMyLocationProvider(this),
            map
        )

        myLocationOverlay.enableMyLocation()
        myLocationOverlay.isDrawAccuracyEnabled = true
        myLocationOverlay.disableFollowLocation()

        map.overlays.add(myLocationOverlay)

        // Solo centrar una vez al iniciar
        myLocationOverlay.runOnFirstFix {
            runOnUiThread {
                map.controller.animateTo(myLocationOverlay.myLocation)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {

            enableMyLocation()
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}

