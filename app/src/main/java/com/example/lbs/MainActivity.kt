package com.example.lbs

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lbs.ui.theme.LBSTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            0
        )
        val mySensors = MySensors(this)
        mySensors.accelerometerSensor.startListening()
        mySensors.gyroscopeSensor.startListening()
        mySensors.magnetometerSensor.startListening()
        mySensors.locationUtils.startListening()
        setContent {
            LBSTheme {
                var sensorsData by remember {
                    mutableStateOf(SensorsData())
                }
                var sensorsDataTime by remember {
                    mutableStateOf(SensorsDataTime())
                }
                var sensorsDataDate by remember {
                    mutableStateOf(SensorsDataDate())
                }
                val csvData by remember {
                    mutableStateOf(StringBuilder("time,fx,fy,fz,gx,gy,gz,mx,my,mz,lat,lon,height\n"))
                }
                var isPauseSensors by remember {
                    mutableStateOf(true)
                }
                mySensors.accelerometerSensor.setOnSensorValuesChangedListener(
                    listener = { values, time ->
                        sensorsData = sensorsData.copy(
                            accelerometerData = values
                        )
                        sensorsDataTime = sensorsDataTime.copy(
                            accelerometerDataTime = System.currentTimeMillis() + (time - SystemClock.elapsedRealtimeNanos()) / 1000000
                        )
                        sensorsDataDate = sensorsDataDate.copy(
                            accelerometerDataDate = timeFormat(sensorsDataTime.accelerometerDataTime)
                        )
                    }
                )
                mySensors.gyroscopeSensor.setOnSensorValuesChangedListener(
                    listener = { values, time ->
                        sensorsData = sensorsData.copy(
                            gyroscopeData = values
                        )
                        sensorsDataTime = sensorsDataTime.copy(
                            gyroscopeDataTime = System.currentTimeMillis() + (time - SystemClock.elapsedRealtimeNanos()) / 1000000
                        )
                        sensorsDataDate = sensorsDataDate.copy(
                            gyroscopeDataDate = timeFormat(sensorsDataTime.accelerometerDataTime)
                        )
                        csvData.append("${sensorsDataDate.gyroscopeDataDate},")
                        for (i in 0..2) csvData.append(String.format("%.4f,", sensorsData.accelerometerData[i]))
                        for (i in 0..2) csvData.append(String.format("%.4f,", sensorsData.gyroscopeData[i]))
                        for (i in 0..2) csvData.append(String.format("%.4f,", sensorsData.magnetometerData[i]))
                        for (i in 0..1) csvData.append(String.format("%.9f,", sensorsData.locationData[i]))
                        csvData.append(String.format("%.3f\n", sensorsData.locationData[2]))
                    }
                )
                mySensors.magnetometerSensor.setOnSensorValuesChangedListener(
                    listener = { values, time ->
                        sensorsData = sensorsData.copy(
                            magnetometerData = values
                        )
                        sensorsDataTime = sensorsDataTime.copy(
                            magnetometerDataTime = System.currentTimeMillis() + (time - SystemClock.elapsedRealtimeNanos()) / 1000000
                        )
                        sensorsDataDate = sensorsDataDate.copy(
                            magnetometerDataDate = timeFormat(sensorsDataTime.accelerometerDataTime)
                        )
                    }
                )
                mySensors.locationUtils.setOnLocationValuesChangedListener(
                    listener = { values, time ->
                        sensorsData = sensorsData.copy(
                            locationData = values
                        )
                        sensorsDataTime = sensorsDataTime.copy(
                            locationDataTime = System.currentTimeMillis() + (time - SystemClock.elapsedRealtimeNanos()) / 1000000
                        )
                        sensorsDataDate = sensorsDataDate.copy(
                            locationDataDate = timeFormat(sensorsDataTime.accelerometerDataTime)
                        )
                    }
                )
                val mainNavController = rememberNavController()
                androidx.compose.material.Scaffold(
                    bottomBar = {
                        BottomNavigationBar(
                            items = listOf(
                                BottomNavItem(
                                    name = "Sensors",
                                    route = "sensors",
                                    icon = Icons.Default.Sensors
                                ),
                                BottomNavItem(
                                    name = "Map",
                                    route = "map",
                                    icon = Icons.Default.Map
                                )
                            ),
                            navController = mainNavController,
                            onItemClick = {
                                mainNavController.navigate(it.route)
                            }
                        )
                    }
                ) {
                    MainNavigation(
                        context = this,
                        navController = mainNavController,
                        mySensors = mySensors,
                        sensorsData = sensorsData,
                        sensorsDataDate = sensorsDataDate,
                        sensorsDataTime = sensorsDataTime,
                        csvData = csvData,
                        isPauseSensors = isPauseSensors,
                        onPauseClick = {
                            isPauseSensors = !isPauseSensors
                        }
                    )
                }

                //val (sensorsData, sensorsDataTime) = sensorScreen(mySensors = mySensors)
            }
        }
    }
}

@Composable
fun MainNavigation(
    context: Context,
    navController: NavHostController,
    mySensors: MySensors,
    sensorsData: SensorsData,
    sensorsDataDate: SensorsDataDate,
    sensorsDataTime: SensorsDataTime,
    csvData: StringBuilder,
    isPauseSensors: Boolean,
    onPauseClick: () -> Unit
) {
    NavHost(navController = navController, startDestination = "sensors") {
        composable("sensors") {
            SensorScreen(
                navController = navController,
                mySensors = mySensors,
                sensorsData = sensorsData,
                sensorsDataDate = sensorsDataDate,
                csvData = csvData,
                isPauseSensors = isPauseSensors,
                onPauseClick = onPauseClick
            )
        }
        composable("map") {
            val cameraPositionState = rememberCameraPositionState()
            getDeviceLocation(context = context, cameraPositionState = cameraPositionState)
            GoogleMap(
                modifier = Modifier.fillMaxHeight(0.9f),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {

            }
        }
        composable("chart") {
            SensorChartScreen(sensorsData = sensorsData, sensorsDataTime = sensorsDataTime)
        }
    }
}

@SuppressLint("MissingPermission")
private fun getDeviceLocation(
    context: Context,
    cameraPositionState: CameraPositionState
) {
    if (context.hasLocationPermission()) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener() {task ->
            if (task.isSuccessful) {
                val lastKnownLocation: Location? = task.result
                cameraPositionState.move(CameraUpdateFactory.newLatLng(
                    LatLng(lastKnownLocation!!.latitude, lastKnownLocation.longitude)
                ))
            }
        }

    }
}

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavItem) -> Unit
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    BottomNavigation(
        modifier = modifier,
        elevation = 15.dp,
        backgroundColor = MaterialTheme.colorScheme.primary
    ) {
        items.forEach { item ->
            val selected = item.route == backStackEntry.value?.destination?.route
            BottomNavigationItem(
                selected = selected,
                onClick = { onItemClick(item) },
                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                unselectedContentColor = Color.Gray,
                icon = {
                    Column(horizontalAlignment = CenterHorizontally) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.name
                        )
                        if (selected) {
                            Text(
                                text = item.name,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LBSTheme {

    }
}