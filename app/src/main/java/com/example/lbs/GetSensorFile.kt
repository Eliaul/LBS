package com.example.lbs

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorCards(
    name: String,
    sensorData: List<Float>,
    sensorDataTime: String,
    onClick: () -> Unit,
    showDetailedInfo: () -> Unit
) {
    val stringFormat: (List<Float>) -> String = { list ->
        val sb = StringBuilder("[")
        for (i in 0 until (list.size - 1) ) {
            sb.append(String.format("%8.4f, ", list[i]))
        }
        sb.append(String.format("%8.4f]", list.last()))
        sb.toString()
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
        onClick = onClick
    ) {
        SensorCardContent(
            name = name,
            data = sensorData,
            time = sensorDataTime,
            stringFormat = stringFormat,
            showDetailedInfo = showDetailedInfo
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationCards(
    locationData: List<Double>,
    locationDataTime: String,
    onClick: () -> Unit,
    showDetailedInfo: () -> Unit
) {
    val stringFormat: (List<Double>) -> String = { list ->
        val sb = StringBuilder("[")
        for (i in 0 until (list.size - 1) ) {
            sb.append(String.format("%13.9f, ", list[i]))
        }
        sb.append(String.format("%7.3f]", list.last()))
        sb.toString()
    }

    //locationUtils.setOnLocationValuesChangedListener(onLocationValuesChanged)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
        onClick = onClick
    ) {
        SensorCardContent(
            name = stringResource(R.string.location_name),
            data = locationData,
            time =  locationDataTime,
            stringFormat = stringFormat,
            showDetailedInfo = showDetailedInfo
        )
    }
}

@Composable
fun <T> SensorCardContent(
    name: String,
    data: List<T>,
    time: String,
    stringFormat: (List<T>) -> String,
    showDetailedInfo: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .padding(12.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            IconButton(
                onClick = showDetailedInfo
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Details"
                )
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) {
                        "Show"
                    } else {
                        "Hide"
                    }
                )
            }
        }
        if (expanded) {
            //Spacer(modifier = Modifier.height(12.dp))
            Row {
                Text(
                    text = "data:${stringFormat(data)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .padding(horizontal = 12.dp)
                )
            }
            Row {
                Text(
                    text = "time: $time",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .padding(horizontal = 12.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}