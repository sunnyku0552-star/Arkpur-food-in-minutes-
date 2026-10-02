package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val role: UserRole,
    val mobile: String,
    val name: String,
    val email: String = "",
    val profilePhoto: String = "",
    val status: String = "ACTIVE", // ACTIVE, BLOCKED, SUSPENDED
    val restaurantId: String? = null,
    val vehicleType: String = "",
    val vehicleNumber: String = "",
    val isOnline: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "otp_sessions")
data class OtpSessionEntity(
    @PrimaryKey val mobile: String,
    val otpCode: String,
    val expiresAt: Long,
    val attemptsRemaining: Int = 3,
    val lastRequestedAt: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val name: String,
    val mobile: String,
    val villageArea: String,
    val fullAddress: String,
    val landmark: String,
    val pincode: String,
    val type: String = "Home", // Home, Work, Other
    val isDefault: Boolean = false
)

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tagline: String,
    val address: String,
    val area: String,
    val rating: Float = 4.5f,
    val totalRatings: Int = 120,
    val deliveryTimeMin: Int = 20,
    val distanceKm: Float = 2.5f,
    val isVegOnly: Boolean = false,
    val minOrder: Double = 99.0,
    val deliveryFee: Double = 25.0,
    val offers: String = "50% OFF up to ₹100",
    val isOpen: Boolean = true,
    val commissionRatePercent: Double = 15.0,
    val logoUrl: String = "",
    val bannerUrl: String = "",
    val upiId: String = "restaurant@upi",
    val bankAccount: String = "918234567890",
    val bankIfsc: String = "SBIN0001234",
    val bankHolderName: String = "",
    val status: String = "ACTIVE", // ACTIVE, PENDING, SUSPENDED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String,
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val restaurantId: String,
    val categoryId: String,
    val name: String,
    val description: String,
    val price: Double,
    val discountPrice: Double? = null,
    val isVeg: Boolean,
    val isAvailable: Boolean = true,
    val imageUrl: String = "",
    val rating: Float = 4.6f,
    val prepTimeMinutes: Int = 15
)

@Entity(tableName = "addons")
data class AddonEntity(
    @PrimaryKey val id: String,
    val menuItemId: String,
    val name: String,
    val price: Double,
    val isAvailable: Boolean = true
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val restaurantId: String,
    val menuItemId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val isVeg: Boolean,
    val specialInstructions: String = "",
    val selectedAddonsJson: String = "" // json or comma-separated name:price
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: String,
    val customerId: String,
    val restaurantId: String,
    val deliveryPartnerId: String? = null,
    val customerName: String,
    val customerMobile: String,
    val deliveryAddressText: String,
    val deliveryAddressLandmark: String = "",
    val deliveryPincode: String = "800001",
    val itemsSummary: String, // Comma separated items for quick display
    val itemsDetailsJson: String, // Full items list snapshot
    val subtotal: Double,
    val deliveryFee: Double,
    val platformFee: Double,
    val tax: Double,
    val couponDiscount: Double,
    val couponCode: String = "",
    val finalAmount: Double,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val orderStatus: OrderStatus,
    val prepTimeMinutes: Int = 20,
    val restaurantShare: Double,
    val deliveryShare: Double,
    val platformCommission: Double,
    val rejectionReason: String = "",
    val cancellationReason: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_status_history")
data class OrderStatusHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val status: OrderStatus,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey val transactionId: String,
    val orderId: String,
    val customerId: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus,
    val gatewayRefId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ledger_entries")
data class LedgerEntryEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val orderId: String,
    val entityType: String, // CUSTOMER, RESTAURANT, DELIVERY, PLATFORM
    val entityId: String,
    val type: LedgerType,
    val amount: Double,
    val isCredit: Boolean,
    val status: String = "SETTLED", // SETTLED, PENDING, REVERSED
    val referenceId: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "restaurant_payouts")
data class RestaurantPayoutEntity(
    @PrimaryKey val id: String,
    val payoutNumber: String,
    val restaurantId: String,
    val restaurantName: String,
    val periodStart: Long,
    val periodEnd: Long,
    val totalOrders: Int,
    val grossEarnings: Double,
    val commissionDeductions: Double,
    val otherDeductions: Double = 0.0,
    val netPayable: Double,
    val status: PayoutStatus = PayoutStatus.REQUESTED,
    val paymentMode: String = "UPI",
    val referenceId: String = "",
    val adminNote: String = "",
    val requestedAt: Long = System.currentTimeMillis(),
    val settledAt: Long? = null
)

@Entity(tableName = "delivery_payouts")
data class DeliveryPayoutEntity(
    @PrimaryKey val id: String,
    val payoutNumber: String,
    val deliveryPartnerId: String,
    val partnerName: String,
    val periodStart: Long,
    val periodEnd: Long,
    val totalDeliveries: Int,
    val deliveryEarnings: Double,
    val tips: Double = 0.0,
    val incentives: Double = 0.0,
    val deductions: Double = 0.0,
    val netPayable: Double,
    val status: PayoutStatus = PayoutStatus.REQUESTED,
    val paymentMode: String = "BANK",
    val referenceId: String = "",
    val adminNote: String = "",
    val requestedAt: Long = System.currentTimeMillis(),
    val settledAt: Long? = null
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val id: String,
    val code: String,
    val description: String,
    val discountType: DiscountType,
    val discountValue: Double,
    val minOrderAmount: Double,
    val maxDiscountAmount: Double,
    val expiryDate: Long = System.currentTimeMillis() + 30L * 24 * 3600 * 1000,
    val usageLimit: Int = 1000,
    val perUserLimit: Int = 5,
    val restaurantId: String? = null,
    val isActive: Boolean = true
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val recipientRole: UserRole,
    val recipientId: String, // "ALL" or specific ID
    val title: String,
    val message: String,
    val type: String, // ORDER, PAYMENT, PAYOUT, ALERT
    val orderId: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val ticketNumber: String,
    val customerId: String,
    val customerName: String,
    val customerMobile: String,
    val orderId: String? = null,
    val category: TicketCategory,
    val description: String,
    val status: TicketStatus = TicketStatus.OPEN,
    val replyNote: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_settings")
data class AdminSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val appName: String = "ARKPUR FOOD IN MINUTES",
    val platformFee: Double = 5.0,
    val baseDeliveryFee: Double = 25.0,
    val deliveryFeePerKm: Double = 6.0,
    val taxRatePercent: Double = 5.0,
    val defaultRestaurantCommissionPercent: Double = 15.0,
    val deliveryPartnerSharePercent: Double = 80.0, // of delivery fee + base pay
    val cancellationGraceMinutes: Int = 5,
    val supportPhone: String = "+91 98765 43210",
    val supportWhatsapp: String = "+91 98765 43210",
    val isTestMode: Boolean = false
)
