package com.example.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun VegBadge(isVeg: Boolean, modifier: Modifier = Modifier) {
    val borderColor = if (isVeg) ArkpurGreen else ArkpurRed
    val dotColor = if (isVeg) ArkpurGreen else ArkpurRed
    Box(
        modifier = modifier
            .size(16.dp)
            .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(3.dp))
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

@Composable
fun RatingBadge(rating: Float, modifier: Modifier = Modifier) {
    Surface(
        color = ArkpurGreen,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = String.format(Locale.US, "%.1f", rating),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.PLACED -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
        OrderStatus.CONFIRMED -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        OrderStatus.PREPARING -> Pair(Color(0xFFEDE7F6), Color(0xFF512DA8))
        OrderStatus.READY_FOR_PICKUP -> Pair(Color(0xFFE0F7FA), Color(0xFF00838F))
        OrderStatus.DELIVERY_ASSIGNED -> Pair(Color(0xFFE1F5FE), Color(0xFF0277BD))
        OrderStatus.OUT_FOR_DELIVERY -> Pair(Color(0xFFFFF8E1), Color(0xFFF57F17))
        OrderStatus.DELIVERED, OrderStatus.COMPLETED -> Pair(Color(0xFFE8F5E9), Color(0xFF1B5E20))
        OrderStatus.CANCELLED -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CurrencyText(
    amount: Double,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = TextPrimary
) {
    val formatted = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount)
        .replace("INR", "₹")
    Text(
        text = formatted,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArkpurPlatformHeader(
    currentPanel: UserRole,
    unreadNotificationsCount: Int,
    onOpenRoleSwitcher: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceLight,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ARKPUR",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = ArkpurOrange,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        color = ArkpurGreen,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "MINUTES",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = when (currentPanel) {
                        UserRole.CUSTOMER -> "Customer Food Portal"
                        UserRole.RESTAURANT -> "Restaurant Partner Dashboard"
                        UserRole.DELIVERY -> "Delivery Fleet Dashboard"
                        UserRole.ADMIN -> "Central Command & Ledger"
                    },
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Notifications icon
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier.testTag("btn_notifications")
                ) {
                    BadgedBox(badge = {
                        if (unreadNotificationsCount > 0) {
                            Badge(containerColor = ArkpurRed) {
                                Text("$unreadNotificationsCount")
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = TextPrimary
                        )
                    }
                }

                // Switch Portal Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (currentPanel) {
                        UserRole.CUSTOMER -> ArkpurOrangeLight
                        UserRole.RESTAURANT -> Color(0xFFEDE7F6)
                        UserRole.DELIVERY -> ArkpurGreenLight
                        UserRole.ADMIN -> Color(0xFFECEFF1)
                    },
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier
                        .clickable { onOpenRoleSwitcher() }
                        .testTag("btn_role_switcher")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (currentPanel) {
                                UserRole.CUSTOMER -> Icons.Default.ShoppingCart
                                UserRole.RESTAURANT -> Icons.Default.Storefront
                                UserRole.DELIVERY -> Icons.Default.TwoWheeler
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                            },
                            contentDescription = null,
                            tint = when (currentPanel) {
                                UserRole.CUSTOMER -> ArkpurOrangeDark
                                UserRole.RESTAURANT -> Color(0xFF512DA8)
                                UserRole.DELIVERY -> ArkpurGreenDark
                                UserRole.ADMIN -> Color(0xFF37474F)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when (currentPanel) {
                                UserRole.CUSTOMER -> "Customer"
                                UserRole.RESTAURANT -> "Restaurant"
                                UserRole.DELIVERY -> "Delivery"
                                UserRole.ADMIN -> "Admin"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Panel",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, OutlineLight),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

