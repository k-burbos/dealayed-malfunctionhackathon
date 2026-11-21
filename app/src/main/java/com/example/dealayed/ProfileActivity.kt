package com.example.dealayed

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.LocalActivity

// ---------- Activity ----------
class ProfileActivity : ComponentActivity() {
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val email = intent.getStringExtra(EXTRA_EMAIL).orEmpty()
        val username = email.substringBefore('@').ifBlank { "guest" }

        setContent {
            MaterialTheme {
                ProfileScreen(
                    username = username,
                    email = email,
                    onLogout = {
                        startActivity(
                            Intent(this, SignInActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        finish()
                    },
                )
            }
        }
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }
}

// ---------- Colors ----------
private val Orange = Color(0xFFF04E2B)
private val Cream = Color(0xFFFFF1E0)
private val Purple = Color(0xFF2B1259)
private val Mauve = Color(0xFFB58BB5)
private val Peach = Color(0xFFFFE0D6)
private val Sun = Color(0xFFFFD84D)
private val CardBorder = Color(0xFFE6DDE8)

// ---------- Data ----------
private data class MembershipTier(val name: String, val desc: String, val price: String)

private val membershipTiers = listOf(
    MembershipTier("Fashionably late", "Hear about details a day late.", "Free • Forever"),
    MembershipTier("Late", "Hear details after a month — maybe.", "₱49 / month"),
    MembershipTier("Absent", "Hear details a month after, or never.", "₱149 / month"),
)

// ---------- Root ----------
@Composable
private fun ProfileScreen(username: String, email: String, onLogout: () -> Unit) {
    Scaffold(
        containerColor = Orange,
        bottomBar = { ProfileBottomBar() }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ProfileHeader()
            // Cream sheet with rounded top corners; only this part scrolls
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(Cream)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileSection(username, email)
                Spacer(Modifier.height(16.dp))
                OverspentCard()
                Spacer(Modifier.height(24.dp))
                MembershipSection()
                Spacer(Modifier.height(24.dp))
                PreferencesSection()
                Spacer(Modifier.height(24.dp))
                LogOutButton(onLogout)
            }
        }
    }
}

// ---------- Header ----------
@Composable
private fun ProfileHeader() {
    val context = LocalContext.current

    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "DEALAYED",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            modifier = Modifier.weight(1f)
        )
        CircleIcon(Icons.Filled.Share, Color.White.copy(alpha = 0.22f), Color.White)
        Spacer(Modifier.width(10.dp))
        CircleIcon(Icons.Filled.Notifications, Color.White, Orange) {
            context.startActivity(Intent(context, NotificationsActivity::class.java))
        }
    }
}

@Composable
private fun CircleIcon(icon: ImageVector, bg: Color, tint: Color, onClick: () -> Unit = {}) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ---------- Profile ----------
@Composable
private fun ProfileSection(username: String, email: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Replace with Image(painterResource(R.drawable.avatar), ...) for the real photo
        Box(
            Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Sun.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text("@$username", color = Purple, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(email, color = Mauve, fontSize = 11.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            "Always one day behind the best deals",
            color = Orange,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

// ---------- Overspent card ----------
@Composable
private fun OverspentCard() {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Purple)
            .clip(RoundedCornerShape(24.dp))
            .background(Purple)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text("Total Amount Overspent:", color = Cream.copy(alpha = 0.75f), fontSize = 13.sp)
        Text("₱67,699", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { 1f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
        )
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
        Spacer(Modifier.height(10.dp))
        Text("12", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Missed Deals", color = Cream.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

// ---------- Membership ----------
@Composable
private fun MembershipSection() {
    var selected by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxWidth()) {
        SectionTitle("Membership", "Choose how late you'd like to be")
        Spacer(Modifier.height(12.dp))
        membershipTiers.forEachIndexed { index, tier ->
            TierCard(tier, selected == index) { selected = index }
            if (index != membershipTiers.lastIndex) Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Column {
        Text(title, color = Purple, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        if (subtitle != null) Text(subtitle, color = Purple.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

@Composable
private fun TierCard(tier: MembershipTier, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    val titleColor = if (isSelected) Color.White else Purple
    val bodyColor = if (isSelected) Color.White.copy(alpha = 0.85f) else Mauve

    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isSelected) Orange else Color.White)
            .then(if (!isSelected) Modifier.border(1.dp, CardBorder, shape) else Modifier)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Peach),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Star, null,
                tint = if (isSelected) Color.White else Orange,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tier.name, color = titleColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(tier.desc, color = bodyColor, fontSize = 11.sp)
            Text(tier.price, color = bodyColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
        Column(horizontalAlignment = Alignment.End) {
            if (isSelected) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Sun)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Current", color = Purple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, null, tint = Orange, modifier = Modifier.size(14.dp))
                }
            } else {
                Box(
                    Modifier
                        .size(22.dp)
                        .border(BorderStroke(1.5.dp, Mauve), CircleShape)
                )
            }
        }
    }
}

// ---------- Preferences ----------
@Composable
private fun PreferencesSection() {
    var expiryPopups by remember { mutableStateOf(true) }
    var weeklyDealays by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        SectionTitle("Preferences")
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(vertical = 6.dp)
        ) {
            PreferenceRow(
                Icons.Filled.Notifications, "Expiry pop-ups", "Ping me after a deal ends",
                trailing = { OrangeSwitch(expiryPopups) { expiryPopups = it } }
            )
            RowDivider()
            PreferenceRow(
                Icons.Filled.DateRange, "Weekly Dealays", "Recap of Deals you missed",
                trailing = { OrangeSwitch(weeklyDealays) { weeklyDealays = it } }
            )
            RowDivider()
            PreferenceRow(Icons.Filled.Favorite, "Favorite categories", "Foods, Movies, Flights")
            RowDivider()
            PreferenceRow(Icons.Filled.Info, "Help & support", "We'll get back to you… Eventually")
        }
    }
}

@Composable
private fun RowDivider() =
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Peach)

@Composable
private fun PreferenceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Peach),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Orange, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Purple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Mauve, fontSize = 10.sp)
        }
        trailing?.invoke()
    }


}


@Composable
private fun OrangeSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Orange,
            checkedBorderColor = Orange,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFBDBDBD),
            uncheckedBorderColor = Color(0xFFBDBDBD),
        )
    )
}

// ---------- Log out ----------
@Composable
private fun LogOutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, Orange),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = Orange)
    ) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Log out", fontWeight = FontWeight.SemiBold)
    }
}

// ---------- Bottom navigation (shared with Home and Search) ----------
@Composable
private fun ProfileBottomBar() {
    val activity = LocalActivity.current ?: return

    // The purple fills the area behind the gesture bar
    Box(
        Modifier
            .fillMaxWidth()
            .background(Purple)
            .navigationBarsPadding()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                LayoutInflater.from(ctx).inflate(R.layout.view_bottom_nav, null).also { nav ->
                    BottomNav.setup(activity, BottomNav.Tab.PROFILE, nav)
                }
            }
        )
    }
}