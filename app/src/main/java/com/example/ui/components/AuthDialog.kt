package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FirebaseUserData
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanBright
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGoldBright
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed

enum class AuthMode {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}

@Composable
fun AuthDialog(
    currentUser: FirebaseUserData?,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onDismiss: () -> Unit,
    onLogin: (email: String, pass: String) -> Unit,
    onRegister: (email: String, pass: String, name: String) -> Unit,
    onForgotPassword: (email: String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var currentMode by remember { mutableStateOf(AuthMode.LOGIN) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .testTag("auth_dialog_surface"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF209111E),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.verticalGradient(
                    listOf(NeonGoldBright.copy(alpha = 0.8f), NeonCyanBright.copy(alpha = 0.5f))
                )
            ),
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar: Icon, Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when (currentMode) {
                                        AuthMode.LOGIN -> Color(0x33F59E0B)
                                        AuthMode.REGISTER -> Color(0x3306B6D4)
                                        AuthMode.FORGOT_PASSWORD -> Color(0x33A855F7)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (currentMode) {
                                    AuthMode.LOGIN -> Icons.Default.Lock
                                    AuthMode.REGISTER -> Icons.Default.Person
                                    AuthMode.FORGOT_PASSWORD -> Icons.Default.VpnKey
                                },
                                contentDescription = "Auth Icon",
                                tint = when (currentMode) {
                                    AuthMode.LOGIN -> NeonGoldBright
                                    AuthMode.REGISTER -> NeonCyanBright
                                    AuthMode.FORGOT_PASSWORD -> Color(0xFFC084FC)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = when (currentMode) {
                                    AuthMode.LOGIN -> "FIREBASE SIGN IN"
                                    AuthMode.REGISTER -> "CREATE ACCOUNT"
                                    AuthMode.FORGOT_PASSWORD -> "RESET PASSWORD 🔑"
                                },
                                color = when (currentMode) {
                                    AuthMode.LOGIN -> NeonGoldBright
                                    AuthMode.REGISTER -> NeonCyanBright
                                    AuthMode.FORGOT_PASSWORD -> Color(0xFFC084FC)
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "A23MAX Cloud Security",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .testTag("auth_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // If user is already logged in, show Account Profile Card
                if (currentUser != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x350F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3322C55E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.displayName.take(2).uppercase(),
                                    color = NeonGreen,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = currentUser.displayName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = currentUser.email,
                                color = NeonCyanBright,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x2222C55E)
                            ) {
                                Text(
                                    text = "● ACTIVE FIREBASE SESSION",
                                    color = NeonGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    onSignOut()
                                    Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x33EF4444),
                                    contentColor = NeonRed
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sign Out of Account", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Auth Tabs selector (LOGIN vs REGISTER)
                    if (currentMode != AuthMode.FORGOT_PASSWORD) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x33000000),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (currentMode == AuthMode.LOGIN) NeonGoldBright else Color.Transparent)
                                        .clickable {
                                            currentMode = AuthMode.LOGIN
                                            localValidationError = null
                                        }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sign In",
                                        color = if (currentMode == AuthMode.LOGIN) Color.Black else Color.Gray,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (currentMode == AuthMode.REGISTER) NeonCyanBright else Color.Transparent)
                                        .clickable {
                                            currentMode = AuthMode.REGISTER
                                            localValidationError = null
                                        }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Register",
                                        color = if (currentMode == AuthMode.REGISTER) Color.Black else Color.Gray,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Display errors or success messages
                    val activeError = localValidationError ?: errorMessage
                    if (!activeError.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x33EF4444),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = NeonRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeError,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (!successMessage.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x3322C55E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = successMessage,
                                    color = Color(0xFF86EFAC),
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Input Form Fields
                    when (currentMode) {
                        AuthMode.LOGIN -> {
                            // EMAIL
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = {
                                    emailInput = it
                                    localValidationError = null
                                },
                                label = { Text("Email Address", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = "Email", tint = NeonGoldBright, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGoldBright,
                                    unfocusedBorderColor = Color(0x44F59E0B),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = NeonGoldBright,
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_login_email_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // PASSWORD
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    localValidationError = null
                                },
                                label = { Text("Password", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = "Password", tint = NeonGoldBright, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Password",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    if (emailInput.isBlank() || passwordInput.isBlank()) {
                                        localValidationError = "Please enter both email and password."
                                    } else {
                                        onLogin(emailInput, passwordInput)
                                    }
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGoldBright,
                                    unfocusedBorderColor = Color(0x44F59E0B),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = NeonGoldBright,
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_login_password_input")
                            )

                            // Forgot Password Link
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        currentMode = AuthMode.FORGOT_PASSWORD
                                        localValidationError = null
                                    },
                                    modifier = Modifier.testTag("auth_forgot_password_btn")
                                ) {
                                    Text(
                                        text = "Forgot password 🔑",
                                        color = NeonCyanBright,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Sign In Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (emailInput.isBlank() || passwordInput.isBlank()) {
                                        localValidationError = "Please fill in all credentials."
                                    } else {
                                        onLogin(emailInput, passwordInput)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonGoldBright,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("auth_submit_login_btn"),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("SIGN IN TO A23MAX", fontSize = 13.5.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick VIP Auto Fill Demo
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x1AF59E0B),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x33F59E0B)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        emailInput = "woldcom87@gmail.com"
                                        passwordInput = "A23max@2026"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "👑 Fill Official Creator Account Demo",
                                        color = NeonGoldBright,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        AuthMode.REGISTER -> {
                            // FULL NAME
                            OutlinedTextField(
                                value = displayNameInput,
                                onValueChange = {
                                    displayNameInput = it
                                    localValidationError = null
                                },
                                label = { Text("Full Name / Display Name", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = "Name", tint = NeonCyanBright, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x4406B6D4),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = NeonCyanBright,
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_register_name_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // EMAIL
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = {
                                    emailInput = it
                                    localValidationError = null
                                },
                                label = { Text("Email Address", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = "Email", tint = NeonCyanBright, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x4406B6D4),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = NeonCyanBright,
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_register_email_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // PASSWORD
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    localValidationError = null
                                },
                                label = { Text("Password (min 6 chars)", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = "Password", tint = NeonCyanBright, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Password",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x4406B6D4),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = NeonCyanBright,
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_register_password_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // CONFIRM PASSWORD
                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = {
                                    confirmPasswordInput = it
                                    localValidationError = null
                                },
                                label = { Text("Confirm Password", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Key, contentDescription = "Confirm", tint = NeonCyanBright, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Password",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    if (emailInput.isBlank() || passwordInput.isBlank() || displayNameInput.isBlank()) {
                                        localValidationError = "Please fill in all fields."
                                    } else if (passwordInput != confirmPasswordInput) {
                                        localValidationError = "Passwords do not match."
                                    } else {
                                        onRegister(emailInput, passwordInput, displayNameInput)
                                    }
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyanBright,
                                    unfocusedBorderColor = Color(0x4406B6D4),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = NeonCyanBright,
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_register_confirm_password_input")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Register Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (emailInput.isBlank() || passwordInput.isBlank() || displayNameInput.isBlank()) {
                                        localValidationError = "Please fill in all registration fields."
                                    } else if (passwordInput.length < 6) {
                                        localValidationError = "Password must be at least 6 characters."
                                    } else if (passwordInput != confirmPasswordInput) {
                                        localValidationError = "Passwords do not match."
                                    } else {
                                        onRegister(emailInput, passwordInput, displayNameInput)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyanBright,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("auth_submit_register_btn"),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("CREATE FREE ACCOUNT", fontSize = 13.5.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        AuthMode.FORGOT_PASSWORD -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x22A855F7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66A855F7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🔑 Password Recovery",
                                        color = Color(0xFFE9D5FF),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Enter your registered email below. Firebase Authentication will send a secure password reset link directly to your inbox.",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // EMAIL
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = {
                                    emailInput = it
                                    localValidationError = null
                                },
                                label = { Text("Registered Email Address", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFFC084FC), modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    if (emailInput.isBlank()) {
                                        localValidationError = "Please enter your email."
                                    } else {
                                        onForgotPassword(emailInput)
                                    }
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFC084FC),
                                    unfocusedBorderColor = Color(0x44A855F7),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = Color(0xFFC084FC),
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_forgot_email_input")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Send Reset Email Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (emailInput.isBlank()) {
                                        localValidationError = "Please enter your registered email address."
                                    } else {
                                        onForgotPassword(emailInput)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFA855F7),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("auth_submit_forgot_btn"),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("SEND RESET LINK ✉️", fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(
                                onClick = {
                                    currentMode = AuthMode.LOGIN
                                    localValidationError = null
                                }
                            ) {
                                Text("➔ Back to Sign In", color = NeonCyanBright, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
