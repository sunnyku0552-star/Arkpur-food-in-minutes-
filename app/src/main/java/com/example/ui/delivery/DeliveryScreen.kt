package com.example.ui.delivery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.common.*
import com.example.ui.theme.*

enum class DeliveryTab {
    JOBS,
    EARNINGS,
    PROFILE
}

@Composable
fun DeliveryScreen(
    viewModel: MainViewModel,
    onOpenRoleSwitcher: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(DeliveryTab.JOBS) }

    val activeRider by viewModel.activeDeliveryPartner.collectAsState()
    val availableOrders by viewModel.deliveryAvailableOrders.collectAsState()
    val assignedOrders by viewModel.deliveryAssignedOrders.collectAsState()
    val payouts by viewModel.deliveryPayouts.collectAsState()
    val notifications by viewModel.customerNotifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            Column {
                ArkpurPlatformHeader(
                    currentPanel = UserRole.DELIVERY,
                    unreadNotificationsCount = unreadCount,
                    onOpenRoleSwitcher = onOpenRoleSwitcher,
                    onOpenNotifications = onOpenNotifications
                )

                // Rider Status Bar with Online/Offline Toggle
                Surface(
                    color = if (activeRider?.isOnline == true) ArkpurGreenLight else Color(0xFFECEFF1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (activeRider?.isOnline == true) ArkpurGreen else Color.Gray)
                            )
                            Column {
                                Text(
                                    text = if (activeRider?.isOnline == true) "You are ONLINE 🛵" else "You are OFFLINE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (activeRider?.isOnline == true) ArkpurGreenDark else TextPrimary
                                )
                                Text(
                                    text = "${activeRider?.name} • ${activeRider?.vehicleType}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = activeRider?.isOnline == true,
                            onCheckedChange = { viewModel.toggleDeliveryOnlineStatus() }
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceLight, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = selectedTab == DeliveryTab.JOBS,
                    onClick = { selectedTab = DeliveryTab.JOBS },
                    icon = {
                        val activeJobCount = assignedOrders.count { it.orderStatus != OrderStatus.DELIVERED && it.orderStatus != OrderStatus.CANCELLED }
                        BadgedBox(badge = {
                            if (activeJobCount > 0) {
                                Badge(containerColor = ArkpurOrange) { Text("$activeJobCount") }
                            }
                        }) {
                            Icon(Icons.Default.TwoWheeler, contentDescription = "Jobs")
                        }
                    },
                    label = { Text("Deliveries", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = ArkpurGreen, indicatorColor = ArkpurGreenLight)
                )
                NavigationBarItem(
                    selected = selectedTab == DeliveryTab.EARNINGS,
                    onClick = { selectedTab = DeliveryTab.EARNINGS },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Earnings") },
                    label = { Text("Earnings", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = ArkpurGreen, indicatorColor = ArkpurGreenLight)
                )
                NavigationBarItem(
                    selected = selectedTab == DeliveryTab.PROFILE,
                    onClick = { selectedTab = DeliveryTab.PROFILE },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Rider Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = ArkpurGreen, indicatorColor = ArkpurGreenLight)
                )
            }
        },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                DeliveryTab.JOBS -> {
                    DeliveryJobsView(
                        isOnline = activeRider?.isOnline == true,
                        availableOrders = availableOrders,
                        assignedOrders = assignedOrders,
                        viewModel = viewModel
                    )
                }
                DeliveryTab.EARNINGS -> {
                    DeliveryEarningsView(
                        assignedOrders = assignedOrders,
                        payouts = payouts,
                        viewModel = viewModel
                    )
                }
                DeliveryTab.PROFILE -> {
                    DeliveryProfileView(
                        rider = activeRider
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// DELIVERY JOBS VIEW
// ----------------------------------------------------------------------------

@Composable
fun DeliveryJobsView(
    isOnline: Boolean,
    availableOrders: List<OrderEntity>,
    assignedOrders: List<OrderEntity>,
    viewModel: MainViewModel
) {
    val activeDeliveries = assignedOrders.filter { it.orderStatus == OrderStatus.DELIVERY_ASSIGNED || it.orderStatus == OrderStatus.OUT_FOR_DELIVERY }
    val completedDeliveries = assignedOrders.filter { it.orderStatus == OrderStatus.DELIVERED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isOnline) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = ArkpurOrange)
                        Column {
                            Text(text = "You are currently offline", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Turn ON status above to receive live delivery jobs in Arkpur", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Active In-Progress Deliveries
        if (activeDeliveries.isNotEmpty()) {
            item {
                Text(
                    text = "ACTIVE DELIVERY JOB",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = ArkpurOrange,
                    letterSpacing = 1.sp
                )
            }

            items(activeDeliveries) { order ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(2.dp, ArkpurOrange),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            OrderStatusBadge(status = order.orderStatus)
                        }

                        // Route details
                        Surface(shape = RoundedCornerShape(8.dp), color = SurfaceVariantLight, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF512DA8), modifier = Modifier.size(16.dp))
                                    Text(text = "Pickup: Arkpur Restaurant Kitchen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = ArkpurOrange, modifier = Modifier.size(16.dp))
                                    Text(text = "Drop: ${order.deliveryAddressText} (${order.customerName})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Payment: ${order.paymentMethod.displayName}", fontSize = 12.sp, color = TextSecondary)
                            Text(text = "Your Earning: ₹${order.deliveryShare.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ArkpurGreen)
                        }

                        // Execution Buttons
                        if (order.orderStatus == OrderStatus.DELIVERY_ASSIGNED) {
                            Button(
                                onClick = { viewModel.deliveryPickupOrder(order.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = ArkpurOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reached Restaurant & Picked Up Parcel 📦", fontWeight = FontWeight.Bold)
                            }
                        } else if (order.orderStatus == OrderStatus.OUT_FOR_DELIVERY) {
                            Button(
                                onClick = { viewModel.deliveryCompleteOrder(order.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = ArkpurGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Delivered to Customer Doorstep ✅", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Available Delivery Requests in Area
        item {
            Text(
                text = "AVAILABLE DELIVERY REQUESTS (${availableOrders.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }

        if (availableOrders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No pending delivery jobs right now. Stay online!", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(availableOrders) { order ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Delivery Pay: ₹${order.deliveryShare.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = ArkpurGreen)
                            }
                            Surface(color = Color(0xFFE0F7FA), shape = RoundedCornerShape(6.dp)) {
                                Text(text = "READY FOR PICKUP", color = Color(0xFF006064), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }

                        Text(text = "Destination: ${order.deliveryAddressText}", fontSize = 12.sp, color = TextSecondary)

                        Button(
                            onClick = { viewModel.deliveryAcceptOrder(order.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = ArkpurGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Accept Delivery Job 🛵", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Completed Deliveries History
        if (completedDeliveries.isNotEmpty()) {
            item {
                Text(
                    text = "RECENT COMPLETED DELIVERIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }

            items(completedDeliveries) { order ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceLight,
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Delivered to ${order.customerName}", fontSize = 11.sp, color = TextSecondary)
                        }
                        CurrencyText(amount = order.deliveryShare, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ArkpurGreen)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// DELIVERY EARNINGS VIEW
// ----------------------------------------------------------------------------

@Composable
fun DeliveryEarningsView(
    assignedOrders: List<OrderEntity>,
    payouts: List<DeliveryPayoutEntity>,
    viewModel: MainViewModel
) {
    val completedDeliveries = assignedOrders.filter { it.orderStatus == OrderStatus.DELIVERED }
    val tripEarnings = completedDeliveries.sumOf { it.deliveryShare }
    val paidPayouts = payouts.filter { it.status == PayoutStatus.PAID }.sumOf { it.netPayable }
    val pendingPayout = maxOf(0.0, tripEarnings + 150.0 - paidPayouts) // base + tips & incentives

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Weekly Rider Earnings Ledger", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    HorizontalDivider(color = OutlineLight)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Delivered Orders", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "${completedDeliveries.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Delivery Trip Pay", fontSize = 13.sp, color = TextSecondary)
                        CurrencyText(amount = tripEarnings, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Weekly Arkpur Incentives & Tips", fontSize = 13.sp, color = ArkpurGreen)
                        Text(text = "+₹150", fontSize = 13.sp, color = ArkpurGreen, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = OutlineLight)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Unsettled Rider Payout", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        CurrencyText(amount = pendingPayout, fontSize = 16.sp, fontWeight = FontWeight.Black, color = ArkpurOrange)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.requestDeliveryPayout() },
                        enabled = pendingPayout > 0.0,
                        colors = ButtonDefaults.buttonColors(containerColor = ArkpurGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Request Rider Payout Settlement", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Payout Records
        item {
            Text(text = "SETTLEMENT HISTORY", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
        }

        if (payouts.isEmpty()) {
            item {
                Text(text = "No payout history recorded yet.", fontSize = 12.sp, color = TextMuted)
            }
        } else {
            items(payouts) { payout ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Payout #${payout.payoutNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Surface(
                                color = if (payout.status == PayoutStatus.PAID) ArkpurGreenLight else Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = payout.status.name,
                                    color = if (payout.status == PayoutStatus.PAID) ArkpurGreenDark else ArkpurOrangeDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        CurrencyText(amount = payout.netPayable, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// DELIVERY PROFILE VIEW
// ----------------------------------------------------------------------------

@Composable
fun DeliveryProfileView(
    rider: UserEntity?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            border = BorderStroke(1.dp, OutlineLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(ArkpurGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = ArkpurGreenDark, modifier = Modifier.size(28.dp))
                    }
                    Column {
                        Text(text = rider?.name ?: "Delivery Partner", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "+91 ${rider?.mobile ?: "9777777771"}", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                HorizontalDivider(color = OutlineLight)

                Text(text = "Vehicle & Verification", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Vehicle: ${rider?.vehicleType ?: "Motorcycle"}", fontSize = 12.sp, color = TextSecondary)
                Text(text = "Registration: ${rider?.vehicleNumber ?: "BR-01-AB-1234"}", fontSize = 12.sp, color = TextSecondary)
                Text(text = "Verification: APPROVED & ACTIVE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ArkpurGreen)

                HorizontalDivider(color = OutlineLight)

                Text(text = "Payout Bank Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "A/c: 981273928192 (State Bank of India)", fontSize = 12.sp, color = TextSecondary)
                Text(text = "IFSC: SBIN0001234 • UPI: rider@upi", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}
