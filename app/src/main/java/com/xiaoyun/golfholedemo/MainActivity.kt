package com.xiaoyun.golfholedemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.widget.SwipeDismissFrameLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
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

            val hole = listOf(
                LatLng(35.78988602826997, 139.71110695944256),
                LatLng(35.79108602826997, 139.71110695944256),
                LatLng(35.79108602826997, 139.71500695944256),
                LatLng(35.78988602826997, 139.71500695944256),
                LatLng(35.78988602826997, 139.71110695944256),
            )

            val areaBounds = LatLngBounds(
                LatLng(35.78988602826997, 139.71110695944256),
                LatLng(35.79108602826997, 139.71500695944256),
            )

            // set map area
            setLatLngBoundsForCameraTarget(areaBounds)

            refreshMap()

            addPolygon(
                PolygonOptions()
                    .add(
                        LatLng(35.58988602826997, 139.51110695944256),
                        LatLng(35.99108602826997, 139.51110695944256),
                        LatLng(35.99108602826997, 139.91500695944256),
                        LatLng(35.58988602826997, 139.91500695944256),
                        LatLng(35.58988602826997, 139.51110695944256),
                    )
                    .addHole(hole)
                    .fillColor(Color.BLACK)
            )

            val newarkLatLng = LatLng(35.79048602826997, 139.71310695944256)

            val newarkMap = GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.hole_background))
                .position(newarkLatLng, 160f, 378f)
                .bearing(-90f)

            addGroundOverlay(newarkMap)

            // add tee and green
            val teePoint = LatLng(35.79068602826997, 139.71460695944256)
            addMarker(
                MarkerOptions().position(teePoint)
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            resizeMapIcons(
                                R.drawable.tee_box_default,
                                12
                            )
                        )
                    )
                    .anchor(0.5f, 0.5f)
            )

            // add tee and green
            val greenPoint = LatLng(35.79028602826997, 139.71190695944256)
            addMarker(
                MarkerOptions().position(greenPoint)
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            resizeMapIcons(
                                R.drawable.gree_default,
                                12
                            )
                        )
                    )
                    .anchor(0.5f, 0.5f)
            )

            // add aim point
            val aimPoint = LatLng(35.79028602826997, 139.71426695944256)
            addMarker(
                MarkerOptions().position(aimPoint)
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            resizeMapIcons(
                                R.drawable.aim_point_default,
                                24
                            )
                        )
                    )
                    .anchor(0.5f, 0.5f)
            )

            // add polyline - tee box to aim point
            addPolyline(
                PolylineOptions()
                    .add(teePoint, aimPoint, greenPoint)
                    .color(Color.WHITE)
                    .width(2f)
            )
        }
    }

    private fun refreshMap() {
        // hole center
        val golfHoleLocation = LatLng(35.79048602826997, 139.71310695944256)

        // move the camera
        val cameraPosition = CameraPosition.Builder().target(golfHoleLocation)
            .zoom(16f).bearing(-90f).tilt(0f).build()
        holeMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun resizeMapIcons(resId: Int, size: Int): Bitmap {
        val imageBitmap = BitmapFactory.decodeResource(resources, resId)
        return Bitmap.createScaledBitmap(
            imageBitmap,
            size,
            size,
            false
        )
    }
}