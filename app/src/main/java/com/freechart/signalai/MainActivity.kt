package com.freechart.signalai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.freechart.signalai.ui.*
import kotlinx.coroutines.launch

data class Dest(val route: String, val title: String, val icon: ImageVector, val phase: String, val items: List<String>)

val dests = listOf(
    Dest("dash", "داشبورد", Icons.Filled.Home, "", emptyList()),
    Dest("signals", "سیگنال‌ها", Icons.Filled.Notifications, "مرحله ۲", listOf("لیست سیگنال با ورود/خروج/حد ضرر", "امتیاز تأیید (مثلاً ۶ از ۱۰) و احتمال درستی", "تصویر چارت و ارسال به تلگرام")),
    Dest("charts", "چارت‌ها", Icons.Filled.Info, "مرحله ۲", listOf("چارت کندلی با اندیکاتورها و استراتژی‌ها", "اندیکاتورهای تأیید ثانویه", "تحلیل هوش مصنوعی")),
    Dest("virtual", "معامله مجازی", Icons.Filled.PlayArrow, "مرحله ۳", listOf("حساب مجازی", "سه سطح ریسک، هرمی، ریکاوری", "تریلینگ، قفل سود، خروج اضطراری")),
    Dest("pump", "رادار پامپ", Icons.Filled.Search, "مرحله ۴", listOf("اسکن میم‌کوین و آلت‌کوین قبل از پامپ", "گزارش رشد هر سیگنال تا پایان پامپ", "فیلتر نقدینگی و ریسک قرارداد")),
    Dest("reports", "گزارش و ژورنال", Icons.Filled.DateRange, "مرحله ۴", listOf("نمایش بر اساس سود درصدی، دلاری و R", "نمودار هر پارامتر نسبت به تاریخ")),
    Dest("optimizer", "بهینه‌ساز خودکار", Icons.Filled.Build, "مرحله ۴", listOf("بهینه‌سازی روزانه (Walk-forward)", "محافظ برابر Overfitting")),
    Dest("telegram", "تلگرام", Icons.Filled.Send, "مرحله ۲", listOf("توکن بات و چت‌آیدی", "ارسال سیگنال و دریافت دستور")),
    Dest("broker", "اتصال بروکر", Icons.Filled.Lock, "مرحله ۴", listOf("کریپتو: Binance/Bybit", "فارکس و طلا: OANDA یا MetaApi", "سقف ضرر روزانه و کلید قطع اضطراری")),
    Dest("settings", "تنظیمات", Icons.Filled.Settings, "", emptyList()),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppTheme { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { AppRoot() } } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val cur = nav.currentBackStackEntryAsState().value?.destination?.route ?: "dash"
    ModalNavigationDrawer(drawerState = drawer, drawerContent = {
        ModalDrawerSheet {
            Text("سیگنال‌یار AI", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gold, modifier = Modifier.padding(24.dp))
            dests.forEach { d ->
                NavigationDrawerItem(label = { Text(d.title) }, icon = { Icon(d.icon, null) }, selected = cur == d.route,
                    modifier = Modifier.padding(horizontal = 12.dp), onClick = {
                        scope.launch { drawer.close() }
                        nav.navigate(d.route) { popUpTo("dash"); launchSingleTop = true }
                    })
            }
        }
    }) {
        Scaffold(topBar = {
            TopAppBar(title = { Text(dests.first { it.route == cur }.title) },
                navigationIcon = { IconButton({ scope.launch { drawer.open() } }) { Icon(Icons.Filled.Menu, "منو") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg, titleContentColor = Gold, navigationIconContentColor = Gold))
        }) { pad ->
            NavHost(nav, "dash", Modifier.padding(pad)) {
                composable("dash") { DashboardScreen() }
                composable("settings") { SettingsScreen() }
                dests.filter { it.items.isNotEmpty() }.forEach { d ->
                    composable(d.route) { PlaceholderScreen(d.title, d.phase, d.items) }
                }
            }
        }
    }
}
