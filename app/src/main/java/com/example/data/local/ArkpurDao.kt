package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSync(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE mobile = :mobile AND role = :role LIMIT 1")
    suspend fun getUserByMobileAndRole(mobile: String, role: UserRole): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role ORDER BY createdAt DESC")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET status = :status WHERE id = :id")
    suspend fun updateUserStatus(id: String, status: String)

    @Query("UPDATE users SET isOnline = :isOnline WHERE id = :id")
    suspend fun updateOnlineStatus(id: String, isOnline: Boolean)
}

@Dao
interface OtpDao {
    @Query("SELECT * FROM otp_sessions WHERE mobile = :mobile LIMIT 1")
    suspend fun getOtpSession(mobile: String): OtpSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtpSession(session: OtpSessionEntity)

    @Query("DELETE FROM otp_sessions WHERE mobile = :mobile")
    suspend fun deleteOtpSession(mobile: String)
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE customerId = :customerId ORDER BY isDefault DESC")
    fun getAddressesForCustomer(customerId: String): Flow<List<AddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun deleteAddress(id: String)

    @Query("UPDATE addresses SET isDefault = 0 WHERE customerId = :customerId")
    suspend fun clearDefaultAddresses(customerId: String)

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultAddress(id: String)
}

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants ORDER BY rating DESC")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE id = :id")
    fun getRestaurantById(id: String): Flow<RestaurantEntity?>

    @Query("SELECT * FROM restaurants WHERE id = :id")
    suspend fun getRestaurantByIdSync(id: String): RestaurantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: RestaurantEntity)

    @Update
    suspend fun updateRestaurant(restaurant: RestaurantEntity)

    @Query("UPDATE restaurants SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE restaurants SET isOpen = :isOpen WHERE id = :id")
    suspend fun updateIsOpen(id: String, isOpen: Boolean)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY displayOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: String)
}

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items ORDER BY name ASC")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE restaurantId = :restaurantId")
    fun getMenuItemsForRestaurant(restaurantId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE restaurantId = :restaurantId AND categoryId = :categoryId")
    fun getMenuItemsByCategory(restaurantId: String, categoryId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :id")
    suspend fun getMenuItemById(id: String): MenuItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMenuItems(items: List<MenuItemEntity>)

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Query("UPDATE menu_items SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateAvailability(id: String, isAvailable: Boolean)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItem(id: String)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE customerId = :customerId")
    fun getCartForCustomer(customerId: String): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE customerId = :customerId")
    suspend fun getCartForCustomerSync(customerId: String): List<CartItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: String)

    @Query("DELETE FROM cart_items WHERE customerId = :customerId")
    suspend fun clearCart(customerId: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderByIdSync(id: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getOrdersForCustomer(customerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE restaurantId = :restaurantId ORDER BY createdAt DESC")
    fun getOrdersForRestaurant(restaurantId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE deliveryPartnerId = :partnerId ORDER BY createdAt DESC")
    fun getOrdersForDeliveryPartner(partnerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderStatus IN ('READY_FOR_PICKUP', 'PREPARING') AND (deliveryPartnerId IS NULL OR deliveryPartnerId = '') ORDER BY createdAt DESC")
    fun getAvailableOrdersForDelivery(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Insert
    suspend fun insertHistory(history: OrderStatusHistoryEntity)

    @Query("SELECT * FROM order_status_history WHERE orderId = :orderId ORDER BY timestamp ASC")
    fun getOrderHistory(orderId: String): Flow<List<OrderStatusHistoryEntity>>
}

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_entries ORDER BY timestamp DESC")
    fun getAllLedgerEntries(): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE entityType = :entityType AND entityId = :entityId ORDER BY timestamp DESC")
    fun getLedgerEntriesForEntity(entityType: String, entityId: String): Flow<List<LedgerEntryEntity>>

    @Query("SELECT * FROM ledger_entries WHERE orderId = :orderId ORDER BY timestamp DESC")
    fun getLedgerEntriesForOrder(orderId: String): Flow<List<LedgerEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: LedgerEntryEntity)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payment_transactions ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentTransactionEntity>>

    @Query("SELECT * FROM payment_transactions WHERE orderId = :orderId LIMIT 1")
    suspend fun getPaymentByOrderId(orderId: String): PaymentTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentTransactionEntity)

    @Update
    suspend fun updatePayment(payment: PaymentTransactionEntity)
}

@Dao
interface PayoutDao {
    @Query("SELECT * FROM restaurant_payouts ORDER BY requestedAt DESC")
    fun getAllRestaurantPayouts(): Flow<List<RestaurantPayoutEntity>>

    @Query("SELECT * FROM restaurant_payouts WHERE restaurantId = :restaurantId ORDER BY requestedAt DESC")
    fun getPayoutsForRestaurant(restaurantId: String): Flow<List<RestaurantPayoutEntity>>

    @Query("SELECT * FROM restaurant_payouts WHERE id = :id LIMIT 1")
    suspend fun getRestaurantPayoutById(id: String): RestaurantPayoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurantPayout(payout: RestaurantPayoutEntity)

    @Update
    suspend fun updateRestaurantPayout(payout: RestaurantPayoutEntity)

    @Query("SELECT * FROM delivery_payouts ORDER BY requestedAt DESC")
    fun getAllDeliveryPayouts(): Flow<List<DeliveryPayoutEntity>>

    @Query("SELECT * FROM delivery_payouts WHERE deliveryPartnerId = :partnerId ORDER BY requestedAt DESC")
    fun getPayoutsForDeliveryPartner(partnerId: String): Flow<List<DeliveryPayoutEntity>>

    @Query("SELECT * FROM delivery_payouts WHERE id = :id LIMIT 1")
    suspend fun getDeliveryPayoutById(id: String): DeliveryPayoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryPayout(payout: DeliveryPayoutEntity)

    @Update
    suspend fun updateDeliveryPayout(payout: DeliveryPayoutEntity)
}

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons ORDER BY code ASC")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE isActive = 1")
    fun getActiveCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code AND isActive = 1 LIMIT 1")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCoupons(coupons: List<CouponEntity>)

    @Update
    suspend fun updateCoupon(coupon: CouponEntity)

    @Query("DELETE FROM coupons WHERE id = :id")
    suspend fun deleteCoupon(id: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE (recipientRole = :role AND (recipientId = :userId OR recipientId = 'ALL')) ORDER BY timestamp DESC")
    fun getNotificationsForUser(role: UserRole, userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientRole = :role AND (recipientId = :userId OR recipientId = 'ALL')")
    suspend fun markAllAsRead(role: UserRole, userId: String)
}

@Dao
interface SupportDao {
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getTicketsForCustomer(customerId: String): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM admin_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AdminSettingsEntity?>

    @Query("SELECT * FROM admin_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): AdminSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AdminSettingsEntity)
}
