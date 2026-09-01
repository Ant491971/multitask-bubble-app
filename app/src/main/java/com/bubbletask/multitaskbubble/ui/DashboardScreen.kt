package com.bubbletask.multitaskbubble.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bubbletask.multitaskbubble.service.BubbleService
import com.bubbletask.multitaskbubble.util.PermissionHelper

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val permissionHelper = remember { PermissionHelper(context) }
    var hasOverlayPermission by remember { mutableStateOf(permissionHelper.canDrawOverlays()) }
    var hasUsageStatsPermission by remember { mutableStateOf(permissionHelper.hasUsageStatsPermission()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Multitask Bubble Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        PermissionRow(label = "System Overlay", isGranted = hasOverlayPermission) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
            context.startActivity(intent)
        }

        PermissionRow(label = "Usage Stats", isGranted = hasUsageStatsPermission) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            context.startActivity(intent)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val intent = Intent(context, BubbleService::class.java)
                context.startService(intent)
            },
            enabled = hasOverlayPermission && hasUsageStatsPermission
        ) {
            Text("Start Bubble Service")
        }

        Button(
            onClick = {
                val intent = Intent(context, BubbleService::class.java)
                context.stopService(intent)
            }
        ) {
            Text("Stop Bubble Service")
        }
    }
}

@Composable
fun PermissionRow(label: String, isGranted: Boolean, onGrant: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        if (isGranted) {
            Text(text = "Granted", color = MaterialTheme.colorScheme.primary)
        } else {
            Button(onClick = onGrant) {
                Text("Grant")
            }
        }
    }
}