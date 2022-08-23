package com.xiaoyun.golfholedemo

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.widget.SwipeDismissFrameLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolygonOptions
import com.xiaoyun.golfholedemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private var holeMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initGoogleMapLayout()
    }

    private fun initGoogleMapLayout() {
        // Users can exit the app by swiping from the far left of the screen to the right
        binding.mapContainer.addCallback(object : SwipeDismissFrameLayout.Callback() {
            override fun onDismissed(layout: SwipeDismissFrameLayout?) {
                onBackPressed()
            }
        })

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)

        binding.btnRefreshMap.setOnClickListener {
            refreshMap()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        holeMap = googleMap
        initMapSetting()
    }

    private fun initMapSetting() {
        holeMap?.apply {
            mapType = GoogleMap.MAP_TYPE_SATELLITE
            uiSettings.isZoomGesturesEnabled = false
            uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = false
            uiSettings.isScrollGesturesEnabled = false

            // Add a marker in map
            //googleMap.addMarker(MarkerOptions().position(golfHoleLocation))

            val hole = listOf(
                LatLng(-33.86114, 151.21490),
                LatLng(-33.86114, 151.21554),
                LatLng(-33.86254, 151.21534),
                LatLng(-33.86394, 151.21554),
                LatLng(-33.86394, 151.21490),
                LatLng(-33.86254, 151.21470),
                LatLng(-33.86114, 151.21490),
            )

            val areaBounds = LatLngBounds(
                LatLng(-33.88154, 151.21400),
                LatLng(-33.82154, 151.21700),
            )

            // set map area
            setLatLngBoundsForCameraTarget(areaBounds)

            refreshMap()

            addPolygon(
                PolygonOptions()
                    .add(
                        LatLng(-33.82154, 151.21400),
                        LatLng(-33.82154, 151.21700),
                        LatLng(-33.88154, 151.21700),
                        LatLng(-33.88154, 151.21400),
                        LatLng(-33.82154, 151.21400),
                    )
                    .addHole(hole)
                    .strokeJointType(JointType.ROUND)
                    .strokeWidth(2f)
                    .strokeColor(Color.WHITE)
                    .fillColor(Color.BLACK)
            )
        }
    }

    private fun refreshMap() {
        // hole center
        val golfHoleLocation = LatLng(-33.86254, 151.21522)

        // move the camera to the marker
        holeMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(golfHoleLocation, 16f))
    }
}