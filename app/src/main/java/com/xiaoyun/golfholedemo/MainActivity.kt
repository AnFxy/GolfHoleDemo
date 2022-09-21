package com.xiaoyun.golfholedemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Path
import android.graphics.Point
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.applyCanvas
import androidx.wear.widget.SwipeDismissFrameLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.xiaoyun.golfholedemo.databinding.ActivityMainBinding
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private var holeMap: GoogleMap? = null
    private lateinit var holeSize: Pair<Int, Int>
    private lateinit var centerLatLng: LatLng
    private var westernmostLongitude: Double = 0.0
    private var easternmostLongitude: Double = 0.0
    private var northernmostLatitude: Double = 0.0
    private var southernmostLatitude: Double = 0.0
    private val staticDistance = 35
    private val extraPicDistance = 10

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
            // refreshMap()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        holeMap = googleMap
        initMapSetting()
    }

    private fun initMapSetting() {
        holeMap?.apply {
            mapType = GoogleMap.MAP_TYPE_SATELLITE

            val greenLatLng = LatLng(35.79038602826997, 139.712006695944256)
            val fairwayLatLng = LatLng(35.78968602826997, 139.712006695944256)
            val teeBoxLatLng = LatLng(35.79038602826997, 139.712806695944256)

            initGolfHoleWidthAndSize(
                greenLatLng = greenLatLng,
                fairwayLatLng = fairwayLatLng,
                teeBoxLatLng = teeBoxLatLng,
            )

            val newarkMap = GroundOverlayOptions()
                .image(
                    BitmapDescriptorFactory.fromBitmap(
                        createGolfHoleBitmap(
                            greenLatLng = greenLatLng,
                            fairwayLatLng = fairwayLatLng,
                            teeBoxLatLng = teeBoxLatLng
                        )
                    )
                )
                .position(centerLatLng, holeSize.first.toFloat(), holeSize.second.toFloat())

            addGroundOverlay(newarkMap)

            addPolygon(
                PolygonOptions().add(
                    LatLng(northernmostLatitude + 1, easternmostLongitude + 1),
                    LatLng(southernmostLatitude - 1, easternmostLongitude + 1),
                    LatLng(southernmostLatitude - 1, westernmostLongitude - 1),
                    LatLng(northernmostLatitude + 1, westernmostLongitude - 1)
                ).addHole(
                    listOf(
                        LatLng(
                            northernmostLatitude + CalculateUtils.getLatitudeDiffFromDistance(
                                staticDistance
                            ),
                            easternmostLongitude + CalculateUtils.getLongitudeDiffFromDistance(
                                northernmostLatitude,
                                staticDistance
                            )
                        ),
                        LatLng(
                            southernmostLatitude - CalculateUtils.getLatitudeDiffFromDistance(
                                staticDistance
                            ),
                            easternmostLongitude + CalculateUtils.getLongitudeDiffFromDistance(
                                southernmostLatitude,
                                staticDistance
                            )
                        ),
                        LatLng(
                            southernmostLatitude - CalculateUtils.getLatitudeDiffFromDistance(
                                staticDistance
                            ),
                            westernmostLongitude - CalculateUtils.getLongitudeDiffFromDistance(
                                southernmostLatitude,
                                staticDistance
                            )
                        ),
                        LatLng(
                            northernmostLatitude + CalculateUtils.getLatitudeDiffFromDistance(
                                staticDistance
                            ),
                            westernmostLongitude - CalculateUtils.getLongitudeDiffFromDistance(
                                northernmostLatitude,
                                staticDistance
                            )
                        )
                    )
                ).fillColor(Color.BLACK)
            )

            // add tee and green
            addMarker(
                MarkerOptions().position(teeBoxLatLng)
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
            addMarker(
                MarkerOptions().position(greenLatLng)
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
            addMarker(
                MarkerOptions().position(fairwayLatLng)
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
                    .add(teeBoxLatLng, fairwayLatLng, greenLatLng)
                    .color(Color.WHITE)
                    .width(2f)
            )

            refreshMap(greenLatLng, teeBoxLatLng)
        }
    }

    private fun refreshMap(greenLatLng: LatLng, teeBoxLatLng: LatLng) {
        // move the camera
        val bearingValue = CalculateUtils.rotateMap(
            greenLatLng.latitude,
            greenLatLng.longitude,
            teeBoxLatLng.latitude,
            teeBoxLatLng.longitude
        )
        val cameraPosition = CameraPosition.Builder().target(centerLatLng)
            .zoom(16f).bearing(bearingValue).tilt(0f).build()
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

    private fun createGolfHoleBitmap(
        teeBoxLatLng: LatLng,
        greenLatLng: LatLng,
        fairwayLatLng: LatLng
    ): Bitmap {
        val originalPicture =
            Bitmap.createBitmap(holeSize.first, holeSize.second, Bitmap.Config.ARGB_8888)

        originalPicture.applyCanvas {
            clipOutPath(
                getGolfHolePath(
                    greenLatLng = greenLatLng,
                    fairwayLatLng = fairwayLatLng,
                    teeBoxLatLng = teeBoxLatLng
                )
            )
            drawColor(Color.BLACK)
        }
        return originalPicture
    }

    private fun getGolfHolePath(
        teeBoxLatLng: LatLng,
        fairwayLatLng: LatLng,
        greenLatLng: LatLng
    ): Path {
        val greenPoint = Point(
            getBitmapPointXFromLatLng(greenLatLng),
            getBitmapPointYFromLatLng(greenLatLng)
        )

        val fairwayPoint = Point(
            getBitmapPointXFromLatLng(fairwayLatLng),
            getBitmapPointYFromLatLng(fairwayLatLng)
        )

        val teeBoxPoint = Point(
            getBitmapPointXFromLatLng(teeBoxLatLng),
            getBitmapPointYFromLatLng(teeBoxLatLng)
        )

        val path = Path()
        // Draw green point circle
        path.addCircle(
            greenPoint.x.toFloat(),
            greenPoint.y.toFloat(),
            staticDistance.toFloat(),
            Path.Direction.CCW
        )

        // Draw fairway point circle
        path.addCircle(
            fairwayPoint.x.toFloat(),
            fairwayPoint.y.toFloat(),
            staticDistance.toFloat(),
            Path.Direction.CCW
        )

        // Draw teeBox point circle
        path.addCircle(
            teeBoxPoint.x.toFloat(),
            teeBoxPoint.y.toFloat(),
            staticDistance.toFloat(),
            Path.Direction.CCW
        )

        // Fairway-Green rectangle
        val distanceFromFairwayToGreen = getDistanceFromPoints(fairwayPoint, greenPoint)
        val greenPrimaryPoint = Point(
            (greenPoint.x - ((greenPoint.y - fairwayPoint.y) / distanceFromFairwayToGreen) * staticDistance).toInt(),
            (greenPoint.y + ((greenPoint.x - fairwayPoint.x) / distanceFromFairwayToGreen) * staticDistance).toInt(),
        )
        val greenSecondaryPoint = Point(
            (greenPoint.x + ((greenPoint.y - fairwayPoint.y) / distanceFromFairwayToGreen) * staticDistance).toInt(),
            (greenPoint.y - ((greenPoint.x - fairwayPoint.x) / distanceFromFairwayToGreen) * staticDistance).toInt(),
        )
        val fairwayGreenPrimaryPoint = Point(
            (fairwayPoint.x - ((greenPoint.y - fairwayPoint.y) / distanceFromFairwayToGreen) * staticDistance).toInt(),
            (fairwayPoint.y + ((greenPoint.x - fairwayPoint.x) / distanceFromFairwayToGreen) * staticDistance).toInt(),
        )
        val fairwayGreenSecondaryPoint = Point(
            (fairwayPoint.x + ((greenPoint.y - fairwayPoint.y) / distanceFromFairwayToGreen) * staticDistance).toInt(),
            (fairwayPoint.y - ((greenPoint.x - fairwayPoint.x) / distanceFromFairwayToGreen) * staticDistance).toInt(),
        )

        path.moveTo(greenPrimaryPoint.x.toFloat(), greenPrimaryPoint.y.toFloat())
        path.lineTo(greenSecondaryPoint.x.toFloat(), greenSecondaryPoint.y.toFloat())
        path.lineTo(fairwayGreenSecondaryPoint.x.toFloat(), fairwayGreenSecondaryPoint.y.toFloat())
        path.lineTo(fairwayGreenPrimaryPoint.x.toFloat(), fairwayGreenPrimaryPoint.y.toFloat())

        // TeeBox-Fairway rectangle
        val distanceFromTeeBoxToFairway = getDistanceFromPoints(fairwayPoint, teeBoxPoint)
        val teeBoxPrimaryPoint = Point(
            (teeBoxPoint.x - ((teeBoxPoint.y - fairwayPoint.y) / distanceFromTeeBoxToFairway) * staticDistance).toInt(),
            (teeBoxPoint.y + ((teeBoxPoint.x - fairwayPoint.x) / distanceFromTeeBoxToFairway) * staticDistance).toInt(),
        )
        val teeBoxSecondaryPoint = Point(
            (teeBoxPoint.x + ((teeBoxPoint.y - fairwayPoint.y) / distanceFromTeeBoxToFairway) * staticDistance).toInt(),
            (teeBoxPoint.y - ((teeBoxPoint.x - fairwayPoint.x) / distanceFromTeeBoxToFairway) * staticDistance).toInt(),
        )
        val fairwayTeeBoxPrimaryPoint = Point(
            (fairwayPoint.x - ((teeBoxPoint.y - fairwayPoint.y) / distanceFromTeeBoxToFairway) * staticDistance).toInt(),
            (fairwayPoint.y + ((teeBoxPoint.x - fairwayPoint.x) / distanceFromTeeBoxToFairway) * staticDistance).toInt(),
        )
        val fairwayTeeBoxSecondaryPoint = Point(
            (fairwayPoint.x + ((teeBoxPoint.y - fairwayPoint.y) / distanceFromTeeBoxToFairway) * staticDistance).toInt(),
            (fairwayPoint.y - ((teeBoxPoint.x - fairwayPoint.x) / distanceFromTeeBoxToFairway) * staticDistance).toInt(),
        )

        path.moveTo(teeBoxPrimaryPoint.x.toFloat(), teeBoxPrimaryPoint.y.toFloat())
        path.lineTo(teeBoxSecondaryPoint.x.toFloat(), teeBoxSecondaryPoint.y.toFloat())
        path.lineTo(
            fairwayTeeBoxSecondaryPoint.x.toFloat(),
            fairwayTeeBoxSecondaryPoint.y.toFloat()
        )
        path.lineTo(fairwayTeeBoxPrimaryPoint.x.toFloat(), fairwayTeeBoxPrimaryPoint.y.toFloat())

        return path
    }

    private fun initGolfHoleWidthAndSize(
        teeBoxLatLng: LatLng,
        greenLatLng: LatLng,
        fairwayLatLng: LatLng
    ) {
        westernmostLongitude = teeBoxLatLng.longitude
            .coerceAtMost(greenLatLng.longitude)
            .coerceAtMost(fairwayLatLng.longitude)

        easternmostLongitude = teeBoxLatLng.longitude
            .coerceAtLeast(greenLatLng.longitude)
            .coerceAtLeast(fairwayLatLng.longitude)

        southernmostLatitude = teeBoxLatLng.latitude
            .coerceAtMost(greenLatLng.latitude)
            .coerceAtMost(fairwayLatLng.latitude)

        northernmostLatitude = teeBoxLatLng.latitude
            .coerceAtLeast(greenLatLng.latitude)
            .coerceAtLeast(fairwayLatLng.latitude)

        // Golf score hole width
        val golfHoleWidth = CalculateUtils.getDistanceFromLatitude(
            northernmostLatitude,
            westernmostLongitude,
            northernmostLatitude,
            easternmostLongitude
        ) + 2 * (staticDistance + extraPicDistance)

        // Golf score hole height
        val golfHoleHeight = CalculateUtils.getDistanceFromLatitude(
            northernmostLatitude,
            westernmostLongitude,
            southernmostLatitude,
            westernmostLongitude
        ) + 2 * (staticDistance + extraPicDistance)

        holeSize = Pair(golfHoleWidth.toInt(), golfHoleHeight.toInt())

        centerLatLng = LatLng(
            (northernmostLatitude + southernmostLatitude) / 2,
            (westernmostLongitude + easternmostLongitude) / 2
        )
    }

    private fun getBitmapPointXFromLatLng(latLng: LatLng): Int {
        if (easternmostLongitude == westernmostLongitude) {
            return staticDistance + extraPicDistance
        } else {
            return ((latLng.longitude - westernmostLongitude) / (easternmostLongitude - westernmostLongitude) *
                    (holeSize.first - 2 * (staticDistance + extraPicDistance)) +
                    (staticDistance + extraPicDistance)).toInt()
        }
    }

    private fun getBitmapPointYFromLatLng(latLng: LatLng): Int {
        if (northernmostLatitude == southernmostLatitude) {
            return staticDistance
        } else {
            return ((northernmostLatitude - latLng.latitude) / (northernmostLatitude - southernmostLatitude) *
                    (holeSize.second - 2 * (staticDistance + extraPicDistance)) + (staticDistance + extraPicDistance)).toInt()
        }
    }

    private fun getDistanceFromPoints(pointA: Point, pointB: Point) =
        sqrt((pointB.x - pointA.x).toDouble().pow(2.0) + (pointB.y - pointA.y).toDouble().pow(2.0))
}