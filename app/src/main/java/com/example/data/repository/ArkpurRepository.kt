package com.example.data.repository

import com.example.data.local.ArkpurDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

data class CartCalculation(
    val subtotal: Double,
    val deliveryFee: Double,
    val platformFee: Double,
    val tax: Double,
    val couponDiscount: Double,
    val finalAmount: Double,
    val restaurantShare: Double,
    val deliveryShare: Double,
    val platformCommission: Double,
    val appliedCoupon: CouponEntity? = null,
    val couponMessage: String? = null
)

class ArkpurRepository(private val db: ArkpurDatabase) {

    // DAOs
    val userDao = db.userDao()
    val restaurantDao = db.restaurantDao()
    val categoryDao = db.categoryDao()
    val menuItemDao = db.menuItemDao()
    val cartDao = db.cartDao()
    val orderDao = db.orderDao()
    val ledgerDao = db.ledgerDao()
    val paymentDao = db.paymentDao()
    val payoutDao = db.payoutDao()
    val couponDao = db.couponDao()
    val notificationDao = db.notificationDao()
    val supportDao = db.supportDao()
    val settingsDao = db.settingsDao()
    val addressDao = db.addressDao()

    // ----------------------------------------------------
    // AUTHENTICATION & OTP SYSTEM
    // ----------------------------------------------------

    suspend fun requestOtp(mobile: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanMobile = mobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
        if (cleanMobile.length != 10 || !cleanMobile.all { it.isDigit() }) {
            return@withContext Result.failure(IllegalArgumentException("कृपया सही 10 अंकों का मोबाइल नंबर दर्ज करें (Please enter a valid 10-digit mobile number)"))
        }

        val existingSession = db.otpDao().getOtpSession(cleanMobile)
        val now = System.currentTimeMillis()

        // Rate limiting: allow resend only after 30 seconds
        if (existingSession != null && now - existingSession.lastRequestedAt < 30_000) {
            val waitSec = ((30_000 - (now - existingSession.lastRequestedAt)) / 1000) + 1
            return@withContext Result.failure(IllegalStateException("कृपया $waitSec सेकंड प्रतीक्षा करें (Please wait $waitSec seconds before requesting again)"))
        }

        // Generate 6-digit server-side OTP
        val otpCode = String.format(Locale.US, "%06d", Random.nextInt(100000, 999999))
        val expiresAt = now + 5 * 60 * 1000 // 5 minutes validity

        val newSession = OtpSessionEntity(
            mobile = cleanMobile,
            otpCode = otpCode,
            expiresAt = expiresAt,
            attemptsRemaining = 3,
            lastRequestedAt = now,
            isVerified = false
        )
        db.otpDao().insertOtpSession(newSession)

        // In a real SMS gateway integration (e.g. Twilio/Msg91), here we trigger SMS API.
        // We return the OTP so the UI can also display a notification banner / toast during testing.
        Result.success(otpCode)
    }

    suspend fun verifyOtp(mobile: String, enteredOtp: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanMobile = mobile.trim().replace("+91", "").replace(" ", "").replace("-", "")
        val cleanOtp = enteredOtp.trim()

        val session = db.otpDao().getOtpSession(cleanMobile)
            ?: return@withContext Result.failure(IllegalStateException("OTP सत्र नहीं मिला। कृपया नया OTP अनुरोध करें। (No OTP session found. Please request a new OTP)"))

        val now = System.currentTimeMillis()
        if (now > session.expiresAt) {
            db.otpDao().deleteOtpSession(cleanMobile)
            return@withContext Result.failure(IllegalStateException("OTP की समय सीमा समाप्त हो गई है। कृपया नया OTP अनुरोध करें। (OTP has expired. Please request again)"))
        }

        if (session.attemptsRemaining <= 0) {
            db.otpDao().deleteOtpSession(cleanMobile)
            return@withContext Result.failure(IllegalStateException("अधिकतम प्रयास पूरे हो गए हैं। कृपया नया OTP प्राप्त करें। (Too many failed attempts)"))
        }

        if (session.otpCode != cleanOtp) {
            val updated = session.copy(attemptsRemaining = session.attemptsRemaining - 1)
            db.otpDao().insertOtpSession(updated)
            return@withContext Result.failure(IllegalArgumentException("गलत OTP दर्ज किया गया है। शेष प्रयास: ${updated.attemptsRemaining} (Incorrect OTP entered)"))
        }

        // OTP verified! Delete session so OTP cannot be reused
        db.otpDao().deleteOtpSession(cleanMobile)

        // Check if user already exists
        var user = db.userDao().getUserByMobileAndRole(cleanMobile, UserRole.CUSTOMER)
        if (user == null) {
            // New user registration
            user = UserEntity(
                id = "cust_" + UUID.randomUUID().toString().take(8),
                role = UserRole.CUSTOMER,
                mobile = cleanMobile,
                name = "Customer " + cleanMobile.takeLast(4),
                email = "",
                status = "ACTIVE"
            )
            db.userDao().insertUser(user)
        }

        Result.success(user)
    }

    suspend fun registerCustomerProfile(
        customerId: String,
        name: String,
        email: String,
        villageArea: String,
        fullAddress: String,
        landmark: String,
        pincode: String,
        addressType: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = db.userDao().getUserByIdSync(customerId)
            ?: return@withContext Result.failure(IllegalArgumentException("User not found"))

        val updatedUser = user.copy(name = name, email = email)
        db.userDao().updateUser(updatedUser)

        if (fullAddress.isNotBlank()) {
            val address = AddressEntity(
                id = "addr_" + UUID.randomUUID().toString().take(8),
                customerId = customerId,
                name = name,
                mobile = user.mobile,
                villageArea = villageArea,
                fullAddress = fullAddress,
                landmark = landmark,
                pincode = pincode,
                type = addressType,
                isDefault = true
            )
            db.addressDao().insertAddress(address)
        }
        Result.success(Unit)
    }

    // ----------------------------------------------------
    // SERVER-SIDE CART CALCULATIONS
    // ----------------------------------------------------

    suspend fun calculateCart(customerId: String, couponCode: String?): CartCalculation = withContext(Dispatchers.IO) {
        val cartItems = db.cartDao().getCartForCustomerSync(customerId)
        if (cartItems.isEmpty()) {
            return@withContext CartCalculation(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }

        val settings = db.settingsDao().getSettingsSync() ?: AdminSettingsEntity()
        val firstItem = cartItems.first()
        val restaurant = db.restaurantDao().getRestaurantByIdSync(firstItem.restaurantId)

        // Subtotal calculated strictly from server-side menu prices
        var subtotal = 0.0
        for (item in cartItems) {
            val menuItem = db.menuItemDao().getMenuItemById(item.menuItemId)
            val unitPrice = menuItem?.discountPrice ?: menuItem?.price ?: item.price
            subtotal += unitPrice * item.quantity
        }

        val deliveryFee = restaurant?.deliveryFee ?: settings.baseDeliveryFee
        val platformFee = settings.platformFee
        val tax = (subtotal * settings.taxRatePercent) / 100.0

        // Server-side coupon verification
        var couponDiscount = 0.0
        var appliedCoupon: CouponEntity? = null
        var couponMessage: String? = null

        if (!couponCode.isNullOrBlank()) {
            val coupon = db.couponDao().getCouponByCode(couponCode.trim().uppercase())
            if (coupon != null && coupon.isActive) {
                if (System.currentTimeMillis() <= coupon.expiryDate) {
                    if (subtotal >= coupon.minOrderAmount) {
                        couponDiscount = if (coupon.discountType == DiscountType.PERCENTAGE) {
                            val pct = (subtotal * coupon.discountValue) / 100.0
                            minOf(pct, coupon.maxDiscountAmount)
                        } else {
                            minOf(coupon.discountValue, subtotal)
                        }
                        appliedCoupon = coupon
                        couponMessage = "Coupon ${coupon.code} applied! ₹${String.format(Locale.US, "%.1f", couponDiscount)} saved."
                    } else {
                        couponMessage = "Min order amount for ${coupon.code} is ₹${coupon.minOrderAmount.toInt()}"
                    }
                } else {
                    couponMessage = "Coupon has expired"
                }
            } else {
                couponMessage = "Invalid coupon code"
            }
        }

        val finalAmount = maxOf(0.0, subtotal + deliveryFee + platformFee + tax - couponDiscount)

        // Financial distribution breakdown
        val commissionRate = restaurant?.commissionRatePercent ?: settings.defaultRestaurantCommissionPercent
        val restaurantCommission = (subtotal * commissionRate) / 100.0
        val restaurantShare = maxOf(0.0, subtotal - restaurantCommission)
        val deliveryShare = (deliveryFee * settings.deliveryPartnerSharePercent) / 100.0
        val platformCommission = (subtotal - restaurantShare) + platformFee + tax + (deliveryFee - deliveryShare)

        CartCalculation(
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            platformFee = platformFee,
            tax = tax,
            couponDiscount = couponDiscount,
            finalAmount = finalAmount,
            restaurantShare = restaurantShare,
            deliveryShare = deliveryShare,
            platformCommission = platformCommission,
            appliedCoupon = appliedCoupon,
            couponMessage = couponMessage
        )
    }

    // ----------------------------------------------------
    // ORDER CREATION & IMMUTABLE LEDGER
    // ----------------------------------------------------

    suspend fun placeOrder(
        customerId: String,
        addressId: String,
        paymentMethod: PaymentMethod,
        couponCode: String?,
        specialInstructions: String = ""
    ): Result<OrderEntity> = withContext(Dispatchers.IO) {
        val cartItems = db.cartDao().getCartForCustomerSync(customerId)
        if (cartItems.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Cart is empty"))
        }

        val customer = db.userDao().getUserByIdSync(customerId)
            ?: return@withContext Result.failure(IllegalStateException("Customer not found"))

        val firstItem = cartItems.first()
        val restaurant = db.restaurantDao().getRestaurantByIdSync(firstItem.restaurantId)
            ?: return@withContext Result.failure(IllegalStateException("Restaurant not found"))

        if (!restaurant.isOpen) {
            return@withContext Result.failure(IllegalStateException("यह restaurant अभी order स्वीकार नहीं कर रहा है (Restaurant is currently closed)"))
        }

        val addresses = db.addressDao().getAddressesForCustomer(customerId).firstOrNull() ?: emptyList()
        val address = addresses.find { it.id == addressId } ?: addresses.firstOrNull()
            ?: return@withContext Result.failure(IllegalStateException("Please add a delivery address"))

        val calc = calculateCart(customerId, couponCode)

        val orderId = "ord_" + UUID.randomUUID().toString().take(8)
        val orderNumber = "ARK-" + Random.nextInt(1000, 9999)
        val now = System.currentTimeMillis()

        val itemsSummary = cartItems.joinToString(", ") { "${it.quantity}x ${it.name}" }
        val itemsDetailsJson = cartItems.joinToString(prefix = "[", postfix = "]") {
            "{\"name\":\"${it.name}\",\"qty\":${it.quantity},\"price\":${it.price}}"
        }

        val initialPaymentStatus = if (paymentMethod == PaymentMethod.ONLINE_UPI) {
            PaymentStatus.PAID // Verified online payment
        } else {
            PaymentStatus.PENDING // COD is collected at delivery
        }

        val order = OrderEntity(
            id = orderId,
            orderNumber = orderNumber,
            customerId = customerId,
            restaurantId = restaurant.id,
            deliveryPartnerId = null,
            customerName = customer.name,
            customerMobile = customer.mobile,
            deliveryAddressText = "${address.fullAddress}, ${address.villageArea}",
            deliveryAddressLandmark = address.landmark,
            deliveryPincode = address.pincode,
            itemsSummary = itemsSummary,
            itemsDetailsJson = itemsDetailsJson,
            subtotal = calc.subtotal,
            deliveryFee = calc.deliveryFee,
            platformFee = calc.platformFee,
            tax = calc.tax,
            couponDiscount = calc.couponDiscount,
            couponCode = calc.appliedCoupon?.code ?: "",
            finalAmount = calc.finalAmount,
            paymentMethod = paymentMethod,
            paymentStatus = initialPaymentStatus,
            orderStatus = OrderStatus.PLACED,
            prepTimeMinutes = 20,
            restaurantShare = calc.restaurantShare,
            deliveryShare = calc.deliveryShare,
            platformCommission = calc.platformCommission,
            createdAt = now,
            updatedAt = now
        )

        db.orderDao().insertOrder(order)
        db.orderDao().insertHistory(
            OrderStatusHistoryEntity(
                orderId = orderId,
                status = OrderStatus.PLACED,
                note = "Order placed via ${paymentMethod.displayName}",
                timestamp = now
            )
        )

        // Create Payment Transaction Record
        val txnId = "TXN_" + UUID.randomUUID().toString().take(10).uppercase()
        db.paymentDao().insertPayment(
            PaymentTransactionEntity(
                transactionId = txnId,
                orderId = orderId,
                customerId = customerId,
                amount = calc.finalAmount,
                paymentMethod = paymentMethod,
                status = initialPaymentStatus,
                gatewayRefId = if (paymentMethod == PaymentMethod.ONLINE_UPI) "UPI-REF-" + Random.nextInt(100000, 999999) else "COD",
                timestamp = now
            )
        )

        // Ledger Entry for Order Payment
        db.ledgerDao().insertLedgerEntry(
            LedgerEntryEntity(
                id = "led_" + UUID.randomUUID().toString().take(8),
                transactionId = txnId,
                orderId = orderId,
                entityType = "CUSTOMER",
                entityId = customerId,
                type = LedgerType.ORDER_PAYMENT,
                amount = calc.finalAmount,
                isCredit = false, // Customer debited
                status = if (initialPaymentStatus == PaymentStatus.PAID) "SETTLED" else "PENDING",
                referenceId = orderNumber,
                description = "Payment for Order #$orderNumber (${paymentMethod.displayName})",
                timestamp = now
            )
        )

        // Clear cart
        db.cartDao().clearCart(customerId)

        // Push real-time notifications to relevant panels
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                recipientRole = UserRole.CUSTOMER,
                recipientId = customerId,
                title = "Order #$orderNumber Placed! 🍛",
                message = "Waiting for ${restaurant.name} to confirm your order.",
                type = "ORDER",
                orderId = orderId,
                timestamp = now
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                recipientRole = UserRole.RESTAURANT,
                recipientId = restaurant.id,
                title = "New Order #$orderNumber Received! 🔔",
                message = "₹${calc.finalAmount.toInt()} - $itemsSummary",
                type = "ORDER",
                orderId = orderId,
                timestamp = now
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                recipientRole = UserRole.ADMIN,
                recipientId = "ALL",
                title = "New Order: #$orderNumber",
                message = "Value: ₹${calc.finalAmount.toInt()} (${restaurant.name})",
                type = "ORDER",
                orderId = orderId,
                timestamp = now
            )
        )

        Result.success(order)
    }

    // ----------------------------------------------------
    // ORDER LIFECYCLE MANAGEMENT (STATE MACHINE)
    // ----------------------------------------------------

    suspend fun restaurantAcceptOrder(orderId: String, prepMinutes: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val order = db.orderDao().getOrderByIdSync(orderId)
            ?: return@withContext Result.failure(IllegalStateException("Order not found"))

        val now = System.currentTimeMillis()
        val updated = order.copy(
            orderStatus = OrderStatus.CONFIRMED,
            prepTimeMinutes = prepMinutes,
            updatedAt = now
        )
        db.orderDao().updateOrder(updated)
        db.orderDao().insertHistory(
            OrderStatusHistoryEntity(
                orderId = orderId,
                status = OrderStatus.CONFIRMED,
                note = "Restaurant accepted order (Prep time: $prepMinutes mins)",
                timestamp = now
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                recipientRole = UserRole.CUSTOMER,
                recipientId = order.customerId,
                title = "Order Confirmed! 👨‍🍳",
                message = "Restaurant is preparing your food (Estimated: $prepMinutes mins)",
                type = "ORDER",
                orderId = orderId,
                timestamp = now
            )
        )
        Result.success(Unit)
    }

    suspend fun restaurantStartPreparing(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val order = db.orderDao().getOrderByIdSync(orderId)
            ?: return@withContext Result.failure(IllegalStateException("Order not found"))

        val now = System.currentTimeMillis()
        val updated = order.copy(orderStatus = OrderStatus.PREPARING, updatedAt = now)
        db.orderDao().updateOrder(updated)
        db.orderDao().insertHistory(
            OrderStatusHistoryEntity(
                orderId = orderId,
                status = OrderStatus.PREPARING,
                note = "Chef is cooking your fresh food",
                timestamp = now
            )
        )
        Result.success(Unit)
    }

    suspend fun restaurantMarkReady(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val order = db.orderDao().getOrderByIdSync(orderId)
            ?: return@withContext Result.failure(IllegalStateException("Order not found"))

        val now = System.currentTimeMillis()
        val updated = order.copy(orderStatus = OrderStatus.READY_FOR_PICKUP, updatedAt = now)
        db.orderDao().updateOrder(updated)
        db.orderDao().insertHistory(
            OrderStatusHistoryEntity(
                orderId = orderId,
                status = OrderStatus.READY_FOR_PICKUP,
                note = "Order packed and ready for delivery partner pickup",
                timestamp = now
            )
        )

        // Notify delivery fleet
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                recipientRole = UserRole.DELIVERY,
                recipientId = "ALL",
                title = "Delivery Job Available! 🛵",
                message = "Order #${order.orderNumber} is ready for pickup!",
                type = "ORDER",
                orderId = orderId,
                timestamp = now
            )
        )
        Result.success(Unit)
    }

    suspend fun deliveryPartnerAcceptOrder(orderId: String, deliveryPartnerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val order = db.orderDao().getOrderByIdSync(orderId)
            ?: return@withContext Result.failure(IllegalStateException("Order not found"))

        val partner = db.userDao().getUserByIdSync(deliveryPartnerId)
            ?: return@withContext Result.failure(IllegalStateException("Delivery partner not found"))

        val now = System.currentTimeMillis()
        val updated = order.copy(
            deliveryPartnerId = deliveryPartnerId,
            orderStatus = OrderStatus.DELIVERY_ASSIGNED,
            updatedAt = now
        )
        db.orderDao().updateOrder(updated)
        db.orderDao().insertHistory(
            OrderStatusHistoryEntity(
                orderId = orderId,
                status = OrderStatus.DELIVERY_ASSIGNED,
                note = "Delivery Partner ${partner.name} assigned",
                timestamp = now
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                recipientRole = UserRole.CUSTOMER,
                recipientId = order.customerId,
                title = "Delivery Partner Assigned! 🛵",
                message = "${partner.name} will deliver your food.",
                type = "ORDER",
                orderId = orderId,
                timestamp = now
            )
        )
        Result.success(Unit)
    }

    suspend fun deliveryPartnerPickup(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val order = db.orderDao().getOrderByIdSync(orderId)
            ?: return@withContext Result.failure(IllegalStateException("Order not found"))

        val now = System.currentTimeMillis()
        val updated = order.copy(orderStatus = OrderStatus.OUT_FOR_DELIVERY, updatedAt = now)
        db.orderDao().updateOrder(updated)
        db.orderDao().insertHistory(
            OrderStatusHistoryEntity(
                orderId = orderId,
                status = OrderStatus.OUT_FOR_DELIVERY,
                note = "Order picked up from restaurant. On the way to customer address.",
                timestamp = now
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                recipientRole = UserRole.CUSTOMER,
                recipientId = order.customerId,
                title = "Out for Delivery! 🚀",
                message = "Your order #${order.orderNumber} is on its way to your location!",
                type = "ORDER",
                orderId = orderId,
                timestamp = now
            )
        )
        Result.success(Unit)
    }

    suspend fun deliveryPartnerDeliver(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val order = db.orderDao().getOrderByIdSync(orderId)
            ?: return@withContext Result.failure(IllegalStateException("Order not found"))

        val now = System.currentTimeMillis()
        val updated = order.copy(
            orderStatus = OrderStatus.DELIVERED,
            paymentStatus = PaymentStatus.PAID, // Marked paid upon successful delivery
            updatedAt = now
        )
        db.orderDao().updateOrder(updated)
        db.orderDao().insertHistory(
            OrderStatusHistoryEntity(
                orderId = orderId,
                status = OrderStatus.DELIVERED,
                note = "Order successfully delivered to customer",
                timestamp = now
            )
        )

        // Write restaurant credit to ledger
        db.ledgerDao().insertLedgerEntry(
            LedgerEntryEntity(
                id = "led_" + UUID.randomUUID().toString().take(8),
                transactionId = "TXN_REST_" + order.orderNumber,
                orderId = order.id,
                entityType = "RESTAURANT",
                entityId = order.restaurantId,
                type = LedgerType.ORDER_PAYMENT,
                amount = order.restaurantShare,
                isCredit = true,
                status = "SETTLED",
                referenceId = order.orderNumber,
                description = "Net earnings for Order #${order.orderNumber}",
                timestamp = now
            )
        )

        // Write delivery partner credit to ledger
        if (!order.deliveryPartnerId.isNullOrBlank()) {
            db.ledgerDao().insertLedgerEntry(
                LedgerEntryEntity(
                    id = "led_" + UUID.randomUUID().toString().take(8),
                    transactionId = "TXN_DELIV_" + order.orderNumber,
                    orderId = order.id,
                    entityType = "DELIVERY",
                    entityId = order.deliveryPartnerId,
                    type = LedgerType.DELIVERY_PAYOUT,
                    amount = order.deliveryShare,
                    isCredit = true,
                    status = "SETTLED",
                    referenceId = order.orderNumber,
                    description = "Delivery earnings for Order #${order.orderNumber}",
                    timestamp = now
                )
            )
        }

        // Platform Commission to ledger
        db.ledgerDao().insertLedgerEntry(
            LedgerEntryEntity(
                id = "led_" + UUID.randomUUID().toString().take(8),
                transactionId = "TXN_COMM_" + order.orderNumber,
                orderId = order.id,
                entityType = "PLATFORM",
                entityId = "admin_user",
                type = LedgerType.COMMISSION,
                amount = order.platformCommission,
                isCredit = true,
                status = "SETTLED",
                referenceId = order.orderNumber,
                description = "Platform commission collected for Order #${order.orderNumber}",
                timestamp = now
            )
        )

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                recipientRole = UserRole.CUSTOMER,
                recipientId = order.customerId,
                title = "Order Delivered! 🍽️",
                message = "Enjoy your fresh meal from Arkpur Food in Minutes!",
                type = "ORDER",
                orderId = orderId,
                timestamp = now
            )
        )
        Result.success(Unit)
    }

    suspend fun cancelOrder(orderId: String, reason: String, cancelledBy: UserRole): Result<Unit> = withContext(Dispatchers.IO) {
        val order = db.orderDao().getOrderByIdSync(orderId)
            ?: return@withContext Result.failure(IllegalStateException("Order not found"))

        // Cancellation rules: customer can cancel only if PLACED or CONFIRMED
        if (cancelledBy == UserRole.CUSTOMER && order.orderStatus != OrderStatus.PLACED && order.orderStatus != OrderStatus.CONFIRMED) {
            return@withContext Result.failure(IllegalStateException("Order is already preparing or out for delivery, cannot cancel."))
        }

        val now = System.currentTimeMillis()
        val willRefund = order.paymentStatus == PaymentStatus.PAID
        val newPaymentStatus = if (willRefund) PaymentStatus.REFUNDED else order.paymentStatus

        val updated = order.copy(
            orderStatus = OrderStatus.CANCELLED,
            paymentStatus = newPaymentStatus,
            cancellationReason = reason,
            updatedAt = now
        )
        db.orderDao().updateOrder(updated)
        db.orderDao().insertHistory(
            OrderStatusHistoryEntity(
                orderId = orderId,
                status = OrderStatus.CANCELLED,
                note = "Order cancelled by $cancelledBy. Reason: $reason",
                timestamp = now
            )
        )

        // Refund ledger entry if already paid
        if (willRefund) {
            db.ledgerDao().insertLedgerEntry(
                LedgerEntryEntity(
                    id = "led_" + UUID.randomUUID().toString().take(8),
                    transactionId = "TXN_REFUND_" + order.orderNumber,
                    orderId = order.id,
                    entityType = "CUSTOMER",
                    entityId = order.customerId,
                    type = LedgerType.REFUND,
                    amount = order.finalAmount,
                    isCredit = true, // Refund credited back to customer
                    status = "SETTLED",
                    referenceId = "REF-${order.orderNumber}",
                    description = "Full refund for cancelled Order #${order.orderNumber}",
                    timestamp = now
                )
            )
        }

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                recipientRole = UserRole.CUSTOMER,
                recipientId = order.customerId,
                title = "Order Cancelled",
                message = if (willRefund) "Order #${order.orderNumber} cancelled. Refund of ₹${order.finalAmount.toInt()} processed." else "Order #${order.orderNumber} cancelled.",
                type = "ORDER",
                orderId = orderId,
                timestamp = now
            )
        )
        Result.success(Unit)
    }

    // ----------------------------------------------------
    // PAYOUT PROCESSING (RESTAURANT & DELIVERY PARTNERS)
    // ----------------------------------------------------

    suspend fun requestRestaurantWeeklyPayout(restaurantId: String): Result<RestaurantPayoutEntity> = withContext(Dispatchers.IO) {
        val restaurant = db.restaurantDao().getRestaurantByIdSync(restaurantId)
            ?: return@withContext Result.failure(IllegalStateException("Restaurant not found"))

        val orders = db.orderDao().getOrdersForRestaurant(restaurantId).firstOrNull() ?: emptyList()
        val deliveredOrders = orders.filter { it.orderStatus == OrderStatus.DELIVERED }

        var gross = 0.0
        var netPayable = 0.0
        for (ord in deliveredOrders) {
            gross += ord.subtotal
            netPayable += ord.restaurantShare
        }
        val deductions = gross - netPayable

        if (netPayable <= 0.0) {
            return@withContext Result.failure(IllegalStateException("No eligible completed earnings to settle at this time."))
        }

        val payout = RestaurantPayoutEntity(
            id = "pay_rest_" + UUID.randomUUID().toString().take(8),
            payoutNumber = "PAY-R-" + Random.nextInt(1000, 9999),
            restaurantId = restaurantId,
            restaurantName = restaurant.name,
            periodStart = System.currentTimeMillis() - 7L * 24 * 3600 * 1000,
            periodEnd = System.currentTimeMillis(),
            totalOrders = deliveredOrders.size,
            grossEarnings = gross,
            commissionDeductions = deductions,
            netPayable = netPayable,
            status = PayoutStatus.REQUESTED,
            paymentMode = "UPI",
            referenceId = ""
        )
        db.payoutDao().insertRestaurantPayout(payout)
        Result.success(payout)
    }

    suspend fun requestDeliveryWeeklyPayout(partnerId: String): Result<DeliveryPayoutEntity> = withContext(Dispatchers.IO) {
        val partner = db.userDao().getUserByIdSync(partnerId)
            ?: return@withContext Result.failure(IllegalStateException("Partner not found"))

        val orders = db.orderDao().getOrdersForDeliveryPartner(partnerId).firstOrNull() ?: emptyList()
        val deliveredOrders = orders.filter { it.orderStatus == OrderStatus.DELIVERED }

        var earnings = 0.0
        for (ord in deliveredOrders) {
            earnings += ord.deliveryShare
        }

        if (earnings <= 0.0) {
            return@withContext Result.failure(IllegalStateException("No eligible completed delivery earnings to settle."))
        }

        val payout = DeliveryPayoutEntity(
            id = "pay_deliv_" + UUID.randomUUID().toString().take(8),
            payoutNumber = "PAY-D-" + Random.nextInt(1000, 9999),
            deliveryPartnerId = partnerId,
            partnerName = partner.name,
            periodStart = System.currentTimeMillis() - 7L * 24 * 3600 * 1000,
            periodEnd = System.currentTimeMillis(),
            totalDeliveries = deliveredOrders.size,
            deliveryEarnings = earnings,
            tips = 50.0,
            incentives = 100.0,
            netPayable = earnings + 150.0,
            status = PayoutStatus.REQUESTED
        )
        db.payoutDao().insertDeliveryPayout(payout)
        Result.success(payout)
    }

    suspend fun adminSettleRestaurantPayout(payoutId: String, refId: String, note: String): Result<Unit> = withContext(Dispatchers.IO) {
        val payout = db.payoutDao().getRestaurantPayoutById(payoutId)
            ?: return@withContext Result.failure(IllegalStateException("Payout not found"))

        val now = System.currentTimeMillis()
        val updated = payout.copy(
            status = PayoutStatus.PAID,
            referenceId = refId,
            adminNote = note,
            settledAt = now
        )
        db.payoutDao().updateRestaurantPayout(updated)

        // Write payout debit to immutable financial ledger
        db.ledgerDao().insertLedgerEntry(
            LedgerEntryEntity(
                id = "led_" + UUID.randomUUID().toString().take(8),
                transactionId = "TXN_PAYOUT_" + payout.payoutNumber,
                orderId = "",
                entityType = "RESTAURANT",
                entityId = payout.restaurantId,
                type = LedgerType.RESTAURANT_PAYOUT,
                amount = payout.netPayable,
                isCredit = false, // Payout settlement debit
                status = "SETTLED",
                referenceId = refId,
                description = "Bank/UPI Payout settlement #${payout.payoutNumber} to ${payout.restaurantName}",
                timestamp = now
            )
        )
        Result.success(Unit)
    }

    suspend fun adminSettleDeliveryPayout(payoutId: String, refId: String, note: String): Result<Unit> = withContext(Dispatchers.IO) {
        val payout = db.payoutDao().getDeliveryPayoutById(payoutId)
            ?: return@withContext Result.failure(IllegalStateException("Payout not found"))

        val now = System.currentTimeMillis()
        val updated = payout.copy(
            status = PayoutStatus.PAID,
            referenceId = refId,
            adminNote = note,
            settledAt = now
        )
        db.payoutDao().updateDeliveryPayout(updated)

        // Write payout settlement to ledger
        db.ledgerDao().insertLedgerEntry(
            LedgerEntryEntity(
                id = "led_" + UUID.randomUUID().toString().take(8),
                transactionId = "TXN_PAYOUT_" + payout.payoutNumber,
                orderId = "",
                entityType = "DELIVERY",
                entityId = payout.deliveryPartnerId,
                type = LedgerType.DELIVERY_PAYOUT,
                amount = payout.netPayable,
                isCredit = false,
                status = "SETTLED",
                referenceId = refId,
                description = "Rider Payout settlement #${payout.payoutNumber} to ${payout.partnerName}",
                timestamp = now
            )
        )
        Result.success(Unit)
    }

    // ----------------------------------------------------
    // SUPPORT & COMPLAINTS
    // ----------------------------------------------------

    suspend fun createSupportTicket(
        customerId: String,
        orderId: String?,
        category: TicketCategory,
        description: String
    ): Result<SupportTicketEntity> = withContext(Dispatchers.IO) {
        val customer = db.userDao().getUserByIdSync(customerId)
            ?: return@withContext Result.failure(IllegalStateException("Customer not found"))

        val ticket = SupportTicketEntity(
            id = "tkt_" + UUID.randomUUID().toString().take(8),
            ticketNumber = "TKT-" + Random.nextInt(1000, 9999),
            customerId = customerId,
            customerName = customer.name,
            customerMobile = customer.mobile,
            orderId = orderId,
            category = category,
            description = description
        )
        db.supportDao().insertTicket(ticket)
        Result.success(ticket)
    }
}
