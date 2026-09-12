package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseUserData
import com.example.model.UserProfile
import com.example.ui.components.WallpaperBackground
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.viewmodel.AuthScreenMode
import com.example.viewmodel.OtpState
import com.example.viewmodel.PinLockState

@Composable
fun AuthGateScreen(
    authScreenMode: AuthScreenMode,
    otpState: OtpState,
    pinLockState: PinLockState,
    currentUser: FirebaseUserData?,
    userProfile: UserProfile,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onSetScreenMode: (AuthScreenMode) -> Unit,
    onLogin: (email: String, pass: String) -> Unit,
    onRegisterPhoneOtp: (activity: Activity, name: String, phone: String, email: String, pass: String, confirmPass: String, city: String) -> Unit,
    onVerifyOtp: (otp: String) -> Unit,
    onResendOtp: (activity: Activity) -> Unit,
    onUnlockPin: (pin: String) -> Unit,
    onForgotPinStart: (activity: Activity) -> Unit,
    onForgotPinVerifyOtp: (otp: String) -> Unit,
    onForgotPinSetNewPin: (newPin: String, confirmPin: String) -> Unit,
    onForgotPassword: (email: String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var cityInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }

    // PIN lock input state
    var pinDigit1 by remember { mutableStateOf("") }
    var pinDigit2 by remember { mutableStateOf("") }
    var pinDigit3 by remember { mutableStateOf("") }
    var pinDigit4 by remember { mutableStateOf("") }

    var newPinInput by remember { mutableStateOf("") }
    var confirmNewPinInput by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    WallpaperBackground(
        wallpaperStyle = com.example.model.WallpaperStyle.ROYAL_GOLD_HD,
        dimLevel = 0.30f,
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_gate_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Official A23 Header Emblem
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(NeonGoldBright, NeonCyanBright, NeonGoldBright)
                        )
                    )
                    .padding(2.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF090E17)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "👑", fontSize = 16.sp)
                        Text(
                            text = "A23",
                            color = NeonGoldBright,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "A23 MAX VIP TERMINAL",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Text(
                text = when (authScreenMode) {
                    AuthScreenMode.LOGIN -> "Official Firebase Production Gateway"
                    AuthScreenMode.REGISTER -> "Create Verified Trader Account via SMS OTP"
                    AuthScreenMode.OTP_VERIFICATION -> "Real Firebase SMS OTP Verification"
                    AuthScreenMode.PIN_UNLOCK -> "4-Digit Terminal PIN Security Lock"
                    AuthScreenMode.FORGOT_PASSWORD -> "Firebase Password Recovery"
                    AuthScreenMode.FORGOT_PIN_OTP -> "Verify Phone to Reset Terminal PIN"
                    AuthScreenMode.FORGOT_PIN_NEW_PIN -> "Set New 4-Digit Terminal PIN"
                },
                color = NeonCyanBright,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Main Glassmorphic Container
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xD90B1220),
                border = BorderStroke(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(NeonGoldBright.copy(alpha = 0.7f), NeonCyanBright.copy(alpha = 0.4f))
                    )
                ),
                shadowElevation = 24.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Mode Tabs (Visible on Login / Register)
                    if (authScreenMode == AuthScreenMode.LOGIN || authScreenMode == AuthScreenMode.REGISTER) {
                        TabRow(
                            selectedTabIndex = if (authScreenMode == AuthScreenMode.LOGIN) 0 else 1,
                            containerColor = Color(0x331E293B),
                            contentColor = NeonGoldBright,
                            indicator = { tabPositions ->
                                androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[if (authScreenMode == AuthScreenMode.LOGIN) 0 else 1]),
                                    color = if (authScreenMode == AuthScreenMode.REGISTER) NeonCyanBright else NeonGoldBright,
                                    height = 3.dp
                                )
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = authScreenMode == AuthScreenMode.LOGIN,
                                onClick = {
                                    localError = null
                                    onSetScreenMode(AuthScreenMode.LOGIN)
                                },
                                text = {
                                    Text(
                                        "🔑 Login",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (authScreenMode == AuthScreenMode.LOGIN) NeonGoldBright else Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            )
                            Tab(
                                selected = authScreenMode == AuthScreenMode.REGISTER,
                                onClick = {
                                    localError = null
                                    onSetScreenMode(AuthScreenMode.REGISTER)
                                },
                                text = {
                                    Text(
                                        "📝 Register (SMS OTP)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (authScreenMode == AuthScreenMode.REGISTER) NeonCyanBright else Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Global Error / Lockout Banner
                    val displayError = localError ?: errorMessage ?: otpState.errorMessage ?: pinLockState.errorMessage
                    AnimatedVisibility(visible = displayError != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x33EF4444),
                            border = BorderStroke(1.dp, Color(0x80EF4444)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "⚠️ ${displayError ?: ""}",
                                color = NeonRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Success Feedback Banner
                    AnimatedVisibility(visible = successMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x3310B981),
                            border = BorderStroke(1.dp, Color(0x8010B981)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "✅ ${successMessage ?: ""}",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Content Switcher
                    when (authScreenMode) {
                        // ==========================================
                        // 1. LOGIN SCREEN
                        // ==========================================
                        AuthScreenMode.LOGIN -> {
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = NeonGoldBright)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGoldBright,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = NeonGoldBright)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility",
                                            tint = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    if (emailInput.isBlank() || passwordInput.isBlank()) {
                                        localError = "Please enter both email and password."
                                    } else {
                                        localError = null
                                        onLogin(emailInput.trim(), passwordInput.trim())
                                    }
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGoldBright,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onSetScreenMode(AuthScreenMode.FORGOT_PASSWORD) }) {
                                    Text("Forgot Password?", color = NeonCyanBright, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (emailInput.isBlank() || passwordInput.isBlank()) {
                                        localError = "Please enter both email and password."
                                    } else {
                                        localError = null
                                        onLogin(emailInput.trim(), passwordInput.trim())
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("auth_signin_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonGoldBright,
                                    contentColor = Color.Black
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                                } else {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("LOG IN & UNLOCK APP", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }
                        }

                        // ==========================================
                        // 2. REGISTER SCREEN (SMS OTP)
                        // ==========================================
                        AuthScreenMode.REGISTER -> {
                            OutlinedTextField(
                                value = displayNameInput,
                                onValueChange = { displayNameInput = it },
                                label = { Text("Your Full Name") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyanBright)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("register_name_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("Mobile Number (SMS OTP Verification)") },
                                placeholder = { Text("+91 9876543210") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = NeonCyanBright)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("register_phone_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = NeonCyanBright)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("register_email_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = cityInput,
                                onValueChange = { cityInput = it },
                                label = { Text("City / State (e.g. Mumbai, MH)") },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = NeonCyanBright)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Create Password (min 6 chars)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyanBright)
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("register_pass_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = { confirmPasswordInput = it },
                                label = { Text("Confirm Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = NeonCyanBright)
                                },
                                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    when {
                                        displayNameInput.isBlank() -> localError = "Please enter your full name."
                                        phoneInput.filter { it.isDigit() }.length < 10 -> localError = "Please enter a valid 10-digit mobile number."
                                        emailInput.isBlank() -> localError = "Please enter your email."
                                        passwordInput.length < 6 -> localError = "Password must be at least 6 characters."
                                        passwordInput != confirmPasswordInput -> localError = "Passwords do not match."
                                        activity == null -> localError = "System error: Activity not ready."
                                        else -> {
                                            localError = null
                                            onRegisterPhoneOtp(
                                                activity,
                                                displayNameInput.trim(),
                                                phoneInput.trim(),
                                                emailInput.trim(),
                                                passwordInput.trim(),
                                                confirmPasswordInput.trim(),
                                                cityInput.trim()
                                            )
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("auth_register_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyanBright,
                                    contentColor = Color.Black
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                                } else {
                                    Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SEND SMS OTP & VERIFY", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }
                        }

                        // ==========================================
                        // 3. OTP VERIFICATION SCREEN
                        // ==========================================
                        AuthScreenMode.OTP_VERIFICATION -> {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = null,
                                tint = NeonCyanBright,
                                modifier = Modifier.size(44.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Enter SMS Verification Code",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )

                            Text(
                                text = "A real 6-digit SMS OTP has been sent by Firebase to:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Text(
                                text = otpState.targetPhone,
                                color = NeonGoldBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                        otpInput = it
                                    }
                                },
                                label = { Text("6-Digit SMS OTP") },
                                placeholder = { Text("123456") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    if (otpInput.length == 6) {
                                        onVerifyOtp(otpInput)
                                    }
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x66FFFFFF),
                                    focusedTextColor = NeonGoldBright,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 8.sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = FontFamily.Monospace
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().testTag("otp_input_field")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (otpInput.length != 6) {
                                        localError = "Please enter complete 6-digit OTP."
                                    } else {
                                        localError = null
                                        onVerifyOtp(otpInput.trim())
                                    }
                                },
                                enabled = !isLoading && otpInput.length == 6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("verify_otp_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyanBright,
                                    contentColor = Color.Black
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("VERIFY OTP & ACTIVATE ACCOUNT", fontWeight = FontWeight.Black, fontSize = 13.5.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { onSetScreenMode(AuthScreenMode.REGISTER) }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change Number", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                }

                                if (otpState.cooldownSecondsRemaining > 0) {
                                    Text(
                                        text = "Resend in ${otpState.cooldownSecondsRemaining}s",
                                        color = NeonGoldBright,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    TextButton(
                                        onClick = {
                                            if (activity != null) {
                                                onResendOtp(activity)
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Resend SMS OTP", color = NeonCyanBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // ==========================================
                        // 4. PIN UNLOCK SCREEN (LOCAL 4-DIGIT PIN)
                        // ==========================================
                        AuthScreenMode.PIN_UNLOCK -> {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = NeonGoldBright,
                                modifier = Modifier.size(46.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Terminal Security PIN",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )

                            Text(
                                text = "Enter your 4-digit App PIN to unlock session",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // 4 PIN Dots Indicator
                            val currentPinLength = pinDigit1.length + pinDigit2.length + pinDigit3.length + pinDigit4.length
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 1..4) {
                                    val isFilled = currentPinLength >= i
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isFilled) NeonGoldBright else Color(0x33FFFFFF)
                                            )
                                            .border(
                                                1.5.dp,
                                                if (isFilled) NeonGold else Color(0x66FFFFFF),
                                                CircleShape
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Keypad
                            val isLockedOut = pinLockState.cooldownSecondsRemaining > 0L

                            if (isLockedOut) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0x33EF4444),
                                    border = BorderStroke(1.dp, Color(0x80EF4444)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🔒 Security Lockout Active\nRetry in ${pinLockState.cooldownSecondsRemaining} seconds",
                                        color = NeonRed,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val keypad = listOf(
                                        listOf("1", "2", "3"),
                                        listOf("4", "5", "6"),
                                        listOf("7", "8", "9"),
                                        listOf("C", "0", "🔓")
                                    )

                                    for (row in keypad) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            for (key in row) {
                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = when (key) {
                                                        "C" -> Color(0x33EF4444)
                                                        "🔓" -> NeonGoldBright
                                                        else -> Color(0x26FFFFFF)
                                                    },
                                                    border = BorderStroke(
                                                        1.dp,
                                                        when (key) {
                                                            "C" -> Color(0x66EF4444)
                                                            "🔓" -> NeonGold
                                                            else -> Color(0x44FFFFFF)
                                                        }
                                                    ),
                                                    modifier = Modifier
                                                        .size(68.dp, 48.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .clickable {
                                                            when (key) {
                                                                "C" -> {
                                                                    pinDigit1 = ""
                                                                    pinDigit2 = ""
                                                                    pinDigit3 = ""
                                                                    pinDigit4 = ""
                                                                }
                                                                "🔓" -> {
                                                                    val fullPin = pinDigit1 + pinDigit2 + pinDigit3 + pinDigit4
                                                                    if (fullPin.length == 4) {
                                                                        onUnlockPin(fullPin)
                                                                    }
                                                                }
                                                                else -> {
                                                                    if (pinDigit1.isEmpty()) pinDigit1 = key
                                                                    else if (pinDigit2.isEmpty()) pinDigit2 = key
                                                                    else if (pinDigit3.isEmpty()) pinDigit3 = key
                                                                    else if (pinDigit4.isEmpty()) {
                                                                        pinDigit4 = key
                                                                        val fullPin = pinDigit1 + pinDigit2 + pinDigit3 + key
                                                                        onUnlockPin(fullPin)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = key,
                                                            color = if (key == "🔓") Color.Black else Color.White,
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        if (activity != null) {
                                            onForgotPinStart(activity)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.LockReset, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Forgot PIN?", color = NeonCyanBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                TextButton(onClick = { onSignOut() }) {
                                    Text("Log Out", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                }
                            }
                        }

                        // ==========================================
                        // 5. FORGOT PASSWORD SCREEN
                        // ==========================================
                        AuthScreenMode.FORGOT_PASSWORD -> {
                            Icon(Icons.Default.Key, contentDescription = null, tint = NeonGoldBright, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Reset Account Password",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )

                            Text(
                                text = "Enter your registered email address to receive a password reset link directly from Firebase.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Registered Email Address") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = NeonGoldBright)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGoldBright,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (emailInput.isBlank()) {
                                        localError = "Please enter your email."
                                    } else {
                                        localError = null
                                        onForgotPassword(emailInput.trim())
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonGoldBright,
                                    contentColor = Color.Black
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                                } else {
                                    Text("SEND PASSWORD RESET LINK", fontWeight = FontWeight.Black, fontSize = 13.5.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(onClick = { onSetScreenMode(AuthScreenMode.LOGIN) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Back to Sign In", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }

                        // ==========================================
                        // 6. FORGOT PIN OTP VERIFICATION
                        // ==========================================
                        AuthScreenMode.FORGOT_PIN_OTP -> {
                            Icon(Icons.Default.Message, contentDescription = null, tint = NeonCyanBright, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Verify Identity to Reset PIN",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )

                            Text(
                                text = "Enter the 6-digit SMS OTP sent to your registered mobile number:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Text(
                                text = otpState.targetPhone,
                                color = NeonGoldBright,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                        otpInput = it
                                    }
                                },
                                label = { Text("6-Digit OTP") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x66FFFFFF),
                                    focusedTextColor = NeonGoldBright,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 8.sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = FontFamily.Monospace
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (otpInput.length != 6) {
                                        localError = "Please enter the complete 6-digit OTP."
                                    } else {
                                        localError = null
                                        onForgotPinVerifyOtp(otpInput.trim())
                                    }
                                },
                                enabled = !isLoading && otpInput.length == 6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyanBright,
                                    contentColor = Color.Black
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                                } else {
                                    Text("VERIFY & CREATE NEW PIN", fontWeight = FontWeight.Black, fontSize = 13.5.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(onClick = { onSetScreenMode(AuthScreenMode.PIN_UNLOCK) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Back to PIN Screen", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }

                        // ==========================================
                        // 7. CREATE NEW PIN AFTER FORGOT PIN OTP
                        // ==========================================
                        AuthScreenMode.FORGOT_PIN_NEW_PIN -> {
                            Icon(Icons.Default.LockReset, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Create New 4-Digit PIN",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )

                            Text(
                                text = "Identity verified! Please choose your new 4-digit security PIN.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = newPinInput,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                        newPinInput = it
                                    }
                                },
                                label = { Text("New 4-Digit PIN") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = confirmNewPinInput,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                        confirmNewPinInput = it
                                    }
                                },
                                label = { Text("Confirm New 4-Digit PIN") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = Color(0x44FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (newPinInput.length != 4) {
                                        localError = "PIN must be exactly 4 digits."
                                    } else if (newPinInput != confirmNewPinInput) {
                                        localError = "PIN entries do not match."
                                    } else {
                                        localError = null
                                        onForgotPinSetNewPin(newPinInput, confirmNewPinInput)
                                    }
                                },
                                enabled = newPinInput.length == 4 && confirmNewPinInput.length == 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonGreen,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SET PIN & UNLOCK TERMINAL", fontWeight = FontWeight.Black, fontSize = 13.5.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = Color(0x33FFFFFF), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Security Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = NeonGoldBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "A23 MAX Cryptographic Auth Shield Active",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
