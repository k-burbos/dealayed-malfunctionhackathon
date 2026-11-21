package com.example.dealayed

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- Activity ----------
class NotificationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme { NotificationsScreen(onBack = { finish() }) }
        }
    }
}

// ---------- Colors ----------
private val Orange = Color(0xFFF04E2B)
private val Cream = Color(0xFFFFF1E0)
private val Purple = Color(0xFF2B1259)
private val Navy = Color(0xFF1E1B4B)
private val Mauve = Color(0xFFB58BB5)
private val Peach = Color(0xFFFFE0D6)
private val Sun = Color(0xFFFFD84D)
private val BodyGray = Color(0xFF8A7F96)

// ---------- Data ----------
private enum class NotifCategory { Missed, Expired, Roasts }
private enum class NotifTagStyle { Yellow, Orange, Cream }

private data class NotifTag(val text: String, val style: NotifTagStyle)

private data class NotifItem(
    val id: Int,
    val category: NotifCategory,
    val icon: ImageVector,
    val title: String,
    val time: String,
    val body: String,
    val unread: Boolean,
    val today: Boolean,
    val tags: List<NotifTag> = emptyList(),
)

private val initialNotifs = listOf(
    NotifItem(
        1, NotifCategory.Expired, Icons.Filled.HourglassBottom,
        "Deal expired. Shocker.", "2m",
        "\u201CBuy 1 Get 1 Milk Tea\u201D ended 4 minutes ago. You were literally looking at it.",
        unread = true, today = true,
        tags = listOf(NotifTag("-\u20B1130 saved by nobody", NotifTagStyle.Yellow)),
    ),
    NotifItem(
        2, NotifCategory.Roasts, Icons.Filled.SentimentDissatisfied,
        "Fashionably late again?", "18m",
        "Your membership says \u201CFashionably late.\u201D We say \u201Cconsistently.\u201D 12 missed deals and counting.",
        unread = true, today = true,
    ),
    NotifItem(
        3, NotifCategory.Missed, Icons.Filled.ShoppingCart,
        "Your cart misses you", "1h",
        "5 items. 2 days. Zero commitment. The price went up \u20B1200 \u2014 thanks for waiting.",
        unread = false, today = true,
        tags = listOf(
            NotifTag("Fine, checkout", NotifTagStyle.Orange),
            NotifTag("Ghost it", NotifTagStyle.Cream),
        ),
    ),
    NotifItem(
        4, NotifCategory.Roasts, Icons.Filled.DateRange,
        "Your Weekly Dealays is here", "Yesterday",
        "A recap of every deal you missed. It\u2019s a long one.",
        unread = false, today = false,
    ),
    NotifItem(
        5, NotifCategory.Expired, Icons.Filled.HourglassBottom,
        "Flash sale ended", "2d",
        "The 50% off flights sale ended while you were \u201Cthinking about it.\u201D",
        unread = false, today = false,
        tags = listOf(NotifTag("-\u20B12,400 saved by nobody", NotifTagStyle.Yellow)),
    ),
)

private val notifFilters = listOf("All", "Missed", "Expired", "Roasts")

// ---------- Screen ----------
@Composable
private fun NotificationsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableIntStateOf(0) }
    val notifs = remember { mutableStateListOf<NotifItem>().apply { addAll(initialNotifs) } }

    val unreadCount = notifs.count { it.unread }
    val visible = notifs.filter {
        when (selectedFilter) {
            1 -> it.category == NotifCategory.Missed
            2 -> it.category == NotifCategory.Expired
            3 -> it.category == NotifCategory.Roasts
            else -> true
        }
    }
    val today = visible.filter { it.today }
    val earlier = visible.filter { !it.today }

    Column(
        Modifier
            .fillMaxSize()
            .background(Orange)
    ) {
        NotifHeader(unreadCount, onBack)

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 56.dp, topEnd = 56.dp))
                .background(Cream)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Title row
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Notifications", color = Purple, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("We just wanted to remind you. Again.", color = BodyGray, fontSize = 11.sp)
                }
                Text(
                    "Mark all as ignored",
                    color = Orange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            for (i in notifs.indices) notifs[i] = notifs[i].copy(unread = false)
                        }
                        .padding(4.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                notifFilters.forEachIndexed { index, label ->
                    NotifFilterChip(label, selectedFilter == index) { selectedFilter = index }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Live shame banner
            LiveShameCard(
                onCry = { Toast.makeText(context, "Noted. Nobody cares.", Toast.LENGTH_SHORT).show() },
                onBlame = { Toast.makeText(context, "Traffic has been notified.", Toast.LENGTH_SHORT).show() },
            )

            if (today.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                NotifSectionLabel("TODAY")
                Spacer(Modifier.height(10.dp))
                today.forEachIndexed { i, item ->
                    NotifCard(item) {
                        val idx = notifs.indexOfFirst { it.id == item.id }
                        if (idx >= 0) notifs[idx] = notifs[idx].copy(unread = false)
                    }
                    if (i != today.lastIndex) Spacer(Modifier.height(12.dp))
                }
            }

            if (earlier.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                NotifSectionLabel("EARLIER")
                Spacer(Modifier.height(10.dp))
                earlier.forEachIndexed { i, item ->
                    NotifCard(item) {
                        val idx = notifs.indexOfFirst { it.id == item.id }
                        if (idx >= 0) notifs[idx] = notifs[idx].copy(unread = false)
                    }
                    if (i != earlier.lastIndex) Spacer(Modifier.height(12.dp))
                }
            }

            if (visible.isEmpty()) {
                Spacer(Modifier.height(40.dp))
                Text(
                    "Nothing here. Suspiciously quiet.",
                    color = BodyGray,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

// ---------- Header ----------
@Composable
private fun NotifHeader(unreadCount: Int, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            "DEALAYED",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            modifier = Modifier.weight(1f),
        )
        Box(Modifier.size(48.dp)) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Orange, modifier = Modifier.size(22.dp))
            }
            if (unreadCount > 0) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Navy),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(unreadCount.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------- Filter chip ----------
@Composable
private fun NotifFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(if (selected) Purple else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) Color.White else Purple,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ---------- Live shame banner ----------
@Composable
private fun LiveShameCard(onCry: () -> Unit, onBlame: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Purple)
            .drawBehind {
                drawCircle(
                    Color(0xFFB0304A).copy(alpha = 0.55f),
                    radius = size.width * 0.18f,
                    center = Offset(size.width * 0.93f, size.height * 0.02f),
                )
            }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = Sun, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "LIVE SHAME \u00B7 JUST NOW",
                color = Sun,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Someone with half your free time just grabbed the 70% off sneakers.",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "They paid \u20B11,299. You paid in regret.",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 11.sp,
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(Orange)
                    .clickable(onClick = onCry)
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text("Cry about it", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f))
                    .clickable(onClick = onBlame)
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text("Blame traffic", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------- Section label ----------
@Composable
private fun NotifSectionLabel(text: String) {
    Text(
        text,
        color = Mauve,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp),
    )
}

// ---------- Notification card ----------
@Composable
private fun NotifCard(item: NotifItem, onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape)
            .clip(shape)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Peach),
            contentAlignment = Alignment.Center,
        ) {
            Icon(item.icon, contentDescription = null, tint = Orange, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.title,
                    color = Purple,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(item.time, color = Mauve, fontSize = 10.sp)
                if (item.unread) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Orange)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(item.body, color = BodyGray, fontSize = 11.sp, lineHeight = 15.sp)
            if (item.tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.tags.forEach { NotifTagPill(it) }
                }
            }
        }
    }
}

@Composable
private fun NotifTagPill(tag: NotifTag) {
    val (bg, fg) = when (tag.style) {
        NotifTagStyle.Yellow -> Sun to Purple
        NotifTagStyle.Orange -> Orange to Color.White
        NotifTagStyle.Cream -> Cream to Purple
    }
    Box(
        Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(tag.text, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}