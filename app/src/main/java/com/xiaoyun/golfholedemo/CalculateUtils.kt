package com.xiaoyun.golfholedemo

import android.location.Location
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object CalculateUtils {

    private const val EARTH_RADIUS: Double = 6378.137

    private const val eps: Double = 0.0001
    private const val pi: Double = 3.141592

    private const val LATLNG_DIFF = 0.0000104014

    private fun rad(distance: Double): Double =
        distance * Math.PI / 180.0

    /**
     * 根据两点间经纬度坐标（double值），计算两点间距离，单位为米
     */
    fun getDistanceFromLatitude(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val radLat1 = rad(lat1)
        val radLat2 = rad(lat2)
        val a = radLat1 - radLat2
        val b = rad(lng1) - rad(lng2)
        var s = 2 * asin(
            sqrt(
                sin(a / 2).pow(2) + cos(radLat1) * cos(radLat2) * sin(b / 2).pow(2)
            )
        )
        s *= EARTH_RADIUS
        s = (s * 10000) / 10
        return s
    }

    fun getLatitudeDiffFromDistance(distance: Int) =
        distance * LATLNG_DIFF

    fun getLongitudeDiffFromDistance(latitude: Double, distance: Int) =
        distance * (LATLNG_DIFF / cos(rad(latitude)))

    fun rotateMap(
        greenLat: Double,
        greenLon: Double,
        teeboxLat: Double,
        teeboxLon: Double
    ): Float {
        val vector = Location("Vector")
        vector.latitude = greenLat - teeboxLat
        vector.longitude = -(greenLon - teeboxLon)
        var alpha: Double = pi / 2
        if (Math.abs(vector.latitude) > eps) alpha =
            Math.atan(Math.abs(vector.longitude / vector.latitude))
        alpha = if (vector.longitude >= 0) {
            if (vector.latitude >= 0) pi / 2 - alpha else -(pi / 2 - alpha)
        } else {
            if (vector.latitude >= 0) pi / 2 + alpha else -(pi / 2 + alpha)
        }
        return (alpha / pi * 180).toFloat()
    }
}