package com.example.ui.publisher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.data.security.AdminAuthManager
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet
import kotlinx.coroutines.launch

private const val DEFAULT_DEVELOPER_PASSWORD = "2812246216200810"

@Composable
fun DeveloperAuthDialog(
    isOpen: Boolean,
    developerPassword: String = DEFAULT_DEVELOPER_PASSWORD,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentAdminUser by AdminAuthManager.currentAdmin.collectAsState()

    var emailInput by remember {
        mutableStateOf(AdminAuthManager.getSavedAdminEmail(context).ifBlank { "admin@foul.tv" })
    }
    var passwordInput by remember { mutableStateOf(DEFAULT_DEVELOPER_PASSWORD) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun executeSignIn(isCreatingAccount: Boolean = false) {
        val trimmedEmail = emailInput.trim()
        val trimmedPass = passwordInput.trim()

        if (trimmedPass == "2008") {
            errorMessage = "تم إلغاء وحظر كلمة المرور القديمة (2008) نهائياً لدواعي الأمان."
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        coroutineScope.launch {
            val result = if (isCreatingAccount) {
                AdminAuthManager.createAdminAccount(trimmedEmail, trimmedPass, context)
            } else {
                AdminAuthManager.signInAdmin(trimmedEmail, trimmedPass, context)
            }

            isLoading = false
            result.fold(
                onSuccess = { user ->
                    successMessage = "تم التحقق وتسجيل الدخول بنجاح كمسؤول (${user.email ?: user.uid})"
                    onSuccess()
                    onDismiss()
                },
                onFailure = { error ->
                    val msg = error.localizedMessage ?: "فشل تسجيل الدخول كمسؤول في Firebase Auth."
                    // If account doesn't exist yet, suggest creating it with one click
                    if (msg.contains("no user record", ignoreCase = true) || msg.contains("user-not-found", ignoreCase = true)) {
                        errorMessage = "الحساب غير موجود مسبقاً، جارٍ إنشاؤه كمسؤول جديد في Firebase..."
                        coroutineScope.launch {
                            val createResult = AdminAuthManager.createAdminAccount(trimmedEmail, trimmedPass, context)
                            if (createResult.isSuccess) {
                                onSuccess()
                                onDismiss()
                            } else {
                                errorMessage = createResult.exceptionOrNull()?.localizedMessage ?: "فشل إنشاء حساب الإدمن."
                            }
                        }
                    } else {
                        // If password matches Developer Master Password, permit entering developer mode
                        val masterPass = developerPassword.trim().ifBlank { DEFAULT_DEVELOPER_PASSWORD }
                        if (trimmedPass == masterPass || trimmedPass == DEFAULT_DEVELOPER_PASSWORD) {
                            // Master password valid
                            errorMessage = null
                            onSuccess()
                            onDismiss()
                        } else {
                            errorMessage = msg
                        }
                    }
                }
            )
        }
    }

    Dialog(
        onDismissRequest = {
            errorMessage = null
            successMessage = null
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = BorderStroke(1.5.dp, NeonPurple.copy(alpha = 0.7f)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .fillMaxWidth(0.95f)
                .testTag("developer_auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Close Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = {
                            errorMessage = null
                            successMessage = null
                            onDismiss()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Glowing Admin Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(DarkBackground, CircleShape)
                        .border(1.5.dp, NeonPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "لوحة التحكم",
                        tint = NeonPurple,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = "مصادقة المطور ولوحة التحكم",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = "نسخة المطور الرسمية: مصادقة Firebase Auth لتفعيل صلاحيات الكتابة والإدارة الكاملة في قاعدة البيانات",
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Current Firebase Auth Status Card
                val isAuthActive = (currentAdminUser != null)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAuthActive) SuccessGreen.copy(alpha = 0.12f) else DarkBackground,
                    border = BorderStroke(1.dp, if (isAuthActive) SuccessGreen else DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isAuthActive) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isAuthActive) SuccessGreen else NeonPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAuthActive) "حساب الإدمن نشط ومصرح بالكتابة 🛡️" else "تسجيل الدخول مطلوب لصلاحيات الكتابة",
                                color = if (isAuthActive) SuccessGreen else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAuthActive) (currentAdminUser?.email ?: currentAdminUser?.uid ?: "") else "العمليات الإدارية مرتبطة بنظام Firebase Auth",
                                color = TextMuted,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Admin Email Input
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("البريد الإلكتروني للإدمن", fontSize = 12.sp) },
                    placeholder = { Text("admin@foul.tv", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        errorBorderColor = LiveRed,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        cursorColor = NeonPurple
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_email_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Password Field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("كلمة المرور للإدمن", fontSize = 12.sp) },
                    placeholder = { Text("2812246216200810", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isPasswordVisible) "إخفاء" else "إظهار",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = (errorMessage != null),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { executeSignIn(false) }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        errorBorderColor = LiveRed,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        cursorColor = NeonPurple
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("developer_password_input")
                )

                // Error Message if any
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = LiveRed,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                // Success Message if any
                if (successMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = successMessage ?: "",
                        color = SuccessGreen,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            errorMessage = null
                            successMessage = null
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkSurfaceBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "إلغاء",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { executeSignIn(false) },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = VividViolet),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("developer_direct_entry_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isAuthActive) "دخول للوحة التحكم" else "تسجيل الدخول كمسؤول",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // If not authenticated, option to create the admin account
                if (!isAuthActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { executeSignIn(true) },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "إنشاء الحساب كمسؤول في Firebase Auth",
                            color = NeonPurple,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
