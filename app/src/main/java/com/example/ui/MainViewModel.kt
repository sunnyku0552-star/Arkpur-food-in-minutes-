package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ArkpurDatabase
import com.example.data.model.*
import com.example.data.repository.ArkpurRepository
import com.example.data.repository.CartCalculation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository: ArkpurRepository
    private val db = ArkpurDatabase.getInstance(application)

    init {
        repository = ArkpurRepository(db)
        viewModelScope.launch {
            // Ensure initial database seed is loaded
            ArkpurDatabase.seedInitialData(db)
        }
    }

    // Role / Panel Selection
    private val _currentPanel = MutableStateFlow(UserRole.CUSTOMER)
    val currentPanel: StateFlow<UserRole> = _currentPanel.asStateFlow()

    fun switchPanel(role: UserRole) {
        _currentPanel.value = role
    }

    // ----------------------------------------------------
    // AUTH & USERS
    // ----------------------------------------------------

    val allUsers = repository.userDao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Customer (default to Sunny Kumar)
    private val _activeCustomerId = MutableStateFlow("cust_sunny")
    val activeCustomer: StateFlow<UserEntity?> = _activeCustomerId
        .flatMapLatest { id -> repository.userDao.getUserById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Restaurant (default to Arkpur Biryani)
    private val _activeRestaurantId = MutableStateFlow("rest_biryani_mahal")
    val activeRestaurant: StateFlow<RestaurantEntity?> = _activeRestaurantId
        .flatMapLatest { id -> repository.restaurantDao.getRestaurantById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setActiveRestaurant(restId: String) {
        _activeRestaurantId.value = restId
    }

    // Active Delivery Partner (default to Rahul Kumar)
    private val _activeDeliveryPartnerId = MutableStateFlow("rider_rahul")
    val activeDeliveryPartner: StateFlow<UserEntity?> = _activeDeliveryPartnerId
        .flatMapLatest { id -> repository.userDao.getUserById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setActiveDeliveryPartner(riderId: String) {
        _activeDeliveryPartnerId.value = riderId
    }

    // Admin Auth State
    private val _adminLoggedIn = MutableStateFlow(false)
    val adminLoggedIn: StateFlow<Boolean> = _adminLoggedIn.asStateFlow()
    val adminLoginError = MutableStateFlow<String?>(null)

    fun adminLogin(id: String, pass: String, pin2FA: String) {
        if ((id.trim().equals("admin@arkpur.food", ignoreCase = true) || id.trim() == "admin") &&
            pass == "arkpur2026" && pin2FA == "1234"
        ) {
            _adminLoggedIn.value = true
            adminLoginError.value = null
        } else {
            adminLoginError.value = "गलत क्रेडेंशियल्स (Invalid Admin ID, Password or 2FA PIN)"
        }
    }

    fun adminLogout() {
        _adminLoggedIn.value = false
    }

    // ----------------------------------------------------
    // CUSTOMER OTP FLOW
    // ----------------------------------------------------

    val otpMobile = MutableStateFlow("9111111111")
    val otpCode = MutableStateFlow("")
    val otpSent = MutableStateFlow(false)
    val otpTimerSeconds = MutableStateFlow(0)
    val otpErrorMessage = MutableStateFlow<String?>(null)
    val otpGeneratedDisplay = MutableStateFlow<String?>(null)
    val isOtpLoading = MutableStateFlow(false)
    private var timerJob: Job? = null

    fun sendOtp() {
        viewModelScope.launch {
            isOtpLoading.value = true
            otpErrorMessage.value = null
            val result = repository.requestOtp(otpMobile.value)
            isOtpLoading.value = false
            result.onSuccess { code ->
                otpSent.value = true
                otpGeneratedDisplay.value = code
                startOtpTimer()
            }.onFailure { err ->
                otpErrorMessage.value = err.message
            }
        }
    }

    private fun startOtpTimer() {
        timerJob?.cancel()
        otpTimerSeconds.value = 30
        timerJob = viewModelScope.launch {
            while (otpTimerSeconds.value > 0) {
                delay(1000)
                otpTimerSeconds.value -= 1
            }
        }
    }

    fun verifyOtp(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isOtpLoading.value = true
            otpErrorMessage.value = null
            val result = repository.verifyOtp(otpMobile.value, otpCode.value)
            isOtpLoading.value = false
            result.onSuccess { user ->
                _activeCustomerId.value = user.id
                otpSent.value = false
                otpCode.value = ""
                otpGeneratedDisplay.value = null
                onSuccess()
            }.onFailure { err ->
                otpErrorMessage.value = err.message
            }
        }
    }

    fun updateProfile(name: String, email: String, area: String, address: String, landmark: String, pin: String) {
        viewModelScope.launch {
            repository.registerCustomerProfile(
                _activeCustomerId.value,
                name, email, area, address, landmark, pin, "Home"
            )
        }
    }

    // ----------------------------------------------------
    // CUSTOMER BROWSING & SEARCH
    // ----------------------------------------------------

    val categories = repository.categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRestaurants = repository.restaurantDao.getAllRestaurants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCategory = MutableStateFlow<String?>(null)
    val searchQuery = MutableStateFlow("")
    val vegOnlyFilter = MutableStateFlow(false)

    val filteredRestaurants = combine(
        allRestaurants,
        selectedCategory,
        searchQuery,
        vegOnlyFilter
    ) { list, cat, query, vegOnly ->
        list.filter { rest ->
            val matchesQuery = query.isBlank() ||
                    rest.name.contains(query, ignoreCase = true) ||
                    rest.tagline.contains(query, ignoreCase = true) ||
                    rest.area.contains(query, ignoreCase = true)
            val matchesVeg = !vegOnly || rest.isVegOnly
            matchesQuery && matchesVeg
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Restaurant for menu browsing
    val selectedRestaurant = MutableStateFlow<RestaurantEntity?>(null)

    val menuItemsForSelected = selectedRestaurant.flatMapLatest { rest ->
        if (rest == null) flowOf(emptyList())
        else repository.menuItemDao.getMenuItemsForRestaurant(rest.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectRestaurant(restaurant: RestaurantEntity?) {
        selectedRestaurant.value = restaurant
    }

    // ----------------------------------------------------
    // CART & CHECKOUT
    // ----------------------------------------------------

    val cartItems = _activeCustomerId.flatMapLatest { custId ->
        repository.cartDao.getCartForCustomer(custId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appliedCoupon = MutableStateFlow<String?>("ARKPUR50")
    val cartCalc = MutableStateFlow<CartCalculation?>(null)

    init {
        // Automatically re-calculate cart whenever cart items or coupon changes
        viewModelScope.launch {
            combine(cartItems, appliedCoupon) { items, coupon ->
                if (items.isEmpty()) {
                    cartCalc.value = null
                } else {
                    cartCalc.value = repository.calculateCart(_activeCustomerId.value, coupon)
                }
            }.collect()
        }
    }

    fun addToCart(item: MenuItemEntity) {
        viewModelScope.launch {
            val existing = cartItems.value.find { it.menuItemId == item.id }
            if (existing != null) {
                repository.cartDao.updateCartItem(existing.copy(quantity = existing.quantity + 1))
            } else {
                // If cart has items from another restaurant, clear old restaurant items to keep single-restaurant delivery
                val currentCart = cartItems.value
                if (currentCart.isNotEmpty() && currentCart.first().restaurantId != item.restaurantId) {
                    repository.cartDao.clearCart(_activeCustomerId.value)
                }
                val newItem = CartItemEntity(
                    id = "cart_" + UUID.randomUUID().toString().take(8),
                    customerId = _activeCustomerId.value,
                    restaurantId = item.restaurantId,
                    menuItemId = item.id,
                    name = item.name,
                    price = item.discountPrice ?: item.price,
                    quantity = 1,
                    isVeg = item.isVeg
                )
                repository.cartDao.insertCartItem(newItem)
            }
        }
    }

    fun decrementOrRemoveCart(cartItem: CartItemEntity) {
        viewModelScope.launch {
            if (cartItem.quantity > 1) {
                repository.cartDao.updateCartItem(cartItem.copy(quantity = cartItem.quantity - 1))
            } else {
                repository.cartDao.deleteCartItem(cartItem.id)
            }
        }
    }

    fun applyCoupon(code: String) {
        appliedCoupon.value = code.trim().uppercase()
        viewModelScope.launch {
            cartCalc.value = repository.calculateCart(_activeCustomerId.value, appliedCoupon.value)
        }
    }

    val customerAddresses = _activeCustomerId.flatMapLatest { custId ->
        repository.addressDao.getAddressesForCustomer(custId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checkoutStateMessage = MutableStateFlow<String?>(null)
    val isPlacingOrder = MutableStateFlow(false)

    fun placeOrder(paymentMethod: PaymentMethod, onPlaced: (OrderEntity) -> Unit) {
        viewModelScope.launch {
            isPlacingOrder.value = true
            checkoutStateMessage.value = null
            val addresses = customerAddresses.value
            val addrId = addresses.firstOrNull()?.id ?: ""
            val result = repository.placeOrder(
                customerId = _activeCustomerId.value,
                addressId = addrId,
                paymentMethod = paymentMethod,
                couponCode = appliedCoupon.value
            )
            isPlacingOrder.value = false
            result.onSuccess { ord ->
                _selectedOrderForTracking.value = ord
                onPlaced(ord)
            }.onFailure { err ->
                checkoutStateMessage.value = err.message
            }
        }
    }

    // ----------------------------------------------------
    // ORDER TRACKING & HISTORY
    // ----------------------------------------------------

    val customerOrders = _activeCustomerId.flatMapLatest { custId ->
        repository.orderDao.getOrdersForCustomer(custId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedOrderForTracking = MutableStateFlow<OrderEntity?>(null)
    val selectedOrderForTracking: StateFlow<OrderEntity?> = _selectedOrderForTracking
        .flatMapLatest { ord ->
            if (ord == null) flowOf(null)
            else repository.orderDao.getOrderById(ord.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun trackOrder(order: OrderEntity) {
        _selectedOrderForTracking.value = order
    }

    val selectedOrderHistory = _selectedOrderForTracking.flatMapLatest { ord ->
        if (ord == null) flowOf(emptyList())
        else repository.orderDao.getOrderHistory(ord.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cancelCustomerOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId, reason, UserRole.CUSTOMER)
        }
    }

    // ----------------------------------------------------
    // RESTAURANT PARTNER PANEL
    // ----------------------------------------------------

    val restaurantOrders = _activeRestaurantId.flatMapLatest { restId ->
        repository.orderDao.getOrdersForRestaurant(restId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val restaurantMenuItems = _activeRestaurantId.flatMapLatest { restId ->
        repository.menuItemDao.getMenuItemsForRestaurant(restId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val restaurantPayouts = _activeRestaurantId.flatMapLatest { restId ->
        repository.payoutDao.getPayoutsForRestaurant(restId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restaurantAcceptOrder(orderId: String, prepMinutes: Int) {
        viewModelScope.launch {
            repository.restaurantAcceptOrder(orderId, prepMinutes)
        }
    }

    fun restaurantStartPreparing(orderId: String) {
        viewModelScope.launch {
            repository.restaurantStartPreparing(orderId)
        }
    }

    fun restaurantMarkReady(orderId: String) {
        viewModelScope.launch {
            repository.restaurantMarkReady(orderId)
        }
    }

    fun toggleMenuItemAvailability(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.menuItemDao.updateAvailability(item.id, !item.isAvailable)
        }
    }

    fun addOrUpdateMenuItem(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.menuItemDao.insertMenuItem(item)
        }
    }

    fun requestRestaurantPayout() {
        viewModelScope.launch {
            repository.requestRestaurantWeeklyPayout(_activeRestaurantId.value)
        }
    }

    // ----------------------------------------------------
    // DELIVERY PARTNER PANEL
    // ----------------------------------------------------

    val deliveryAvailableOrders = repository.orderDao.getAvailableOrdersForDelivery()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliveryAssignedOrders = _activeDeliveryPartnerId.flatMapLatest { riderId ->
        repository.orderDao.getOrdersForDeliveryPartner(riderId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliveryPayouts = _activeDeliveryPartnerId.flatMapLatest { riderId ->
        repository.payoutDao.getPayoutsForDeliveryPartner(riderId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleDeliveryOnlineStatus() {
        viewModelScope.launch {
            val partner = activeDeliveryPartner.value ?: return@launch
            repository.userDao.updateOnlineStatus(partner.id, !partner.isOnline)
        }
    }

    fun deliveryAcceptOrder(orderId: String) {
        viewModelScope.launch {
            repository.deliveryPartnerAcceptOrder(orderId, _activeDeliveryPartnerId.value)
        }
    }

    fun deliveryPickupOrder(orderId: String) {
        viewModelScope.launch {
            repository.deliveryPartnerPickup(orderId)
        }
    }

    fun deliveryCompleteOrder(orderId: String) {
        viewModelScope.launch {
            repository.deliveryPartnerDeliver(orderId)
        }
    }

    fun requestDeliveryPayout() {
        viewModelScope.launch {
            repository.requestDeliveryWeeklyPayout(_activeDeliveryPartnerId.value)
        }
    }

    // ----------------------------------------------------
    // ADMIN PANEL
    // ----------------------------------------------------

    val adminAllOrders = repository.orderDao.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminLedgerEntries = repository.ledgerDao.getAllLedgerEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminPayments = repository.paymentDao.getAllPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminRestaurantPayouts = repository.payoutDao.getAllRestaurantPayouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminDeliveryPayouts = repository.payoutDao.getAllDeliveryPayouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminSettings = repository.settingsDao.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adminCoupons = repository.couponDao.getAllCoupons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminTickets = repository.supportDao.getAllTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun adminApproveRestaurantPayout(payoutId: String, refId: String) {
        viewModelScope.launch {
            repository.adminSettleRestaurantPayout(payoutId, refId, "Approved and transferred via Bank/UPI")
        }
    }

    fun adminApproveDeliveryPayout(payoutId: String, refId: String) {
        viewModelScope.launch {
            repository.adminSettleDeliveryPayout(payoutId, refId, "Rider earnings disbursed successfully")
        }
    }

    fun adminAssignRider(orderId: String, riderId: String) {
        viewModelScope.launch {
            repository.deliveryPartnerAcceptOrder(orderId, riderId)
        }
    }

    fun adminCancelOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId, reason, UserRole.ADMIN)
        }
    }

    fun adminToggleRestaurantStatus(restaurant: RestaurantEntity) {
        viewModelScope.launch {
            val newStatus = if (restaurant.status == "ACTIVE") "SUSPENDED" else "ACTIVE"
            repository.restaurantDao.updateStatus(restaurant.id, newStatus)
        }
    }

    fun adminUpdateSettings(settings: AdminSettingsEntity) {
        viewModelScope.launch {
            repository.settingsDao.insertOrUpdate(settings)
        }
    }

    fun adminCreateCoupon(coupon: CouponEntity) {
        viewModelScope.launch {
            repository.couponDao.insertCoupon(coupon)
        }
    }

    // ----------------------------------------------------
    // NOTIFICATIONS
    // ----------------------------------------------------

    val customerNotifications = combine(_activeCustomerId, _currentPanel) { custId, role ->
        Pair(custId, role)
    }.flatMapLatest { (id, role) ->
        repository.notificationDao.getNotificationsForUser(role, id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.notificationDao.markAsRead(id)
        }
    }

    // ----------------------------------------------------
    // SUPPORT
    // ----------------------------------------------------

    fun submitSupportTicket(orderId: String?, category: TicketCategory, desc: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.createSupportTicket(_activeCustomerId.value, orderId, category, desc)
            onDone()
        }
    }
}
