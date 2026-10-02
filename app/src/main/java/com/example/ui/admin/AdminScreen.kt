package com.example.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.common.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class AdminTab {
    DASHBOARD,
    ORDERS,
    RESTAURANTS,
    DELIVERY_FLEET,
    LEDGER,
    PAYOUTS,
    COUPONS_SETTINGS,
    COMPLAINTS
}

@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    onOpenRoleSwitcher: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdminLoggedIn by viewModel.adminLoggedIn.collectAsState()

    if (!isAdminLoggedIn) {
        AdminLoginView(
            viewModel = viewModel,
            onOpenRoleSwitcher = onOpenRoleSwitcher
        )
    } else {
        AdminMainControlView(
            viewModel = viewModel,
            onOpenRoleSwitcher = onOpenRoleSwitcher,
            onOpenNotifications = onOpenNotifications,
            modifier = modifier
        )
    }
}

// ----------------------------------------------------------------------------
// 1. SECURE ADMIN LOGIN VIEW
// ----------------------------------------------------------------------------

@Composable
fun AdminLoginView(
    viewModel: MainViewModel,
    onOpenRoleSwitcher: () -> Unit
) {
    var adminId by remember { mutableStateOf("admin@arkpur.food") }
    var password by remember { mutableStateOf("arkpur2026") }
    var pin2FA by remember { mutableStateOf("1234") }
    val loginError by viewModel.adminLoginError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            border = BorderStroke(1.dp, OutlineLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFECEFF1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color(0xFF37474F),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ARKPUR CENTRAL ADMIN",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Secure Platform Command & Financial Ledger",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                HorizontalDivider(color = OutlineLight)

                // Demo credentials hint card
                Surface(
                    color = Color(0xFFECEFF1),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "🛡️ Demo Admin Credentials Pre-filled:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                        Text(text = "ID: admin@arkpur.food • Pass: arkpur2026 • 2FA: 1234", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                OutlinedTextField(
                    value = adminId,
                    onValueChange = { adminId = it },
                    label = { Text("Admin ID / Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Admin Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pin2FA,
                    onValueChange = { pin2FA = it },
                    label = { Text("2FA Security PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                loginError?.let { err ->
                    Text(text = err, color = ArkpurRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.adminLogin(adminId, password, pin2FA) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Authenticate & Enter Admin Console", fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onOpenRoleSwitcher) {
                    Text("Return to Other Portals", color = ArkpurOrange, fontSize = 12.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 2. ADMIN MAIN CONSOLE
// ----------------------------------------------------------------------------

@Composable
fun AdminMainControlView(
    viewModel: MainViewModel,
    onOpenRoleSwitcher: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(AdminTab.DASHBOARD) }

    val allOrders by viewModel.adminAllOrders.collectAsState()
    val allRestaurants by viewModel.allRestaurants.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val ledgerEntries by viewModel.adminLedgerEntries.collectAsState()
    val restPayouts by viewModel.adminRestaurantPayouts.collectAsState()
    val delivPayouts by viewModel.adminDeliveryPayouts.collectAsState()
    val coupons by viewModel.adminCoupons.collectAsState()
    val settings by viewModel.adminSettings.collectAsState()
    val tickets by viewModel.adminTickets.collectAsState()

    Scaffold(
        topBar = {
            Column {
                ArkpurPlatformHeader(
                    currentPanel = UserRole.ADMIN,
                    unreadNotificationsCount = 0,
                    onOpenRoleSwitcher = onOpenRoleSwitcher,
                    onOpenNotifications = onOpenNotifications
                )

                // Admin Action & Logout Bar
                Surface(
                    color = Color(0xFF263238),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ArkpurGreen, modifier = Modifier.size(16.dp))
                            Text(text = "Superadmin Session ACTIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { viewModel.adminLogout() }) {
                            Text("Logout Admin", color = Color(0xFFFF8A65), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Horizontal Navigation Pills
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = SurfaceLight,
                    edgePadding = 12.dp
                ) {
                    Tab(
                        selected = selectedTab == AdminTab.DASHBOARD,
                        onClick = { selectedTab = AdminTab.DASHBOARD },
                        text = { Text("Dashboard") }
                    )
                    Tab(
                        selected = selectedTab == AdminTab.ORDERS,
                        onClick = { selectedTab = AdminTab.ORDERS },
                        text = { Text("Orders (${allOrders.size})") }
                    )
                    Tab(
                        selected = selectedTab == AdminTab.LEDGER,
                        onClick = { selectedTab = AdminTab.LEDGER },
                        text = { Text("Financial Ledger") }
                    )
                    Tab(
                        selected = selectedTab == AdminTab.PAYOUTS,
                        onClick = { selectedTab = AdminTab.PAYOUTS },
                        text = {
                            val pending = restPayouts.count { it.status == PayoutStatus.REQUESTED } + delivPayouts.count { it.status == PayoutStatus.REQUESTED }
                            Text("Payout Approvals ${if (pending > 0) "($pending)" else ""}")
                        }
                    )
                    Tab(
                        selected = selectedTab == AdminTab.RESTAURANTS,
                        onClick = { selectedTab = AdminTab.RESTAURANTS },
                        text = { Text("Restaurants (${allRestaurants.size})") }
                    )
                    Tab(
                        selected = selectedTab == AdminTab.DELIVERY_FLEET,
                        onClick = { selectedTab = AdminTab.DELIVERY_FLEET },
                        text = { Text("Delivery Fleet") }
                    )
                    Tab(
                        selected = selectedTab == AdminTab.COUPONS_SETTINGS,
                        onClick = { selectedTab = AdminTab.COUPONS_SETTINGS },
                        text = { Text("Coupons & Settings") }
                    )
                    Tab(
                        selected = selectedTab == AdminTab.COMPLAINTS,
                        onClick = { selectedTab = AdminTab.COMPLAINTS },
                        text = { Text("Complaints (${tickets.size})") }
                    )
                }
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
                AdminTab.DASHBOARD -> AdminDashboardOverview(
                    allOrders = allOrders,
                    allRestaurants = allRestaurants,
                    allUsers = allUsers,
                    ledgerEntries = ledgerEntries
                )
                AdminTab.ORDERS -> AdminOrderManagementView(
                    allOrders = allOrders,
                    allUsers = allUsers,
                    viewModel = viewModel
                )
                AdminTab.LEDGER -> AdminLedgerView(
                    ledgerEntries = ledgerEntries
                )
                AdminTab.PAYOUTS -> AdminPayoutsApprovalView(
                    restPayouts = restPayouts,
                    delivPayouts = delivPayouts,
                    viewModel = viewModel
                )
                AdminTab.RESTAURANTS -> AdminRestaurantsManagementView(
                    restaurants = allRestaurants,
                    viewModel = viewModel
                )
                AdminTab.DELIVERY_FLEET -> AdminDeliveryFleetView(
                    allUsers = allUsers
                )
                AdminTab.COUPONS_SETTINGS -> AdminCouponsSettingsView(
                    coupons = coupons,
                    settings = settings,
                    viewModel = viewModel
                )
                AdminTab.COMPLAINTS -> AdminComplaintsView(
                    tickets = tickets
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// DASHBOARD OVERVIEW
// ----------------------------------------------------------------------------

@Composable
fun AdminDashboardOverview(
    allOrders: List<OrderEntity>,
    allRestaurants: List<RestaurantEntity>,
    allUsers: List<UserEntity>,
    ledgerEntries: List<LedgerEntryEntity>
) {
    val customers = allUsers.filter { it.role == UserRole.CUSTOMER }
    val riders = allUsers.filter { it.role == UserRole.DELIVERY }

    val deliveredOrders = allOrders.filter { it.orderStatus == OrderStatus.DELIVERED }
    val totalRevenue = deliveredOrders.sumOf { it.finalAmount }
    val totalPlatformCommission = deliveredOrders.sumOf { it.platformCommission }
    val totalRestaurantPayable = deliveredOrders.sumOf { it.restaurantShare }
    val totalDeliveryPayable = deliveredOrders.sumOf { it.deliveryShare }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "PLATFORM METRICS & FINANCIALS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricStatCard(
                        title = "Gross Merchandise Value (GMV)",
                        value = "₹${totalRevenue.toInt()}",
                        icon = Icons.Default.CurrencyRupee,
                        tint = ArkpurGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Net Platform Commission",
                        value = "₹${totalPlatformCommission.toInt()}",
                        icon = Icons.Default.Savings,
                        tint = ArkpurOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricStatCard(
                        title = "Restaurant Disbursements",
                        value = "₹${totalRestaurantPayable.toInt()}",
                        icon = Icons.Default.Storefront,
                        tint = Color(0xFF512DA8),
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Rider Fleet Disbursements",
                        value = "₹${totalDeliveryPayable.toInt()}",
                        icon = Icons.Default.TwoWheeler,
                        tint = Color(0xFF00838F),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricStatCard(
                        title = "Total Orders Placed",
                        value = "${allOrders.size}",
                        icon = Icons.Default.ReceiptLong,
                        tint = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Delivery Fleet Active",
                        value = "${riders.size} Riders",
                        icon = Icons.Default.Groups,
                        tint = ArkpurGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live System Integrity Badge
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ArkpurGreenLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = ArkpurGreenDark, modifier = Modifier.size(28.dp))
                    Column {
                        Text(text = "Immutable Financial Ledger Active", fontWeight = FontWeight.Black, fontSize = 14.sp, color = ArkpurGreenDark)
                        Text(text = "${ledgerEntries.size} verified immutable financial transactions logged", fontSize = 12.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// ORDERS CONTROL
// ----------------------------------------------------------------------------

@Composable
fun AdminOrderManagementView(
    allOrders: List<OrderEntity>,
    allUsers: List<UserEntity>,
    viewModel: MainViewModel
) {
    val riders = allUsers.filter { it.role == UserRole.DELIVERY }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(allOrders) { order ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        OrderStatusBadge(status = order.orderStatus)
                    }

                    Text(text = "Customer: ${order.customerName} (+91 ${order.customerMobile})", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "Items: ${order.itemsSummary}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Deliver To: ${order.deliveryAddressText}", fontSize = 11.sp, color = TextMuted)

                    HorizontalDivider(color = OutlineLight)

                    // Financial breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Paid: ₹${order.finalAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "Comm: ₹${order.platformCommission.toInt()} • Rest: ₹${order.restaurantShare.toInt()} • Rider: ₹${order.deliveryShare.toInt()}", fontSize = 11.sp, color = TextSecondary)
                    }

                    // Admin Actions (Assign Rider / Cancel)
                    if (order.orderStatus != OrderStatus.DELIVERED && order.orderStatus != OrderStatus.CANCELLED) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (riders.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        viewModel.adminAssignRider(order.id, riders.first().id)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArkpurGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Assign ${riders.first().name}", fontSize = 11.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.adminCancelOrder(order.id, "Admin intervention")
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ArkpurRed),
                                border = BorderStroke(1.dp, ArkpurRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancel", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FINANCIAL LEDGER VIEW
// ----------------------------------------------------------------------------

@Composable
fun AdminLedgerView(
    ledgerEntries: List<LedgerEntryEntity>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = SurfaceLight, shadowElevation = 1.dp) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Immutable Financial Ledger (${ledgerEntries.size} Entries)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        if (ledgerEntries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No ledger transactions logged yet.", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ledgerEntries) { entry ->
                    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(entry.timestamp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        border = BorderStroke(1.dp, OutlineLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (entry.isCredit) ArkpurGreenLight else ArkpurOrangeLight,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${entry.type.name} • ${if (entry.isCredit) "CREDIT (+)" else "DEBIT (-)"}",
                                        color = if (entry.isCredit) ArkpurGreenDark else ArkpurOrangeDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                CurrencyText(
                                    amount = entry.amount,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (entry.isCredit) ArkpurGreen else TextPrimary
                                )
                            }

                            Text(text = entry.description, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Entity: ${entry.entityType} • Ref: ${entry.referenceId}", fontSize = 10.sp, color = TextSecondary)
                                Text(text = dateStr, fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// PAYOUT APPROVALS VIEW
// ----------------------------------------------------------------------------

@Composable
fun AdminPayoutsApprovalView(
    restPayouts: List<RestaurantPayoutEntity>,
    delivPayouts: List<DeliveryPayoutEntity>,
    viewModel: MainViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Restaurant Payouts Section
        item {
            Text(
                text = "RESTAURANT PAYOUT REQUESTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }

        if (restPayouts.isEmpty()) {
            item {
                Text(text = "No restaurant payouts requested.", fontSize = 12.sp, color = TextMuted)
            }
        } else {
            items(restPayouts) { payout ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${payout.restaurantName} (#${payout.payoutNumber})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(
                                color = if (payout.status == PayoutStatus.PAID) ArkpurGreenLight else ArkpurOrangeLight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = payout.status.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (payout.status == PayoutStatus.PAID) ArkpurGreenDark else ArkpurOrangeDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(text = "Completed: ${payout.totalOrders} orders • Gross: ₹${payout.grossEarnings.toInt()} • Commission: ₹${payout.commissionDeductions.toInt()}", fontSize = 11.sp, color = TextSecondary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Net Payable:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            CurrencyText(amount = payout.netPayable, fontSize = 15.sp, fontWeight = FontWeight.Black, color = ArkpurGreen)
                        }

                        if (payout.status == PayoutStatus.REQUESTED) {
                            Button(
                                onClick = {
                                    val ref = "BANK-NEFT-" + java.util.Random().nextInt(100000, 999999)
                                    viewModel.adminApproveRestaurantPayout(payout.id, ref)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ArkpurGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Approve & Settle Payout ✅", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Delivery Payouts Section
        item {
            Text(
                text = "DELIVERY PARTNER PAYOUT REQUESTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        if (delivPayouts.isEmpty()) {
            item {
                Text(text = "No rider payouts requested.", fontSize = 12.sp, color = TextMuted)
            }
        } else {
            items(delivPayouts) { payout ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${payout.partnerName} (#${payout.payoutNumber})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(
                                color = if (payout.status == PayoutStatus.PAID) ArkpurGreenLight else ArkpurOrangeLight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = payout.status.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (payout.status == PayoutStatus.PAID) ArkpurGreenDark else ArkpurOrangeDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(text = "Trips: ${payout.totalDeliveries} • Base: ₹${payout.deliveryEarnings.toInt()} • Tips & Incentives: ₹${payout.tips + payout.incentives}", fontSize = 11.sp, color = TextSecondary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Net Payable:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            CurrencyText(amount = payout.netPayable, fontSize = 15.sp, fontWeight = FontWeight.Black, color = ArkpurGreen)
                        }

                        if (payout.status == PayoutStatus.REQUESTED) {
                            Button(
                                onClick = {
                                    val ref = "UPI-SETTLE-" + java.util.Random().nextInt(100000, 999999)
                                    viewModel.adminApproveDeliveryPayout(payout.id, ref)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ArkpurGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Approve & Disburse Rider Pay ✅", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// RESTAURANTS MANAGEMENT
// ----------------------------------------------------------------------------

@Composable
fun AdminRestaurantsManagementView(
    restaurants: List<RestaurantEntity>,
    viewModel: MainViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(restaurants) { rest ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = rest.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "${rest.area} • Commission: ${rest.commissionRatePercent.toInt()}%", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = "Status: ${rest.status}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rest.status == "ACTIVE") ArkpurGreen else ArkpurRed
                        )
                    }

                    Button(
                        onClick = { viewModel.adminToggleRestaurantStatus(rest) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (rest.status == "ACTIVE") ArkpurRed else ArkpurGreen
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (rest.status == "ACTIVE") "Suspend" else "Activate", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// DELIVERY FLEET
// ----------------------------------------------------------------------------

@Composable
fun AdminDeliveryFleetView(
    allUsers: List<UserEntity>
) {
    val riders = allUsers.filter { it.role == UserRole.DELIVERY }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(riders) { rider ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = rider.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "+91 ${rider.mobile} • ${rider.vehicleType}", fontSize = 12.sp, color = TextSecondary)
                        Text(text = "Reg No: ${rider.vehicleNumber}", fontSize = 11.sp, color = TextMuted)
                    }
                    Surface(
                        color = if (rider.isOnline) ArkpurGreenLight else Color(0xFFECEFF1),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (rider.isOnline) "ONLINE" else "OFFLINE",
                            color = if (rider.isOnline) ArkpurGreenDark else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COUPONS & SETTINGS
// ----------------------------------------------------------------------------

@Composable
fun AdminCouponsSettingsView(
    coupons: List<CouponEntity>,
    settings: AdminSettingsEntity?,
    viewModel: MainViewModel
) {
    var newCode by remember { mutableStateOf("") }
    var newDiscountStr by remember { mutableStateOf("") }
    var newMinOrderStr by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Platform Config
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Global Platform Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    HorizontalDivider(color = OutlineLight)
                    Text(text = "Platform Fee: ₹${settings?.platformFee?.toInt() ?: 5}", fontSize = 12.sp)
                    Text(text = "Base Delivery Fee: ₹${settings?.baseDeliveryFee?.toInt() ?: 25}", fontSize = 12.sp)
                    Text(text = "GST / Food Tax: ${settings?.taxRatePercent?.toInt() ?: 5}%", fontSize = 12.sp)
                    Text(text = "Default Restaurant Commission: ${settings?.defaultRestaurantCommissionPercent?.toInt() ?: 15}%", fontSize = 12.sp)
                    Text(text = "Support Hotline: ${settings?.supportPhone ?: "+91 98765 43210"}", fontSize = 12.sp)
                }
            }
        }

        // Active Coupons
        item {
            Text(text = "ACTIVE DISCOUNT COUPONS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
        }

        items(coupons) { coupon ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = coupon.code, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ArkpurOrange)
                        Text(text = coupon.description, fontSize = 11.sp, color = TextSecondary)
                        Text(text = "Min Order: ₹${coupon.minOrderAmount.toInt()}", fontSize = 10.sp, color = TextMuted)
                    }
                    Surface(color = ArkpurGreenLight, shape = RoundedCornerShape(4.dp)) {
                        Text(text = "ACTIVE", color = ArkpurGreenDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPLAINTS VIEW
// ----------------------------------------------------------------------------

@Composable
fun AdminComplaintsView(
    tickets: List<SupportTicketEntity>
) {
    if (tickets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No customer complaints registered.", color = TextMuted)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tickets) { ticket ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${ticket.category.displayName} (#${ticket.ticketNumber})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(4.dp)) {
                                Text(text = ticket.status.name, color = ArkpurOrangeDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Text(text = "Customer: ${ticket.customerName} (+91 ${ticket.customerMobile})", fontSize = 11.sp, color = TextSecondary)
                        Text(text = "Issue: ${ticket.description}", fontSize = 12.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}
