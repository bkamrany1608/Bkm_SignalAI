package com.freechart.signalai.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freechart.signalai.data.AppDb
import com.freechart.signalai.service.ScanState
import com.freechart.signalai.service.ScannerService
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RadarView(scanning: Boolean, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "radar")
    val angle by t.animateFloat(0f, 360f, infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "a")
    val a = if (scanning) angle else 0f
    Canvas(modifier.size(200.dp)) {
        val c = center; val r = size.minDimension / 2
        for (k in 1..4) drawCircle(Gold.copy(alpha = 0.25f), r * k / 4, c, style = Stroke(2f))
        drawLine(Gold.copy(0.2f), Offset(c.x - r, c.y), Offset(c.x + r, c.y))
        drawLine(Gold.copy(0.2f), Offset(c.x, c.y - r), Offset(c.x, c.y + r))
        if (scanning) rotate(a, c) {
            drawArc(Brush.sweepGradient(listOf(Color.Transparent, Gold.copy(0.55f)), c), -60f, 60f, true,
                Offset(c.x - r, c.y - r), Size(r * 2, r * 2))
            drawLine(Gold, c, Offset(c.x + r, c.y), 4f)
        }
        drawCircle(if (scanning) Gold else Color.Gray, 8f, c)
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier, color: Color = Color.White) =
    Card(modifier, colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }

@Composable
fun DashboardScreen() {
    val ctx = LocalContext.current
    val dao = remember { AppDb.get(ctx).dao() }
    val running by ScanState.running.collectAsState()
    val scanning by ScanState.scanning.collectAsState()
    val last by ScanState.lastScan.collectAsState()
    val sigs by dao.signalCount().collectAsState(0)
    val open by dao.openTradeCount().collectAsState(0)
    val pnl by dao.totalPnl().collectAsState(0.0)
    val perm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ScannerService.start(ctx) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        RadarView(scanning)
        Text(when { scanning -> "در حال اسکن بازار..."; running -> "سرویس فعال | اسکن بعدی هر ۱۵ دقیقه"; else -> "سرویس متوقف است" },
            color = if (running) Gold else Color.Gray, fontWeight = FontWeight.Medium)
        if (last > 0) Text("آخرین اسکن: " + SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(last)), fontSize = 12.sp, color = Color.Gray)
        Button(onClick = {
            if (running) ScannerService.stop(ctx)
            else if (Build.VERSION.SDK_INT >= 33) perm.launch(Manifest.permission.POST_NOTIFICATIONS)
            else ScannerService.start(ctx)
        }, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(if (running) "توقف سرویس" else "شروع سرویس ۲۴/۷", fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("سیگنال‌ها", "$sigs", Modifier.weight(1f))
            StatCard("معاملات باز", "$open", Modifier.weight(1f))
        }
        StatCard("سود/زیان کل (مجازی)", "%.2f $".format(pnl), Modifier.fillMaxWidth(), if (pnl >= 0) Green else Red)
    }
}

@Composable
fun PlaceholderScreen(title: String, phase: String, items: List<String>) =
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gold)
                Text("پیاده‌سازی در $phase", fontSize = 12.sp, color = Color.Gray)
                items.forEach { Text("• $it") }
            }
        }
    }

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("cfg", Context.MODE_PRIVATE) }
    var risk by remember { mutableIntStateOf(prefs.getInt("risk", 2)) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("سطح مدیریت ریسک", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gold)
        listOf(1 to "محافظه‌کار", 2 to "متوازن", 3 to "تهاجمی").forEach { (v, n) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(risk == v, { risk = v; prefs.edit().putInt("risk", v).apply() }); Text(n)
            }
        }
        HorizontalDivider()
        Text("پایداری ۲۴/۷ روی شیائومی", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gold)
        Text("۱) بهینه‌سازی باتری را برای اپ غیرفعال کن\n۲) در تنظیمات اپ: Autostart روشن و Battery saver روی No restrictions\n۳) اپ را در Recent Apps قفل کن", fontSize = 13.sp)
        Button(onClick = {
            ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}")))
        }, modifier = Modifier.fillMaxWidth()) { Text("غیرفعال‌سازی بهینه‌سازی باتری") }
        OutlinedButton(onClick = {
            ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}")))
        }, modifier = Modifier.fillMaxWidth()) { Text("باز کردن تنظیمات اپ") }
    }
}
