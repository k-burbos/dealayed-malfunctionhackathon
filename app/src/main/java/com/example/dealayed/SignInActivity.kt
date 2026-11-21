package com.example.dealayed

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
private val ErrorRed = Color(0xFF8E0F0F)

private val validEmail = "admin@gmail.com"

private val validPassword = "123"


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
// Demo login: only validEmail + validPassword (defined at the top) are accepted.
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
            else -> null
        }
        if (emailError != null || passwordError != null) {
            _form.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            delay(1_200) // pretend network call
            val emailMatches = f.email.trim().equals(validEmail, ignoreCase = true)
            val passwordMatches = f.password == validPassword
            if (!emailMatches || !passwordMatches) {
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

        // When login succeeds, open HomeActivity and close this screen
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userEmail.collect { email ->
                    if (email != null) {
                        startActivity(
                            Intent(this@SignInActivity, HomeActivity::class.java)
                                .putExtra("EXTRA_EMAIL", email)
                        )
                        finish()
                    }
                }
            }
        }

        setContent {
            MaterialTheme { AppRoot(viewModel) }
        }
    }
}

// ---------- Root: shows Login ----------
@Composable
fun AppRoot(vm: AppViewModel) {
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