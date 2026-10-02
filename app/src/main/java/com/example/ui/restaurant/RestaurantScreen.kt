package com.example.ui.restaurant

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

enum class RestaurantTab {
    DASHBOARD,
    ORDERS,
    MENU,
    PAYOUTS
}

@Composable
fun RestaurantScreen(
    viewModel: MainViewModel,
    onOpenRoleSwitcher: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(RestaurantTab.DASHBOARD) }

    val activeRestaurant by viewModel.activeRestaurant.collectAsState()
    val allRestaurants by viewModel.allRestaurants.collectAsState()
    val orders by viewModel.restaurantOrders.collectAsState()
    val menuItems by viewModel.restaurantMenuItems.collectAsState()
    val payouts by viewModel.restaurantPayouts.collectAsState()
    val notifications by viewModel.customerNotifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    var showSwitchRestaurantDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                ArkpurPlatformHeader(
                    currentPanel = UserRole.RESTAURANT,
                    unreadNotificationsCount = unreadCount,
                    onOpenRoleSwitcher = onOpenRoleSwitcher,
                    onOpenNotifications = onOpenNotifications
                )

                // Active Restaurant Banner & Quick Selector
                Surface(
                    color = Color(0xFFF3E5F5),
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF512DA8), modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = activeRestaurant?.name ?: "Restaurant Partner",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF311B92)
                                )
                                Text(
                                    text = "${activeRestaurant?.area} • Commission: ${activeRestaurant?.commissionRatePercent?.toInt()}%",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        TextButton(onClick = { showSwitchRestaurantDialog = true }) {
                            Text("Switch Outlet", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF512DA8))
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceLight, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = selectedTab == RestaurantTab.DASHBOARD,
                    onClick = { selectedTab = RestaurantTab.DASHBOARD },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Overview", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF512DA8), indicatorColor = Color(0xFFEDE7F6))
                )
                NavigationBarItem(
                    selected = selectedTab == RestaurantTab.ORDERS,
                    onClick = { selectedTab = RestaurantTab.ORDERS },
                    icon = {
                        val pendingCount = orders.count { it.orderStatus == OrderStatus.PLACED || it.orderStatus == OrderStatus.CONFIRMED || it.orderStatus == OrderStatus.PREPARING }
                        BadgedBox(badge = {
                            if (pendingCount > 0) {
                                Badge(containerColor = ArkpurOrange) { Text("$pendingCount") }
                            }
                        }) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = "Orders")
                        }
                    },
                    label = { Text("Orders", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF512DA8), indicatorColor = Color(0xFFEDE7F6))
                )
                NavigationBarItem(
                    selected = selectedTab == RestaurantTab.MENU,
                    onClick = { selectedTab = RestaurantTab.MENU },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") },
                    label = { Text("Menu (${menuItems.size})", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF512DA8), indicatorColor = Color(0xFFEDE7F6))
                )
                NavigationBarItem(
                    selected = selectedTab == RestaurantTab.PAYOUTS,
                    onClick = { selectedTab = RestaurantTab.PAYOUTS },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Payouts") },
                    label = { Text("Payouts", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF512DA8), indicatorColor = Color(0xFFEDE7F6))
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
                RestaurantTab.DASHBOARD -> {
                    RestaurantDashboardView(
                        restaurant = activeRestaurant,
                        orders = orders,
                        payouts = payouts,
                        onViewOrders = { selectedTab = RestaurantTab.ORDERS },
                        onRequestPayout = { selectedTab = RestaurantTab.PAYOUTS }
                    )
                }
                RestaurantTab.ORDERS -> {
                    RestaurantOrdersView(
                        orders = orders,
                        viewModel = viewModel
                    )
                }
                RestaurantTab.MENU -> {
                    RestaurantMenuView(
                        restaurant = activeRestaurant,
                        menuItems = menuItems,
                        viewModel = viewModel
                    )
                }
                RestaurantTab.PAYOUTS -> {
                    RestaurantPayoutsView(
                        restaurant = activeRestaurant,
                        orders = orders,
                        payouts = payouts,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    if (showSwitchRestaurantDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchRestaurantDialog = false },
            title = { Text("Select Restaurant Outlet") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allRestaurants) { rest ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (rest.id == activeRestaurant?.id) Color(0xFFEDE7F6) else SurfaceVariantLight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setActiveRestaurant(rest.id)
                                    showSwitchRestaurantDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = rest.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = rest.area, fontSize = 11.sp, color = TextSecondary)
                                }
                                if (rest.id == activeRestaurant?.id) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF512DA8))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSwitchRestaurantDialog = false }) { Text("Close") }
            }
        )
    }
}

// ----------------------------------------------------------------------------
// RESTAURANT DASHBOARD
// ----------------------------------------------------------------------------

@Composable
fun RestaurantDashboardView(
    restaurant: RestaurantEntity?,
    orders: List<OrderEntity>,
    payouts: List<RestaurantPayoutEntity>,
    onViewOrders: () -> Unit,
    onRequestPayout: () -> Unit
) {
    val totalOrders = orders.size
    val pendingOrders = orders.count { it.orderStatus == OrderStatus.PLACED || it.orderStatus == OrderStatus.CONFIRMED || it.orderStatus == OrderStatus.PREPARING }
    val completedOrders = orders.count { it.orderStatus == OrderStatus.DELIVERED }
    val cancelledOrders = orders.count { it.orderStatus == OrderStatus.CANCELLED }

    val grossSales = orders.filter { it.orderStatus == OrderStatus.DELIVERED }.sumOf { it.subtotal }
    val netEarnings = orders.filter { it.orderStatus == OrderStatus.DELIVERED }.sumOf { it.restaurantShare }
    val paidPayouts = payouts.filter { it.status == PayoutStatus.PAID }.sumOf { it.netPayable }
    val pendingPayout = maxOf(0.0, netEarnings - paidPayouts)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Alert Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (restaurant?.isOpen == true) ArkpurGreenLight else Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (restaurant?.isOpen == true) "Accepting Orders 🟢" else "Currently Closed 🔴",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = if (restaurant?.isOpen == true) ArkpurGreenDark else ArkpurRed
                        )
                        Text(
                            text = "Avg delivery: ${restaurant?.deliveryTimeMin ?: 20} mins • Min order: ₹${restaurant?.minOrder?.toInt() ?: 99}",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }
                    if (pendingOrders > 0) {
                        Surface(
                            color = ArkpurOrange,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable { onViewOrders() }
                        ) {
                            Text(
                                text = "$pendingOrders ACTION NEEDED",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Metrics Grid (2x2)
        item {
            Text(text = "BUSINESS OVERVIEW", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricStatCard(
                        title = "Gross Food Sales",
                        value = "₹${grossSales.toInt()}",
                        icon = Icons.Default.TrendingUp,
                        tint = ArkpurGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Net Earnings",
                        value = "₹${netEarnings.toInt()}",
                        icon = Icons.Default.AccountBalanceWallet,
                        tint = Color(0xFF512DA8),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricStatCard(
                        title = "Pending Orders",
                        value = "$pendingOrders",
                        icon = Icons.Default.PendingActions,
                        tint = ArkpurOrange,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Completed Deliveries",
                        value = "$completedOrders",
                        icon = Icons.Default.CheckCircle,
                        tint = ArkpurGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Payout Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Weekly Payout Settlement", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    HorizontalDivider(color = OutlineLight)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Unsettled Net Earnings", fontSize = 13.sp, color = TextSecondary)
                        CurrencyText(amount = pendingPayout, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ArkpurOrange)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Settled / Paid Payouts", fontSize = 13.sp, color = TextSecondary)
                        CurrencyText(amount = paidPayouts, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ArkpurGreen)
                    }

                    Button(
                        onClick = onRequestPayout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF512DA8)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Payout Details & Request Settlement")
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// RESTAURANT ORDERS (STATE MACHINE WORKFLOW)
// ----------------------------------------------------------------------------

@Composable
fun RestaurantOrdersView(
    orders: List<OrderEntity>,
    viewModel: MainViewModel
) {
    var filterStatus by remember { mutableStateOf<OrderStatus?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter tabs
        ScrollableTabRow(
            selectedTabIndex = if (filterStatus == null) 0 else 1,
            containerColor = SurfaceLight,
            edgePadding = 16.dp
        ) {
            Tab(
                selected = filterStatus == null,
                onClick = { filterStatus = null },
                text = { Text("All (${orders.size})") }
            )
            Tab(
                selected = filterStatus == OrderStatus.PLACED,
                onClick = { filterStatus = OrderStatus.PLACED },
                text = { Text("New Orders (${orders.count { it.orderStatus == OrderStatus.PLACED }})") }
            )
            Tab(
                selected = filterStatus == OrderStatus.PREPARING,
                onClick = { filterStatus = OrderStatus.PREPARING },
                text = { Text("In Kitchen (${orders.count { it.orderStatus == OrderStatus.CONFIRMED || it.orderStatus == OrderStatus.PREPARING }})") }
            )
            Tab(
                selected = filterStatus == OrderStatus.DELIVERED,
                onClick = { filterStatus = OrderStatus.DELIVERED },
                text = { Text("Delivered (${orders.count { it.orderStatus == OrderStatus.DELIVERED }})") }
            )
        }

        val filtered = orders.filter { ord ->
            filterStatus == null || ord.orderStatus == filterStatus
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No orders found in this category.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filtered) { order ->
                    RestaurantOrderManageCard(order = order, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun RestaurantOrderManageCard(
    order: OrderEntity,
    viewModel: MainViewModel
) {
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
                    Text(text = "Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "Customer: ${order.customerName} • +91 ${order.customerMobile}", fontSize = 11.sp, color = TextSecondary)
                }
                OrderStatusBadge(status = order.orderStatus)
            }

            Surface(shape = RoundedCornerShape(8.dp), color = SurfaceVariantLight, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = order.itemsSummary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Deliver to: ${order.deliveryAddressText}", fontSize = 11.sp, color = TextSecondary)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Order Total: ₹${order.finalAmount.toInt()} (${order.paymentMethod.displayName})", fontSize = 12.sp, color = TextSecondary)
                Text(text = "Net Earning: ₹${order.restaurantShare.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ArkpurGreen)
            }

            HorizontalDivider(color = OutlineLight)

            // Dynamic Action Buttons based on order lifecycle
            when (order.orderStatus) {
                OrderStatus.PLACED -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.restaurantAcceptOrder(order.id, 15) },
                            colors = ButtonDefaults.buttonColors(containerColor = ArkpurGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept (15 Mins)")
                        }
                        Button(
                            onClick = { viewModel.restaurantAcceptOrder(order.id, 25) },
                            colors = ButtonDefaults.buttonColors(containerColor = ArkpurOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept (25 Mins)")
                        }
                    }
                }
                OrderStatus.CONFIRMED -> {
                    Button(
                        onClick = { viewModel.restaurantStartPreparing(order.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = ArkpurOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Preparing in Kitchen 👨‍🍳")
                    }
                }
                OrderStatus.PREPARING -> {
                    Button(
                        onClick = { viewModel.restaurantMarkReady(order.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark Ready for Pickup 📦")
                    }
                }
                OrderStatus.READY_FOR_PICKUP -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0F7FA),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Waiting for delivery partner to arrive & pickup parcel...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006064),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                OrderStatus.DELIVERY_ASSIGNED, OrderStatus.OUT_FOR_DELIVERY -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ArkpurOrangeLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Rider on the road delivering parcel to customer address.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArkpurOrangeDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                OrderStatus.DELIVERED -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ArkpurGreenLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✅ Order delivered successfully! Earnings added to weekly ledger.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArkpurGreenDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

// ----------------------------------------------------------------------------
// RESTAURANT MENU MANAGEMENT
// ----------------------------------------------------------------------------

@Composable
fun RestaurantMenuView(
    restaurant: RestaurantEntity?,
    menuItems: List<MenuItemEntity>,
    viewModel: MainViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = SurfaceLight, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Manage Dishes & Stock", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ArkpurOrange),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ Add Dish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(menuItems) { item ->
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            VegBadge(isVeg = item.isVeg)
                            Column {
                                Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CurrencyText(amount = item.discountPrice ?: item.price, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    if (item.discountPrice != null) {
                                        Text(text = "₹${item.price.toInt()}", fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                            }
                        }

                        // Availability Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (item.isAvailable) "In Stock" else "Sold Out",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isAvailable) ArkpurGreen else ArkpurRed
                            )
                            Switch(
                                checked = item.isAvailable,
                                onCheckedChange = { viewModel.toggleMenuItemAvailability(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog && restaurant != null) {
        RestaurantAddDishDialog(
            restaurantId = restaurant.id,
            onDismiss = { showAddDialog = false },
            onSave = { dish ->
                viewModel.addOrUpdateMenuItem(dish)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RestaurantAddDishDialog(
    restaurantId: String,
    onDismiss: () -> Unit,
    onSave: (MenuItemEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var discountPriceStr by remember { mutableStateOf("") }
    var isVeg by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Food Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Price (₹)") }, singleLine = true)
                OutlinedTextField(value = discountPriceStr, onValueChange = { discountPriceStr = it }, label = { Text("Discount Price (Optional)") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isVeg, onCheckedChange = { isVeg = it })
                    Text("Pure Veg Item")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 100.0
                    val disc = discountPriceStr.toDoubleOrNull()
                    val newItem = MenuItemEntity(
                        id = "m_" + java.util.UUID.randomUUID().toString().take(8),
                        restaurantId = restaurantId,
                        categoryId = "cat_fastfood",
                        name = name.ifBlank { "Special Dish" },
                        description = desc.ifBlank { "Freshly prepared in Arkpur" },
                        price = price,
                        discountPrice = disc,
                        isVeg = isVeg
                    )
                    onSave(newItem)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArkpurOrange)
            ) {
                Text("Add Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ----------------------------------------------------------------------------
// RESTAURANT PAYOUTS VIEW
// ----------------------------------------------------------------------------

@Composable
fun RestaurantPayoutsView(
    restaurant: RestaurantEntity?,
    orders: List<OrderEntity>,
    payouts: List<RestaurantPayoutEntity>,
    viewModel: MainViewModel
) {
    val completedOrders = orders.filter { it.orderStatus == OrderStatus.DELIVERED }
    val grossSales = completedOrders.sumOf { it.subtotal }
    val netEarnings = completedOrders.sumOf { it.restaurantShare }
    val paidPayouts = payouts.filter { it.status == PayoutStatus.PAID }.sumOf { it.netPayable }
    val pendingPayout = maxOf(0.0, netEarnings - paidPayouts)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Payout Calculation Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Weekly Settlement Ledger", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    HorizontalDivider(color = OutlineLight)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Completed Orders", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "${completedOrders.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Gross Completed Sales", fontSize = 13.sp, color = TextSecondary)
                        CurrencyText(amount = grossSales, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Platform Commission (${restaurant?.commissionRatePercent?.toInt()}%)", fontSize = 13.sp, color = ArkpurRed)
                        Text(text = "-₹${(grossSales - netEarnings).toInt()}", fontSize = 13.sp, color = ArkpurRed, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = OutlineLight)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Unsettled Balance", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        CurrencyText(amount = pendingPayout, fontSize = 16.sp, fontWeight = FontWeight.Black, color = ArkpurOrange)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.requestRestaurantPayout() },
                        enabled = pendingPayout > 0.0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF512DA8)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Request Weekly Payout Settlement", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Bank / UPI details
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Verified Payout Bank Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Account Holder: ${restaurant?.bankHolderName ?: "Partner"}", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "Bank A/c: ${restaurant?.bankAccount ?: "918234567890"}", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "IFSC: ${restaurant?.bankIfsc ?: "SBIN0001234"} • UPI: ${restaurant?.upiId ?: "rest@upi"}", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        // Payout Request History
        item {
            Text(text = "PAYOUT TRANSACTIONS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
        }

        if (payouts.isEmpty()) {
            item {
                Text(text = "No payout requests recorded yet.", fontSize = 12.sp, color = TextMuted)
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
                        if (payout.referenceId.isNotBlank()) {
                            Text(text = "Bank Ref: ${payout.referenceId}", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}
