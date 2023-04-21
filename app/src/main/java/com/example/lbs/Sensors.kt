package com.example.lbs

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor

class AccelerometerSensor(
    context: Context
): AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_ACCELEROMETER,
    sensorType = Sensor.TYPE_ACCELEROMETER
)

class GyroscopeSensor(
    context: Context
): AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_GYROSCOPE,
    sensorType = Sensor.TYPE_GYROSCOPE
)

class MagnetometerSensor(
    context: Context
): AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_COMPASS,
    sensorType = Sensor.TYPE_MAGNETIC_FIELD
)

data class MySensors(val context: Context) {
    val accelerometerSensor = AccelerometerSensor(context)
    val gyroscopeSensor = GyroscopeSensor(context)
    val magnetometerSensor = MagnetometerSensor(context)
    val locationUtils = LocationUtils(context)
}

data class SensorsData(
    val accelerometerData: List<Float> = List(3) { 0f },
    val gyroscopeData: List<Float> = List(3) { 0f },
    val magnetometerData: List<Float> = List(3) { 0f },
    val locationData: List<Double> = List(3) { 0.0 }
)

data class SensorsDataTime(
    val accelerometerDataTime: Long = 0,
    val gyroscopeDataTime: Long = 0,
    val magnetometerDataTime: Long = 0,
    val locationDataTime: Long = 0
)

data class SensorsDataDate(
    val accelerometerDataDate: String = "",
    val gyroscopeDataDate: String = "",
    val magnetometerDataDate: String = "",
    val locationDataDate: String = ""
)