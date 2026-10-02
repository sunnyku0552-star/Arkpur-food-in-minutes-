package com.example.data.model

enum class UserRole {
    CUSTOMER,
    RESTAURANT,
    DELIVERY,
    ADMIN
}

enum class OrderStatus(val displayName: String) {
    PLACED("Order Placed"),
    CONFIRMED("Restaurant Confirmed"),
    PREPARING("Food Preparing"),
    READY_FOR_PICKUP("Ready for Pickup"),
    DELIVERY_ASSIGNED("Delivery Partner Assigned"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    PAID,
    FAILED,
    REFUNDED,
    PARTIALLY_REFUNDED
}

enum class PaymentMethod(val displayName: String) {
    COD("Cash on Delivery"),
    ONLINE_UPI("UPI / Online Payment")
}

enum class PayoutStatus {
    REQUESTED,
    PROCESSING,
    PAID,
    REJECTED
}

enum class LedgerType {
    ORDER_PAYMENT,
    RESTAURANT_PAYOUT,
    DELIVERY_PAYOUT,
    COMMISSION,
    REFUND,
    REVERSAL
}

enum class DiscountType {
    PERCENTAGE,
    FIXED
}

enum class TicketCategory(val displayName: String) {
    FOOD_QUALITY("Food Quality Issue"),
    LATE_DELIVERY("Delayed Delivery"),
    PAYMENT_ISSUE("Payment / Refund Issue"),
    WRONG_ITEMS("Missing or Wrong Items"),
    OTHER("Other Query")
}

enum class TicketStatus {
    OPEN,
    RESOLVED,
    CLOSED
}
