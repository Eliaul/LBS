package com.example.lbs

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SensorScreen(
    navController: NavController,
    mySensors: MySensors,
    sensorsData: SensorsData,
    sensorsDataDate: SensorsDataDate,
    csvData: StringBuilder,
    isPauseSensors: Boolean,
    onPauseClick: () -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    var detailedText by remember { mutableStateOf("") }
    var showImg by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var isExport by remember { mutableStateOf(false) }
    if (isPauseSensors) {
        mySensors.accelerometerSensor.stopListening()
        mySensors.gyroscopeSensor.stopListening()
        mySensors.magnetometerSensor.stopListening()
        mySensors.locationUtils.stopListening()
    } else {
        mySensors.accelerometerSensor.startListening()
        mySensors.gyroscopeSensor.startListening()
        mySensors.magnetometerSensor.startListening()
        mySensors.locationUtils.startListening()
    }


    Column {
        SensorCards(
            name = stringResource(R.string.accelerometer_name),
            sensorData = sensorsData.accelerometerData,
            sensorDataTime = sensorsDataDate.accelerometerDataDate,
            onClick = {
                navController.navigate("chart")
            },
            showDetailedInfo = {
                openDialog = true
                showImg = true
                detailedText = myGetString(context, R.string.accelerometer_info)
            }
        )
        SensorCards(
            name = stringResource(R.string.gyroscope_name),
            sensorData = sensorsData.gyroscopeData,
            sensorDataTime = sensorsDataDate.gyroscopeDataDate,
            onClick = {

            },
            showDetailedInfo = {
                openDialog = true
                showImg = true
                detailedText = myGetString(context, R.string.gyroscope_info)
            }
        )
        SensorCards(
            name = stringResource(R.string.magnetometer_name),
            sensorData = sensorsData.magnetometerData,
            sensorDataTime = sensorsDataDate.magnetometerDataDate,
            onClick = {

            },
            showDetailedInfo = {
                openDialog = true
                showImg = true
                detailedText = myGetString(context, R.string.magnetometer_info)
            }
        )
        LocationCards(
            locationData = sensorsData.locationData,
            locationDataTime = sensorsDataDate.locationDataDate,
            onClick = {

            },
            showDetailedInfo = {
                openDialog = true
                showImg = false
                detailedText = myGetString(context, R.string.location_info)
            }
        )
    }
    PauseAndExportSensor(
        isPause = isPauseSensors,
        onPauseClick = onPauseClick,
        onExportClick = {
            isExport = true
        }
    )
    DetailedDialog(
        openDialog = openDialog,
        detailInfo = detailedText,
        showImg = showImg,
        onOpenDialog = {
            openDialog = false
        }
    )

    if(isExport) {
        exportData(context, csvData.toString())
        csvData.clear()
        val path = Environment.getExternalStorageDirectory().path + "/Documents/sensor_data.csv"
        val fileUri: Uri? = try { FileProvider.getUriForFile(
            context,
            "com.example.lbs.provider",
            File(path)
        ) } catch (e: IllegalArgumentException) {
            null
        }
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "*/*"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
        isExport = false
    }
}

fun myGetString(context: Context, id: Int) = context.resources.getString(id)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DetailedDialog(
    openDialog: Boolean,
    detailInfo: String,
    showImg: Boolean,
    onOpenDialog: () -> Unit
) {
    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                onOpenDialog()
            },
            title = {
                Text(
                    modifier = Modifier.offset(y = 12.dp),
                    text = "详情",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.SansSerif
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .offset(y = 12.dp)
                        .wrapContentHeight()
                ) {
                    if (showImg) {
                        Text(text = "传感器的轴向如下图所示：")
                        Card(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(5)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sensor_axis),
                                contentDescription = "Axis",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Text(
                        text = detailInfo,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onOpenDialog()
                    }
                ) {
                    Text("确定")
                }
            },
            shape = RoundedCornerShape(5),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight()
        )
    }
}

@Composable
fun PauseAndExportSensor(
    isPause: Boolean,
    onPauseClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxHeight(0.9f)
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth(0.5f)) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onPauseClick()
                },
                shape = RoundedCornerShape(25),
                contentPadding = PaddingValues(15.dp)
            ) {
                Text(
                    text = if (isPause) "开始" else "暂停",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 20.sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onExportClick,
                shape = RoundedCornerShape(25),
                contentPadding = PaddingValues(15.dp)
            ) {
                Text(
                    text = "导出",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 20.sp
                    )
                )
            }
        }
    }
}

fun exportData(
    context: Context,
    csvData: String
) {
    ActivityCompat.requestPermissions(
        context as Activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 23
    )
    val folder: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val file = File(folder, "sensor_data.csv")
    writeTextData(file, csvData)
}

fun writeTextData(
    file: File,
    data: String
) {
    var fileOutputStream: FileOutputStream? = null
    try {
        fileOutputStream = FileOutputStream(file, true)
        fileOutputStream.write(data.toByteArray())
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        fileOutputStream?.close()
    }
}

fun timeFormat(
    milliTime: Long,
): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(milliTime + 3600000 * 8)
}
