package com.akilincarslan.travelapp.utils

import android.graphics.Color
import com.google.android.gms.maps.model.*

fun stylePolyline(polyline: Polyline) {
    val type = polyline.tag?.toString() ?: ""
    when(type) {
        "Polyline1" -> {
            polyline.startCap = RoundCap()
            polyline.endCap = SquareCap()
            polyline.color = Color.RED
            polyline.jointType = JointType.ROUND
        }
    }
}
fun stylePolygon(polygon: Polygon) {
    val type = polygon.tag?.toString() ?: ""
    when(type) {
        "Polygon1" -> {
            polygon.strokeColor= Color.GREEN

        }
    }
}