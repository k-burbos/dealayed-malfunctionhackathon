package com.example.dealayed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ---------- Colors ----------
private val Orange = Color(0xFFF04E2B)
private val Cream = Color(0xFFFFF1E0)
private val Purple = Color(0xFF2B1259)
private val Navy = Color(0xFF1E1B4B)
private val Mauve = Color(0xFFB58BB5)
private val Peach = Color(0xFFFFE0D6)
private val Sun = Color(0xFFFFD84D)
private val CardBorder = Color(0xFFE6DDE8)
private val ErrorRed = Color(0xFF8E0F0F)

// ---------- State ----------
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

data class LoginForm(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val passwordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
)

// ---------- ViewModel ----------
// Demo login: any valid email + password of 6+ characters works.
// The password "incorrect" is rejected so you can see the error state.
class AppViewModel : ViewModel() {

    private val _form = MutableStateFlow(LoginForm())
    val form: StateFlow<LoginForm> = _form.asStateFlow()

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    /** null = logged out, otherwise the signed-in email. */
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val dealSecondsLeft: StateFlow<Long> = flow {
        var left = DEAL_SECONDS
        while (true) {
            emit(left)
            delay(1_000)
            left = if (left > 0) left - 1 else DEAL_SECONDS
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DEAL_SECONDS)

    fun onEmailChange(v: String) = _form.update { it.copy(email = v, emailError = null) }
    fun onPasswordChange(v: String) = _form.update { it.copy(password = v, passwordError = null) }
    fun onTogglePassword() = _form.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun onRememberChange(v: Boolean) = _form.update { it.copy(rememberMe = v) }
    fun onComingSoon(what: String) { _messages.tryEmit("$what isn't available yet") }

    fun onErrorShown() {
        if (_loginState.value is LoginUiState.Error) _loginState.value = LoginUiState.Idle
    }

    fun onLoginClick() {
        if (_loginState.value is LoginUiState.Loading) return
        val f = _form.value
        val emailError = when {
            f.email.isBlank() -> "Enter your email"
            !EMAIL_REGEX.matches(f.email.trim()) -> "That email doesn't look right"
            else -> null
        }
        val passwordError = when {
            f.password.isEmpty() -> "Enter your password"
            f.password.length < 6 -> "Use at least 6 characters"
            else -> null
        }
        if (emailError != null || passwordError != null) {
            _form.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            delay(1_200) // pretend network call
            if (f.password == "incorrect") {
                _loginState.value = LoginUiState.Error("Wrong email or password. Try again.")
            } else {
                _loginState.value = LoginUiState.Success
                _userEmail.value = f.email.trim()
                _form.update { it.copy(password = "", passwordVisible = false) }
            }
        }
    }

    fun onLogout() {
        _userEmail.value = null
        _loginState.value = LoginUiState.Idle
    }

    private companion object {
        const val DEAL_SECONDS = 2 * 3600L + 11 * 60L + 32L
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

// ---------- Activity ----------
class SignInActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme { AppRoot(viewModel) }
        }
    }
}

// ---------- Root: shows Login or Profile ----------
@Composable
fun AppRoot(vm: AppViewModel) {
    val email by vm.userEmail.collectAsState()
    val form by vm.form.collectAsState()
    val loginState by vm.loginState.collectAsState()
    val secondsLeft by vm.dealSecondsLeft.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(loginState) {
        val s = loginState
        if (s is LoginUiState.Error) {
            snackbar.showSnackbar(s.message)
            vm.onErrorShown()
        }
    }
    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    Box(Modifier.fillMaxSize()) {
        val signedIn = email
        if (signedIn == null) {
            LoginScreen(
                form = form,
                isLoading = loginState is LoginUiState.Loading,
                secondsLeft = secondsLeft,
                onEmailChange = vm::onEmailChange,
                onPasswordChange = vm::onPasswordChange,
                onTogglePassword = vm::onTogglePassword,
                onRememberChange = vm::onRememberChange,
                onLoginClick = vm::onLoginClick,
                onForgotClick = { vm.onComingSoon("Password reset") },
                onSocialClick = { vm.onComingSoon("$it login") },
                onSignUpClick = { vm.onComingSoon("Sign up") },
            )
        } else {
            ProfileScreen(
                username = signedIn.substringBefore('@').ifBlank { "guest" },
                email = signedIn,
                onLogout = vm::onLogout,
            )
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(16.dp),
        )
    }
}

// =====================================================================
// LOGIN
// =====================================================================
private val HeaderHeight = 290.dp

@Composable
private fun LoginScreen(
    form: LoginForm,
    isLoading: Boolean,
    secondsLeft: Long,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onRememberChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onForgotClick: () -> Unit,
    onSocialClick: (String) -> Unit,
    onSignUpClick: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Cream)) {
        val sheetMinHeight = maxHeight - HeaderHeight
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            LoginHeader(secondsLeft, Modifier.fillMaxWidth().height(HeaderHeight))
            LoginSheet(
                form, isLoading,
                onEmailChange, onPasswordChange, onTogglePassword, onRememberChange,
                onLoginClick, onForgotClick, onSocialClick, onSignUpClick,
                Modifier.fillMaxWidth().heightIn(min = sheetMinHeight),
            )
        }
    }
}

@Composable
private fun LoginHeader(secondsLeft: Long, modifier: Modifier = Modifier) {
    Box(modifier) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-34).dp, y = 10.dp)
                .size(96.dp)
                .border(14.dp, Orange.copy(alpha = 0.12f), CircleShape)
        )
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = 18.dp, y = (-24).dp)
                .rotate(-12f)
                .clip(RoundedCornerShape(6.dp))
                .background(Navy)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text("-40%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        CountdownPill(
            secondsLeft,
            Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 8.dp, end = 12.dp),
        )

        Column(
            Modifier.align(Alignment.Center).statusBarsPadding().padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DealayedLogo()
            Spacer(Modifier.height(10.dp))
            Text("DEALAYED", color = Orange, fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(
                buildAnnotatedString {
                    append("Great deals, ")
                    withStyle(SpanStyle(color = Orange, fontWeight = FontWeight.SemiBold)) { append("right") }
                    append(" on time.")
                },
                color = Purple,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun CountdownPill(secondsLeft: Long, modifier: Modifier = Modifier) {
    val time = remember(secondsLeft) {
        "%02d:%02d:%02d".format(secondsLeft / 3600, (secondsLeft % 3600) / 60, secondsLeft % 60)
    }
    Row(
        modifier
            .shadow(6.dp, CircleShape, spotColor = Orange)
            .clip(CircleShape)
            .background(Color.White)
            .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(24.dp).clip(CircleShape).background(Orange), contentAlignment = Alignment.Center) {
            Text("⚡", fontSize = 12.sp)
        }
        Spacer(Modifier.width(6.dp))
        Text("Ends in ", color = Purple, fontSize = 10.sp)
        Text(time, color = Orange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DealayedLogo(modifier: Modifier = Modifier) {
    Canvas(modifier.size(72.dp).semantics { contentDescription = "Dealayed logo" }) {
        val s = size.minDimension
        val stroke = s * 0.11f
        drawCircle(Orange, radius = s * 0.2f, center = Offset(s * 0.3f, s * 0.3f), style = Stroke(stroke))
        drawCircle(Orange, radius = s * 0.2f, center = Offset(s * 0.72f, s * 0.72f))
        drawLine(
            Orange,
            start = Offset(s * 0.84f, s * 0.1f),
            end = Offset(s * 0.12f, s * 0.9f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun LoginSheet(
    form: LoginForm,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onRememberChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onForgotClick: () -> Unit,
    onSocialClick: (String) -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier
            .clip(RoundedCornerShape(topStart = 64.dp))
            .background(Orange)
            .drawBehind {
                drawCircle(
                    Color.White.copy(alpha = 0.08f),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.85f, size.height * 0.97f),
                )
            }
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp)
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
    ) {
        Text("Log in", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Text("Welcome back! Your deals are waiting.", color = Color.White.copy(alpha = 0.92f), fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        AuthTextField(
            label = "EMAIL",
            value = form.email,
            onValueChange = onEmailChange,
            placeholder = "you@example.com",
            leadingIcon = Icons.Filled.Email,
            error = form.emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        )
        Spacer(Modifier.height(14.dp))
        AuthTextField(
            label = "PASSWORD",
            value = form.password,
            onValueChange = onPasswordChange,
            placeholder = "Enter your password",
            leadingIcon = Icons.Filled.Lock,
            error = form.passwordError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onLoginClick()
            }),
            visualTransformation = if (form.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailing = {
                Text(
                    if (form.passwordVisible) "HIDE" else "SHOW",
                    color = Purple,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onTogglePassword)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                )
            },
        )

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onRememberChange(!form.rememberMe) }
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = form.rememberMe,
                    onCheckedChange = null,
                    modifier = Modifier.padding(end = 6.dp),
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.White,
                        checkmarkColor = Orange,
                        uncheckedColor = Color.White,
                    ),
                )
                Text("Remember me", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Text(
                "Forgot password?",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onForgotClick).padding(4.dp),
            )
        }

        Spacer(Modifier.height(16.dp))
        LoginButton(isLoading, onLoginClick)

        Spacer(Modifier.height(24.dp))
        Text("or continue with", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SocialButton("Facebook", { onSocialClick("Facebook") }, Modifier.weight(1f)) {
                Text("f", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            SocialButton("Google", { onSocialClick("Google") }, Modifier.weight(1f)) {
                Text("G", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            // Placeholder glyph: swap for the Apple logo from your design assets.
            SocialButton("Apple", { onSocialClick("Apple") }, Modifier.weight(1f)) {
                Icon(Icons.Filled.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(28.dp))
        Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account?", color = Color.White, fontSize = 12.sp)
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onSignUpClick)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text("Sign up", color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    error: String?,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(Modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = Navy, fontSize = 14.sp),
            cursorBrush = SolidColor(Orange),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(Cream)
                        .then(if (error != null) Modifier.border(2.dp, ErrorRed, shape) else Modifier)
                        .padding(start = 14.dp, end = if (trailing != null) 4.dp else 14.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(leadingIcon, contentDescription = null, tint = Orange, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(label, color = Purple.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Box(Modifier.heightIn(min = 22.dp), contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) Text(placeholder, color = Mauve, fontSize = 14.sp)
                            innerTextField()
                        }
                    }
                    trailing?.invoke()
                }
            },
        )
        if (error != null) {
            Text(
                error,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
            )
        }
    }
}

@Composable
private fun LoginButton(isLoading: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Button(
        onClick = onClick,
        enabled = !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Navy,
            contentColor = Color.White,
            disabledContainerColor = Navy.copy(alpha = 0.75f),
            disabledContentColor = Color.White,
        ),
        modifier = Modifier.fillMaxWidth().height(56.dp).shadow(12.dp, shape, spotColor = Navy),
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
        } else {
            Text("Log In", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.size(24.dp).clip(CircleShape).background(Orange), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun SocialButton(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .height(48.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.18f))
            .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Continue with $name" },
        contentAlignment = Alignment.Center,
    ) { content() }
}

// =====================================================================
// PROFILE
// =====================================================================
// ---------- Data ----------
private data class Tier(val name: String, val desc: String, val price: String)

private val tiers = listOf(
    Tier("Fashionably late", "Hear about details a day late.", "Free • Forever"),
    Tier("Late", "Hear details after a month — maybe.", "₱49 / month"),
    Tier("Absent", "Hear details a month after, or never.", "₱149 / month"),
)

// ---------- Root ----------
@Composable
fun ProfileScreen(username: String, email: String, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Orange,
        bottomBar = { BottomBar(selectedTab) { selectedTab = it } }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Header()
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
private fun Header() {
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
        CircleIcon(Icons.Filled.Notifications, Color.White, Orange)
    }
}

@Composable
private fun CircleIcon(icon: ImageVector, bg: Color, tint: Color) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable { },
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
        tiers.forEachIndexed { index, tier ->
            TierCard(tier, selected == index) { selected = index }
            if (index != tiers.lastIndex) Spacer(Modifier.height(12.dp))
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
private fun TierCard(tier: Tier, isSelected: Boolean, onClick: () -> Unit) {
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

// ---------- Bottom navigation ----------
@Composable
private fun BottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        "Home" to Icons.Filled.Home,
        "Search" to Icons.Filled.Search,
        "Profile" to Icons.Filled.Person,
    )
    Row(
        Modifier
            .fillMaxWidth()
            .background(Purple)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, (label, icon) ->
            val active = index == selected
            Column(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (active) Orange else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(horizontal = if (active) 28.dp else 16.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon, contentDescription = label,
                    tint = if (active) Color.White else Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    label,
                    color = if (active) Color.White else Color.White.copy(alpha = 0.55f),
                    fontSize = 9.sp
                )
            }
        }
    }
}