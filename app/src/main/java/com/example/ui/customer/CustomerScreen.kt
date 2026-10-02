package com.example.ui.customer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.common.*
import com.example.ui.theme.*

enum class CustomerSubScreen {
    HOME,
    RESTAURANT_MENU,
    CART,
    ORDER_TRACKING,
    ORDERS_LIST,
    PROFILE,
    SUPPORT
}

@Composable
fun CustomerScreen(
    viewModel: MainViewModel,
    onOpenRoleSwitcher: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subScreen by remember { mutableStateOf(CustomerSubScreen.HOME) }
    var showOtpDialog by remember { mutableStateOf(false) }

    val activeCustomer by viewModel.activeCustomer.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartCalc by viewModel.cartCalc.collectAsState()
    val selectedRestaurant by viewModel.selectedRestaurant.collectAsState()
    val selectedOrderForTracking by viewModel.selectedOrderForTracking.collectAsState()
    val notifications by viewModel.customerNotifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    // Intercept back button when inside sub-screens
    BackHandler(enabled = subScreen != CustomerSubScreen.HOME) {
        if (subScreen == CustomerSubScreen.RESTAURANT_MENU) {
            viewModel.selectRestaurant(null)
            subScreen = CustomerSubScreen.HOME
        } else if (subScreen == CustomerSubScreen.CART) {
            subScreen = if (selectedRestaurant != null) CustomerSubScreen.RESTAURANT_MENU else CustomerSubScreen.HOME
        } else {
            subScreen = CustomerSubScreen.HOME
        }
    }

    Scaffold(
        topBar = {
            ArkpurPlatformHeader(
                currentPanel = UserRole.CUSTOMER,
                unreadNotificationsCount = unreadCount,
                onOpenRoleSwitcher = onOpenRoleSwitcher,
                onOpenNotifications = onOpenNotifications
            )
        },
        bottomBar = {
            Column {
                // Floating Cart SnackBar / Bar if in Home or Menu and cart not empty
                if ((subScreen == CustomerSubScreen.HOME || subScreen == CustomerSubScreen.RESTAURANT_MENU) && cartItems.isNotEmpty()) {
                    Surface(
                        color = ArkpurGreen,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { subScreen = CustomerSubScreen.CART }
                            .testTag("floating_cart_bar")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val totalItems = cartItems.sumOf { it.quantity }
                                Text(
                                    text = "$totalItems ITEM${if (totalItems > 1) "S" else ""}",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                cartCalc?.let { calc ->
                                    CurrencyText(
                                        amount = calc.finalAmount,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "View Cart",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Customer Bottom Navigation Bar
                NavigationBar(
                    containerColor = SurfaceLight,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = subScreen == CustomerSubScreen.HOME || subScreen == CustomerSubScreen.RESTAURANT_MENU,
                        onClick = { subScreen = CustomerSubScreen.HOME },
                        icon = { Icon(Icons.Default.Restaurant, contentDescription = "Delivery") },
                        label = { Text("Delivery", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ArkpurOrange,
                            indicatorColor = ArkpurOrangeLight
                        )
                    )
                    NavigationBarItem(
                        selected = subScreen == CustomerSubScreen.CART,
                        onClick = { subScreen = CustomerSubScreen.CART },
                        icon = {
                            BadgedBox(badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge(containerColor = ArkpurOrange) {
                                        Text("${cartItems.sumOf { it.quantity }}")
                                    }
                                }
                            }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                            }
                        },
                        label = { Text("Cart", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ArkpurOrange,
                            indicatorColor = ArkpurOrangeLight
                        )
                    )
                    NavigationBarItem(
                        selected = subScreen == CustomerSubScreen.ORDERS_LIST || subScreen == CustomerSubScreen.ORDER_TRACKING,
                        onClick = { subScreen = CustomerSubScreen.ORDERS_LIST },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                        label = { Text("Orders", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ArkpurOrange,
                            indicatorColor = ArkpurOrangeLight
                        )
                    )
                    NavigationBarItem(
                        selected = subScreen == CustomerSubScreen.PROFILE || subScreen == CustomerSubScreen.SUPPORT,
                        onClick = { subScreen = CustomerSubScreen.PROFILE },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ArkpurOrange,
                            indicatorColor = ArkpurOrangeLight
                        )
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
            when (subScreen) {
                CustomerSubScreen.HOME -> {
                    CustomerHomeView(
                        viewModel = viewModel,
                        onSelectRestaurant = { rest ->
                            viewModel.selectRestaurant(rest)
                            subScreen = CustomerSubScreen.RESTAURANT_MENU
                        },
                        onOpenOtpLogin = { showOtpDialog = true }
                    )
                }
                CustomerSubScreen.RESTAURANT_MENU -> {
                    selectedRestaurant?.let { rest ->
                        CustomerRestaurantMenuView(
                            restaurant = rest,
                            viewModel = viewModel,
                            onBack = {
                                viewModel.selectRestaurant(null)
                                subScreen = CustomerSubScreen.HOME
                            },
                            onGoToCart = { subScreen = CustomerSubScreen.CART }
                        )
                    } ?: run {
                        subScreen = CustomerSubScreen.HOME
                    }
                }
                CustomerSubScreen.CART -> {
                    CustomerCartView(
                        viewModel = viewModel,
                        onBack = {
                            subScreen = if (selectedRestaurant != null) CustomerSubScreen.RESTAURANT_MENU else CustomerSubScreen.HOME
                        },
                        onOrderPlaced = { order ->
                            viewModel.trackOrder(order)
                            subScreen = CustomerSubScreen.ORDER_TRACKING
                        }
                    )
                }
                CustomerSubScreen.ORDER_TRACKING -> {
                    selectedOrderForTracking?.let { order ->
                        CustomerOrderTrackingView(
                            order = order,
                            viewModel = viewModel,
                            onBack = { subScreen = CustomerSubScreen.ORDERS_LIST },
                            onOpenSupport = { subScreen = CustomerSubScreen.SUPPORT }
                        )
                    } ?: run {
                        subScreen = CustomerSubScreen.ORDERS_LIST
                    }
                }
                CustomerSubScreen.ORDERS_LIST -> {
                    CustomerOrdersListView(
                        viewModel = viewModel,
                        onTrackOrder = { order ->
                            viewModel.trackOrder(order)
                            subScreen = CustomerSubScreen.ORDER_TRACKING
                        },
                        onStartOrdering = { subScreen = CustomerSubScreen.HOME }
                    )
                }
                CustomerSubScreen.PROFILE -> {
                    CustomerProfileView(
                        viewModel = viewModel,
                        onOpenSupport = { subScreen = CustomerSubScreen.SUPPORT },
                        onOpenOtpLogin = { showOtpDialog = true }
                    )
                }
                CustomerSubScreen.SUPPORT -> {
                    CustomerSupportView(
                        viewModel = viewModel,
                        onBack = { subScreen = CustomerSubScreen.PROFILE }
                    )
                }
            }
        }
    }

    if (showOtpDialog) {
        CustomerOtpLoginDialog(
            viewModel = viewModel,
            onDismiss = { showOtpDialog = false }
        )
    }
}

// ----------------------------------------------------------------------------
// 1. CUSTOMER HOME VIEW
// ----------------------------------------------------------------------------

@Composable
fun CustomerHomeView(
    viewModel: MainViewModel,
    onSelectRestaurant: (RestaurantEntity) -> Unit,
    onOpenOtpLogin: () -> Unit
) {
    val activeCustomer by viewModel.activeCustomer.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val restaurants by viewModel.filteredRestaurants.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val vegOnly by viewModel.vegOnlyFilter.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Location & User Header
        item {
            Surface(
                color = SurfaceLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ArkpurOrangeLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ArkpurOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Arkpur Central",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Delivering in 15-20 Mins",
                                fontSize = 11.sp,
                                color = ArkpurGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // User login status button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceVariantLight,
                        border = BorderStroke(1.dp, OutlineLight),
                        modifier = Modifier.clickable { onOpenOtpLogin() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = ArkpurOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = activeCustomer?.name?.take(10) ?: "Login",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search biryani, pizza, sweets, restaurants...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedBorderColor = ArkpurOrange,
                        unfocusedBorderColor = OutlineLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_food")
                )
            }
        }

        // Promotional Banners Carousel
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PromoBannerCard(
                        title = "50% OFF UP TO ₹100",
                        subtitle = "Use Code ARKPUR50 on your orders",
                        badge = "LIMITED TIME",
                        gradientColors = listOf(Color(0xFFFF5722), Color(0xFFFF8A65))
                    )
                }
                item {
                    PromoBannerCard(
                        title = "FOOD IN MINUTES ⚡",
                        subtitle = "Guaranteed 15-25 min rapid delivery in Arkpur",
                        badge = "SUPERFAST",
                        gradientColors = listOf(Color(0xFF00897B), Color(0xFF26A69A))
                    )
                }
                item {
                    PromoBannerCard(
                        title = "FREE DELIVERY",
                        subtitle = "On all orders above ₹199 today",
                        badge = "SPECIAL",
                        gradientColors = listOf(Color(0xFF5E35B1), Color(0xFF7E57C2))
                    )
                }
            }
        }

        // Categories Row
        item {
            Column(modifier = Modifier.padding(top = 20.dp)) {
                Text(
                    text = "WHAT'S ON YOUR MIND?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        CategoryChip(
                            name = "All Food",
                            isSelected = selectedCategory == null,
                            onClick = { viewModel.selectedCategory.value = null }
                        )
                    }
                    items(categories) { cat ->
                        CategoryChip(
                            name = cat.name,
                            isSelected = selectedCategory == cat.id,
                            onClick = {
                                viewModel.selectedCategory.value = if (selectedCategory == cat.id) null else cat.id
                            }
                        )
                    }
                }
            }
        }

        // Filter chips (Veg Only, Offers)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = vegOnly,
                    onClick = { viewModel.vegOnlyFilter.value = !vegOnly },
                    label = { Text("Pure Veg 🌿", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ArkpurGreenLight,
                        selectedLabelColor = ArkpurGreenDark
                    )
                )
            }
        }

        // Restaurants Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOP RESTAURANTS TO EXPLORE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${restaurants.size} places",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // Restaurant Cards
        if (restaurants.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No restaurants found matching your filter.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(restaurants) { restaurant ->
                RestaurantCardItem(
                    restaurant = restaurant,
                    onClick = { onSelectRestaurant(restaurant) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PromoBannerCard(
    title: String,
    subtitle: String,
    badge: String,
    gradientColors: List<Color>
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = gradientColors.first()),
        modifier = Modifier
            .width(280.dp)
            .height(115.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.25f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) ArkpurOrange else SurfaceLight,
        border = BorderStroke(1.dp, if (isSelected) ArkpurOrange else OutlineLight),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = name,
            color = if (isSelected) Color.White else TextPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun RestaurantCardItem(
    restaurant: RestaurantEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, OutlineLight),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("rest_card_${restaurant.id}")
    ) {
        Column {
            // Visual header band
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        if (restaurant.isVegOnly) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = ArkpurOrange,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = restaurant.offers,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    if (restaurant.isVegOnly) {
                        Surface(
                            color = ArkpurGreen,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "PURE VEG",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Delivery time pill overlaid bottom right
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 8.dp),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = ArkpurOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${restaurant.deliveryTimeMin} mins",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Info section
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    RatingBadge(rating = restaurant.rating)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = restaurant.tagline,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${restaurant.area} • ${restaurant.distanceKm} km",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(text = "•", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "₹${restaurant.deliveryFee.toInt()} Delivery",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 2. RESTAURANT MENU VIEW
// ----------------------------------------------------------------------------

@Composable
fun CustomerRestaurantMenuView(
    restaurant: RestaurantEntity,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onGoToCart: () -> Unit
) {
    val menuItems by viewModel.menuItemsForSelected.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top action bar
        Surface(color = SurfaceLight, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = restaurant.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "${restaurant.area} • ${restaurant.deliveryTimeMin} mins", fontSize = 11.sp, color = TextSecondary)
                }
                RatingBadge(rating = restaurant.rating, modifier = Modifier.padding(end = 8.dp))
            }
        }

        // Menu items list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Restaurant summary card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Offers for you", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Min order ₹${restaurant.minOrder.toInt()}", fontSize = 11.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "🔥 ${restaurant.offers}", color = ArkpurOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Text(
                    text = "MENU ITEMS (${menuItems.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(menuItems) { item ->
                val inCart = cartItems.find { it.menuItemId == item.id }
                MenuItemRow(
                    item = item,
                    cartQuantity = inCart?.quantity ?: 0,
                    onAddToCart = { viewModel.addToCart(item) },
                    onDecrement = { inCart?.let { viewModel.decrementOrRemoveCart(it) } }
                )
            }
        }
    }
}

@Composable
fun MenuItemRow(
    item: MenuItemEntity,
    cartQuantity: Int,
    onAddToCart: () -> Unit,
    onDecrement: () -> Unit
) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VegBadge(isVeg = item.isVeg)
                    if (item.rating >= 4.7f) {
                        Surface(
                            color = ArkpurOrangeLight,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Bestseller",
                                color = ArkpurOrangeDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CurrencyText(
                        amount = item.discountPrice ?: item.price,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (item.discountPrice != null) {
                        Text(
                            text = "₹${item.price.toInt()}",
                            fontSize = 12.sp,
                            color = TextMuted,
                            style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Add to Cart Button or Stepper
            if (cartQuantity == 0) {
                OutlinedButton(
                    onClick = onAddToCart,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.5.dp, ArkpurOrange),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ArkpurOrange),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("ADD", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ArkpurOrange,
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        IconButton(onClick = onDecrement, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrement", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text(text = "$cartQuantity", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(onClick = onAddToCart, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Increment", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 3. CART & CHECKOUT VIEW
// ----------------------------------------------------------------------------

@Composable
fun CustomerCartView(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOrderPlaced: (OrderEntity) -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val cartCalc by viewModel.cartCalc.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val addresses by viewModel.customerAddresses.collectAsState()
    val isPlacing by viewModel.isPlacingOrder.collectAsState()
    val checkoutMessage by viewModel.checkoutStateMessage.collectAsState()

    var paymentMethod by remember { mutableStateOf(PaymentMethod.ONLINE_UPI) }
    var couponInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = SurfaceLight, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(text = "My Cart & Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your cart is empty",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add delicious food from nearby restaurants",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = ArkpurOrange)
                    ) {
                        Text("Browse Food")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cart Items Section
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        border = BorderStroke(1.dp, OutlineLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Order Items", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            cartItems.forEachIndexed { index, cartItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        VegBadge(isVeg = cartItem.isVeg)
                                        Column {
                                            Text(text = cartItem.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            CurrencyText(amount = cartItem.price, fontSize = 12.sp, color = TextMuted)
                                        }
                                    }

                                    // Stepper
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, OutlineLight),
                                        color = SurfaceVariantLight
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.decrementOrRemoveCart(cartItem) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp))
                                            }
                                            Text(text = "${cartItem.quantity}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            IconButton(
                                                onClick = {
                                                    // Quick increment
                                                    viewModel.addToCart(
                                                        MenuItemEntity(
                                                            id = cartItem.menuItemId,
                                                            restaurantId = cartItem.restaurantId,
                                                            categoryId = "",
                                                            name = cartItem.name,
                                                            description = "",
                                                            price = cartItem.price,
                                                            isVeg = cartItem.isVeg
                                                        )
                                                    )
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }
                                if (index < cartItems.size - 1) {
                                    HorizontalDivider(color = OutlineLight.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }

                // Delivery Address Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        border = BorderStroke(1.dp, OutlineLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Home, contentDescription = null, tint = ArkpurOrange, modifier = Modifier.size(18.dp))
                                    Text(text = "Delivery Address", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Surface(
                                    color = ArkpurGreenLight,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "15-20 MINS",
                                        color = ArkpurGreenDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            val addr = addresses.firstOrNull()
                            if (addr != null) {
                                Text(text = "${addr.name} (+91 ${addr.mobile})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = addr.fullAddress, fontSize = 12.sp, color = TextSecondary)
                                Text(text = "Landmark: ${addr.landmark} • PIN: ${addr.pincode}", fontSize = 11.sp, color = TextMuted)
                            } else {
                                Text(text = "Station Road, Arkpur Central, PIN 800001", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                // Coupon Code Box
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        border = BorderStroke(1.dp, OutlineLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Offers & Coupons", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = couponInput,
                                    onValueChange = { couponInput = it.uppercase() },
                                    placeholder = { Text("Enter coupon code") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Button(
                                    onClick = { viewModel.applyCoupon(couponInput) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArkpurOrange),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Apply", fontWeight = FontWeight.Bold)
                                }
                            }

                            // Quick coupon suggestion chips
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SuggestionChip(
                                    onClick = {
                                        couponInput = "ARKPUR50"
                                        viewModel.applyCoupon("ARKPUR50")
                                    },
                                    label = { Text("ARKPUR50 (50% Off)", fontSize = 11.sp) }
                                )
                                SuggestionChip(
                                    onClick = {
                                        couponInput = "MINUTES20"
                                        viewModel.applyCoupon("MINUTES20")
                                    },
                                    label = { Text("MINUTES20 (₹20 Flat)", fontSize = 11.sp) }
                                )
                            }

                            cartCalc?.couponMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = msg,
                                    color = if (cartCalc?.appliedCoupon != null) ArkpurGreen else ArkpurRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Bill Details (Server-Side Calculations Display)
                item {
                    cartCalc?.let { calc ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            border = BorderStroke(1.dp, OutlineLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Bill Details (Server-Verified)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                HorizontalDivider(color = OutlineLight.copy(alpha = 0.5f))

                                BillRow("Item Subtotal", calc.subtotal)
                                BillRow("Delivery Fee", calc.deliveryFee)
                                BillRow("Platform Fee", calc.platformFee)
                                BillRow("GST & Restaurant Taxes (5%)", calc.tax)

                                if (calc.couponDiscount > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Coupon Discount (${calc.appliedCoupon?.code})", fontSize = 13.sp, color = ArkpurGreen, fontWeight = FontWeight.Bold)
                                        Text(text = "-₹${String.format(java.util.Locale.US, "%.1f", calc.couponDiscount)}", fontSize = 13.sp, color = ArkpurGreen, fontWeight = FontWeight.Bold)
                                    }
                                }

                                HorizontalDivider(color = OutlineLight)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "TO PAY", fontWeight = FontWeight.Black, fontSize = 15.sp, color = TextPrimary)
                                    CurrencyText(amount = calc.finalAmount, fontSize = 18.sp, fontWeight = FontWeight.Black, color = ArkpurOrange)
                                }
                            }
                        }
                    }
                }

                // Payment Options Section
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        border = BorderStroke(1.dp, OutlineLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = "Select Payment Option", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (paymentMethod == PaymentMethod.ONLINE_UPI) ArkpurOrange else OutlineLight),
                                color = if (paymentMethod == PaymentMethod.ONLINE_UPI) ArkpurOrangeLight.copy(alpha = 0.3f) else SurfaceLight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { paymentMethod = PaymentMethod.ONLINE_UPI }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    RadioButton(
                                        selected = paymentMethod == PaymentMethod.ONLINE_UPI,
                                        onClick = { paymentMethod = PaymentMethod.ONLINE_UPI },
                                        colors = RadioButtonDefaults.colors(selectedColor = ArkpurOrange)
                                    )
                                    Column {
                                        Text(text = "UPI / Online Payment (Recommended)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "GooglePay, PhonePe, Paytm, BHIM, Cards", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (paymentMethod == PaymentMethod.COD) ArkpurOrange else OutlineLight),
                                color = if (paymentMethod == PaymentMethod.COD) ArkpurOrangeLight.copy(alpha = 0.3f) else SurfaceLight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { paymentMethod = PaymentMethod.COD }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    RadioButton(
                                        selected = paymentMethod == PaymentMethod.COD,
                                        onClick = { paymentMethod = PaymentMethod.COD },
                                        colors = RadioButtonDefaults.colors(selectedColor = ArkpurOrange)
                                    )
                                    Column {
                                        Text(text = "Cash on Delivery (COD)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "Pay cash or UPI upon delivery at your doorstep", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Place Order Bar
            cartCalc?.let { calc ->
                Surface(
                    color = SurfaceLight,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        checkoutMessage?.let { msg ->
                            Text(text = msg, color = ArkpurRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                        }

                        Button(
                            onClick = {
                                viewModel.placeOrder(paymentMethod) { order ->
                                    onOrderPlaced(order)
                                }
                            },
                            enabled = !isPlacing,
                            colors = ButtonDefaults.buttonColors(containerColor = ArkpurOrange),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_place_order")
                        ) {
                            if (isPlacing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CurrencyText(amount = calc.finalAmount, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    Text(
                                        text = if (paymentMethod == PaymentMethod.ONLINE_UPI) "PAY & PLACE ORDER ⚡" else "PLACE ORDER (COD) ⚡",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
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
fun BillRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        CurrencyText(amount = amount, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

// ----------------------------------------------------------------------------
// 4. REAL-TIME ORDER TRACKING VIEW
// ----------------------------------------------------------------------------

@Composable
fun CustomerOrderTrackingView(
    order: OrderEntity,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenSupport: () -> Unit
) {
    val timeline by viewModel.selectedOrderHistory.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = SurfaceLight, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(text = "Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "Live Real-Time Status", fontSize = 11.sp, color = ArkpurGreen, fontWeight = FontWeight.Bold)
                    }
                }
                OrderStatusBadge(status = order.orderStatus, modifier = Modifier.padding(end = 8.dp))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (order.orderStatus == OrderStatus.DELIVERED) ArkpurGreenLight else ArkpurOrangeLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (order.orderStatus) {
                                        OrderStatus.PLACED -> Icons.Default.Schedule
                                        OrderStatus.CONFIRMED -> Icons.Default.Check
                                        OrderStatus.PREPARING -> Icons.Default.SoupKitchen
                                        OrderStatus.READY_FOR_PICKUP -> Icons.Default.Inventory2
                                        OrderStatus.DELIVERY_ASSIGNED, OrderStatus.OUT_FOR_DELIVERY -> Icons.Default.TwoWheeler
                                        OrderStatus.DELIVERED, OrderStatus.COMPLETED -> Icons.Default.CheckCircle
                                        OrderStatus.CANCELLED -> Icons.Default.Cancel
                                    },
                                    contentDescription = null,
                                    tint = if (order.orderStatus == OrderStatus.DELIVERED) ArkpurGreenDark else ArkpurOrangeDark,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = order.orderStatus.displayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (order.orderStatus == OrderStatus.DELIVERED) ArkpurGreenDark else ArkpurOrangeDark
                                )
                                Text(
                                    text = when (order.orderStatus) {
                                        OrderStatus.PLACED -> "Waiting for restaurant to confirm..."
                                        OrderStatus.CONFIRMED -> "Restaurant confirmed! Prep time: ~${order.prepTimeMinutes} mins"
                                        OrderStatus.PREPARING -> "Chef is cooking your fresh food right now"
                                        OrderStatus.READY_FOR_PICKUP -> "Food packed hot! Waiting for delivery partner"
                                        OrderStatus.DELIVERY_ASSIGNED -> "Delivery partner assigned and reaching restaurant"
                                        OrderStatus.OUT_FOR_DELIVERY -> "Delivery partner is on the way to your door 🛵"
                                        OrderStatus.DELIVERED, OrderStatus.COMPLETED -> "Delivered! Hope you loved your meal"
                                        OrderStatus.CANCELLED -> "Order has been cancelled"
                                    },
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Real-Time Steps Timeline
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Live Order Progression", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        val allSteps = listOf(
                            OrderStatus.PLACED,
                            OrderStatus.CONFIRMED,
                            OrderStatus.PREPARING,
                            OrderStatus.READY_FOR_PICKUP,
                            OrderStatus.DELIVERY_ASSIGNED,
                            OrderStatus.OUT_FOR_DELIVERY,
                            OrderStatus.DELIVERED
                        )

                        val currentIdx = allSteps.indexOf(order.orderStatus)

                        allSteps.forEachIndexed { idx, step ->
                            val isCompleted = currentIdx >= idx
                            val isCurrent = currentIdx == idx
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCompleted) ArkpurGreen else Color(0xFFE0E0E0)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    if (idx < allSteps.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(24.dp)
                                                .background(if (currentIdx > idx) ArkpurGreen else Color(0xFFE0E0E0))
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                    Text(
                                        text = step.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isCurrent) FontWeight.Black else if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isCurrent) ArkpurOrange else if (isCompleted) TextPrimary else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Order Details Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = order.itemsSummary, fontSize = 13.sp, color = TextPrimary)
                        HorizontalDivider(color = OutlineLight, modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Payment (${order.paymentMethod.displayName})", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = order.paymentStatus.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (order.paymentStatus == PaymentStatus.PAID) ArkpurGreen else ArkpurOrange
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Paid", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            CurrencyText(amount = order.finalAmount, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Action Buttons (Cancel / Support)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (order.orderStatus == OrderStatus.PLACED || order.orderStatus == OrderStatus.CONFIRMED) {
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ArkpurRed),
                            border = BorderStroke(1.dp, ArkpurRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel Order")
                        }
                    }

                    Button(
                        onClick = onOpenSupport,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantLight),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Need Help?", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Order #${order.orderNumber}?") },
            text = { Text("Are you sure? If payment was made online, an immediate refund record will be generated in the platform ledger.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelCustomerOrder(order.id, "Customer requested cancellation")
                        showCancelDialog = false
                    }
                ) {
                    Text("Yes, Cancel", color = ArkpurRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Order")
                }
            }
        )
    }
}

// ----------------------------------------------------------------------------
// 5. CUSTOMER ORDERS LIST
// ----------------------------------------------------------------------------

@Composable
fun CustomerOrdersListView(
    viewModel: MainViewModel,
    onTrackOrder: (OrderEntity) -> Unit,
    onStartOrdering: () -> Unit
) {
    val orders by viewModel.customerOrders.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = SurfaceLight, shadowElevation = 2.dp) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(text = "My Past & Active Orders", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "No orders yet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onStartOrdering, colors = ButtonDefaults.buttonColors(containerColor = ArkpurOrange)) {
                        Text("Order Now")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        border = BorderStroke(1.dp, OutlineLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTrackOrder(order) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                OrderStatusBadge(status = order.orderStatus)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = order.itemsSummary, fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CurrencyText(amount = order.finalAmount, fontSize = 15.sp, fontWeight = FontWeight.Black)
                                Text(text = "View Status & Tracking →", fontSize = 12.sp, color = ArkpurOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 6. CUSTOMER PROFILE & ADDRESSES
// ----------------------------------------------------------------------------

@Composable
fun CustomerProfileView(
    viewModel: MainViewModel,
    onOpenSupport: () -> Unit,
    onOpenOtpLogin: () -> Unit
) {
    val activeCustomer by viewModel.activeCustomer.collectAsState()
    val addresses by viewModel.customerAddresses.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            border = BorderStroke(1.dp, OutlineLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(ArkpurOrangeLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeCustomer?.name?.firstOrNull()?.toString() ?: "U",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = ArkpurOrangeDark
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeCustomer?.name ?: "Customer Profile",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "+91 ${activeCustomer?.mobile ?: "9111111111"}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = ArkpurOrange)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = OutlineLight)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onOpenOtpLogin) {
                        Text("Switch / Login Mobile", color = ArkpurOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    TextButton(onClick = onOpenSupport) {
                        Text("Help & Support", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Saved Addresses Card
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
                    Text(text = "Saved Addresses", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = { showEditProfileDialog = true }) {
                        Text("+ Add Address", color = ArkpurOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (addresses.isEmpty()) {
                    Text(text = "No saved addresses yet.", fontSize = 12.sp, color = TextMuted)
                } else {
                    addresses.forEach { addr ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceVariantLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "${addr.type} - ${addr.villageArea}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (addr.isDefault) {
                                        Surface(color = ArkpurGreen, shape = RoundedCornerShape(4.dp)) {
                                            Text(text = "DEFAULT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text(text = addr.fullAddress, fontSize = 12.sp, color = TextSecondary)
                                Text(text = "Landmark: ${addr.landmark} • PIN: ${addr.pincode}", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditProfileDialog) {
        CustomerEditProfileDialog(
            viewModel = viewModel,
            onDismiss = { showEditProfileDialog = false }
        )
    }
}

@Composable
fun CustomerEditProfileDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val activeCustomer by viewModel.activeCustomer.collectAsState()
    var name by remember { mutableStateOf(activeCustomer?.name ?: "") }
    var email by remember { mutableStateOf(activeCustomer?.email ?: "") }
    var area by remember { mutableStateOf("Station Road") }
    var fullAddress by remember { mutableStateOf("Flat 201, Shanti Niketan Apartment") }
    var landmark by remember { mutableStateOf("Near Railway Station") }
    var pin by remember { mutableStateOf("800001") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profile & Delivery Address") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (Optional)") }, singleLine = true)
                OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Village / Area") }, singleLine = true)
                OutlinedTextField(value = fullAddress, onValueChange = { fullAddress = it }, label = { Text("Full Address") }, singleLine = true)
                OutlinedTextField(value = landmark, onValueChange = { landmark = it }, label = { Text("Landmark") }, singleLine = true)
                OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("PIN Code") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updateProfile(name, email, area, fullAddress, landmark, pin)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArkpurOrange)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ----------------------------------------------------------------------------
// 7. CUSTOMER SUPPORT & COMPLAINTS
// ----------------------------------------------------------------------------

@Composable
fun CustomerSupportView(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(TicketCategory.FOOD_QUALITY) }
    var description by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = SurfaceLight, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(text = "Customer Support & Complaints", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Direct contact cards
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArkpurGreenLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Need Instant Assistance?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ArkpurGreenDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Direct WhatsApp & Call Support for Arkpur food delivery", fontSize = 12.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ArkpurGreen,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "WhatsApp: +91 98765 43210",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Ticket Submission Form
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, OutlineLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Submit a Support Ticket / Issue", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        Text(text = "Select Complaint Category:", fontSize = 12.sp, color = TextSecondary)
                        TicketCategory.values().forEach { cat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCategory = cat }
                            ) {
                                RadioButton(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    colors = RadioButtonDefaults.colors(selectedColor = ArkpurOrange)
                                )
                                Text(text = cat.displayName, fontSize = 13.sp)
                            }
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Describe the issue in detail") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (isSubmitted) {
                            Text(
                                text = "Ticket submitted successfully! Our Arkpur manager will call you shortly.",
                                color = ArkpurGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                if (description.isNotBlank()) {
                                    viewModel.submitSupportTicket(null, selectedCategory, description) {
                                        isSubmitted = true
                                        description = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArkpurOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Complaint", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
