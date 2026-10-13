package com.example.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ChatUser
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkPurpleContainer
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividViolet

@Composable
fun ChatAuthDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onRegister: (ChatUser, onResult: (Result<ChatUser>) -> Unit) -> Unit,
    onLogin: (String, String, onResult: (Result<ChatUser>) -> Unit) -> Unit
) {
    if (!isOpen) return

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Register, 1: Login
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("chat_auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedTabIndex == 0) "إنشاء حساب للدردشة" else "تسجيل الدخول للدردشة",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("chat_auth_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إلغاء",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "يتطلب إرسال الرسائل والتفاعل حساباً معتمداً. يمكنك الإلغاء والاستمرار في قراءة الرسائل.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp)
                )

                // Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = DarkSurfaceVariant,
                    contentColor = TextPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = NeonPurple
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = {
                            selectedTabIndex = 0
                            errorMessage = null
                        },
                        text = {
                            Text(
                                "إنشاء حساب جديد",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == 0) TextPrimary else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = {
                            selectedTabIndex = 1
                            errorMessage = null
                        },
                        text = {
                            Text(
                                "تسجيل الدخول",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == 1) TextPrimary else TextSecondary
                            )
                        }
                    )
                }

                // Error Banner
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LiveRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, LiveRed.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = LiveRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Fields
                if (selectedTabIndex == 0) {
                    // Full Name (الاسم الشخصي)
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            errorMessage = null
                        },
                        label = { Text("الاسم الشخصي (الظاهر للجميع)", fontSize = 11.sp) },
                        placeholder = { Text("مثال: أحمد محمد", fontSize = 11.sp, color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = NeonPurple,
                            unfocusedLabelColor = TextSecondary,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_full_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Username (اسم المستخدم)
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it.trim().lowercase().replace(" ", "_").replace("@", "")
                        errorMessage = null
                    },
                    label = { Text("اسم المستخدم (معرّف الحساب)", fontSize = 11.sp) },
                    placeholder = { Text("مثال: ahmed_99", fontSize = 11.sp, color = TextMuted) },
                    leadingIcon = {
                        Text("@", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(start = 12.dp))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = NeonPurple,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_username_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password (كلمة المرور)
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("كلمة المرور", fontSize = 11.sp) },
                    placeholder = { Text("••••••••", fontSize = 11.sp, color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "تبديل الرؤية",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = NeonPurple,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Submit + Cancel (إلغاء)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel button (إلغاء)
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DarkSurfaceBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("auth_cancel_button")
                    ) {
                        Text("إلغاء", fontSize = 12.sp)
                    }

                    // Submit button
                    Button(
                        onClick = {
                            if (selectedTabIndex == 0) {
                                if (fullName.isBlank()) {
                                    errorMessage = "يرجى كتابة الاسم الشخصي"
                                    return@Button
                                }
                                if (username.isBlank()) {
                                    errorMessage = "يرجى كتابة اسم المستخدم"
                                    return@Button
                                }
                                if (password.length < 4) {
                                    errorMessage = "كلمة المرور يجب أن تكون 4 أحرف على الأقل"
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null
                                onRegister(
                                    ChatUser(
                                        username = username,
                                        fullName = fullName.trim(),
                                        password = password.trim()
                                    )
                                ) { result ->
                                    isLoading = false
                                    if (result.isSuccess) {
                                        onDismiss()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "حدث خطأ أثناء إنشاء الحساب"
                                    }
                                }
                            } else {
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = "يرجى إدخال اسم المستخدم وكلمة المرور"
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null
                                onLogin(username, password) { result ->
                                    isLoading = false
                                    if (result.isSuccess) {
                                        onDismiss()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "حدث خطأ أثناء تسجيل الدخول"
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VividViolet),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(42.dp)
                            .testTag("auth_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = if (selectedTabIndex == 0) "إنشاء ومتابعة" else "تسجيل الدخول",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
