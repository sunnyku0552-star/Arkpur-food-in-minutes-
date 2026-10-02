package com.example.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RoleSwitcherDialog(
    currentPanel: UserRole,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val allRestaurants by viewModel.allRestaurants.collectAsState()
    val activeRestaurant by viewModel.activeRestaurant.collectAsState()
    val activeDeliveryPartner by viewModel.activeDeliveryPartner.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Switch Platform Portal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "All 4 portals share ONE live database engine",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = OutlineLight)

                // 1. Customer
                PortalOptionCard(
                    title = "1. Customer App",
                    subtitle = "Order food in minutes, browse menus, live tracking",
                    icon = Icons.Default.ShoppingCart,
                    iconBg = ArkpurOrangeLight,
                    iconTint = ArkpurOrangeDark,
                    isSelected = currentPanel == UserRole.CUSTOMER,
                    onClick = {
                        viewModel.switchPanel(UserRole.CUSTOMER)
                        onDismiss()
                    }
                )

                // 2. Restaurant Partner
                PortalOptionCard(
                    title = "2. Restaurant Partner Panel",
                    subtitle = "Active: ${activeRestaurant?.name ?: "Arkpur Biryani"}",
                    icon = Icons.Default.Storefront,
                    iconBg = Color(0xFFEDE7F6),
                    iconTint = Color(0xFF512DA8),
                    isSelected = currentPanel == UserRole.RESTAURANT,
                    onClick = {
                        viewModel.switchPanel(UserRole.RESTAURANT)
                        onDismiss()
                    }
                )

                // 3. Delivery Partner
                PortalOptionCard(
                    title = "3. Delivery Partner Panel",
                    subtitle = "Active: ${activeDeliveryPartner?.name ?: "Rahul Kumar (Bike)"}",
                    icon = Icons.Default.TwoWheeler,
                    iconBg = ArkpurGreenLight,
                    iconTint = ArkpurGreenDark,
                    isSelected = currentPanel == UserRole.DELIVERY,
                    onClick = {
                        viewModel.switchPanel(UserRole.DELIVERY)
                        onDismiss()
                    }
                )

                // 4. Admin Panel
                PortalOptionCard(
                    title = "4. Admin Control & Ledger",
                    subtitle = "Platform financials, ledger, commission & approvals",
                    icon = Icons.Default.AdminPanelSettings,
                    iconBg = Color(0xFFECEFF1),
                    iconTint = Color(0xFF37474F),
                    isSelected = currentPanel == UserRole.ADMIN,
                    onClick = {
                        viewModel.switchPanel(UserRole.ADMIN)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun PortalOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SurfaceVariantLight else SurfaceLight,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) ArkpurOrange else OutlineLight),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = ArkpurOrange, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    notifications: List<NotificationEntity>,
    onDismiss: () -> Unit,
    onMarkRead: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Notifications",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${notifications.size} total",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No notifications yet", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(notifications) { notif ->
                        val dateStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(notif.timestamp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (notif.isRead) SurfaceLight else ArkpurOrangeLight.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, OutlineLight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMarkRead(notif.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ArkpurOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fastfood,
                                        contentDescription = null,
                                        tint = ArkpurOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = notif.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Text(text = dateStr, fontSize = 10.sp, color = TextMuted)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = notif.message,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerOtpLoginDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val mobile by viewModel.otpMobile.collectAsState()
    val otp by viewModel.otpCode.collectAsState()
    val isSent by viewModel.otpSent.collectAsState()
    val timer by viewModel.otpTimerSeconds.collectAsState()
    val error by viewModel.otpErrorMessage.collectAsState()
    val otpHint by viewModel.otpGeneratedDisplay.collectAsState()
    val isLoading by viewModel.isOtpLoading.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (!isSent) "Customer Login" else "Verify Mobile OTP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = if (!isSent)
                        "Enter your 10-digit mobile number to receive a secure OTP."
                    else
                        "Enter the 6-digit verification code sent to +91 $mobile",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                if (!isSent) {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { if (it.length <= 10) viewModel.otpMobile.value = it },
                        label = { Text("Mobile Number") },
                        prefix = { Text("+91 ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_mobile")
                    )

                    Button(
                        onClick = { viewModel.sendOtp() },
                        enabled = mobile.length == 10 && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = ArkpurOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_send_otp")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Send OTP", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // OTP Sent view
                    otpHint?.let { code ->
                        Surface(
                            color = ArkpurGreenLight,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ArkpurGreen)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "SMS Sent! Test OTP: $code",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArkpurGreenDark
                                )
                                TextButton(onClick = { viewModel.otpCode.value = code }) {
                                    Text("Auto-fill", fontSize = 11.sp, color = ArkpurGreenDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) viewModel.otpCode.value = it },
                        label = { Text("6-Digit OTP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_otp")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (timer > 0) {
                            Text(
                                text = "Resend OTP in ${timer}s",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        } else {
                            TextButton(onClick = { viewModel.sendOtp() }) {
                                Text("Resend OTP", color = ArkpurOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        TextButton(onClick = { viewModel.otpSent.value = false }) {
                            Text("Change Number", color = TextSecondary, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.verifyOtp {
                                onDismiss()
                            }
                        },
                        enabled = otp.length == 6 && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = ArkpurGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_verify_otp")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Verify & Continue", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                error?.let { err ->
                    Text(
                        text = err,
                        color = ArkpurRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
