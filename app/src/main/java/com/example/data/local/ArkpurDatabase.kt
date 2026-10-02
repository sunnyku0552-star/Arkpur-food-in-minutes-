package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        OtpSessionEntity::class,
        AddressEntity::class,
        RestaurantEntity::class,
        CategoryEntity::class,
        MenuItemEntity::class,
        AddonEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderStatusHistoryEntity::class,
        PaymentTransactionEntity::class,
        LedgerEntryEntity::class,
        RestaurantPayoutEntity::class,
        DeliveryPayoutEntity::class,
        CouponEntity::class,
        NotificationEntity::class,
        SupportTicketEntity::class,
        AdminSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ArkpurDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun otpDao(): OtpDao
    abstract fun addressDao(): AddressDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun categoryDao(): CategoryDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun paymentDao(): PaymentDao
    abstract fun payoutDao(): PayoutDao
    abstract fun couponDao(): CouponDao
    abstract fun notificationDao(): NotificationDao
    abstract fun supportDao(): SupportDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: ArkpurDatabase? = null

        fun getInstance(context: Context): ArkpurDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArkpurDatabase::class.java,
                    "arkpur_food_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed initial platform data
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedInitialData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(db: ArkpurDatabase) {
            // Admin Settings
            db.settingsDao().insertOrUpdate(
                AdminSettingsEntity(
                    id = 1,
                    appName = "ARKPUR FOOD IN MINUTES",
                    platformFee = 5.0,
                    baseDeliveryFee = 25.0,
                    deliveryFeePerKm = 6.0,
                    taxRatePercent = 5.0,
                    defaultRestaurantCommissionPercent = 15.0,
                    deliveryPartnerSharePercent = 80.0,
                    supportPhone = "+91 98765 43210",
                    supportWhatsapp = "+91 98765 43210",
                    isTestMode = false
                )
            )

            // Categories
            val categories = listOf(
                CategoryEntity("cat_biryani", "Biryani & Rice", "rice_bowl", 1),
                CategoryEntity("cat_chicken", "Chicken & Mutton", "kebab_dining", 2),
                CategoryEntity("cat_thali", "Thali & Meals", "restaurant", 3),
                CategoryEntity("cat_pizza", "Pizza & Burgers", "local_pizza", 4),
                CategoryEntity("cat_rolls", "Rolls & Fast Food", "fastfood", 5),
                CategoryEntity("cat_chinese", "Chinese & Noodles", "ramen_dining", 6),
                CategoryEntity("cat_sweets", "Mithai & Desserts", "bakery_dining", 7),
                CategoryEntity("cat_drinks", "Beverages & Shakes", "local_cafe", 8)
            )
            db.categoryDao().insertAllCategories(categories)

            // Restaurants
            val rest1 = RestaurantEntity(
                id = "rest_biryani_mahal",
                name = "Arkpur Biryani & Kebabs",
                tagline = "Authentic Dum Biryani delivered in 20 minutes",
                address = "Main Market Road, Near Gandhi Chowk",
                area = "Central Arkpur",
                rating = 4.8f,
                totalRatings = 248,
                deliveryTimeMin = 20,
                distanceKm = 1.6f,
                isVegOnly = false,
                minOrder = 99.0,
                deliveryFee = 20.0,
                offers = "50% OFF up to ₹100",
                isOpen = true,
                commissionRatePercent = 15.0,
                upiId = "arkpurbiryani@upi",
                bankAccount = "982301928374",
                bankIfsc = "SBIN0004321",
                bankHolderName = "Arkpur Biryani Foods Pvt Ltd"
            )

            val rest2 = RestaurantEntity(
                id = "rest_royal_darbar",
                name = "Royal Darbar Grand",
                tagline = "Royal Mughlai & North Indian Curries",
                address = "Station Road, Opp. SBI Main Branch",
                area = "Station Area",
                rating = 4.6f,
                totalRatings = 180,
                deliveryTimeMin = 25,
                distanceKm = 2.4f,
                isVegOnly = false,
                minOrder = 149.0,
                deliveryFee = 25.0,
                offers = "FLAT ₹40 OFF above ₹249",
                isOpen = true,
                commissionRatePercent = 14.0,
                upiId = "royaldarbar@upi",
                bankAccount = "872349012384",
                bankIfsc = "HDFC0001824",
                bankHolderName = "Royal Darbar Hospitality"
            )

            val rest3 = RestaurantEntity(
                id = "rest_shree_krishna",
                name = "Shree Krishna Shuddh Shakahari",
                tagline = "100% Pure Veg Delicacies, Paneer Specials & Thali",
                address = "Cinema Road, Near Durga Mandir",
                area = "Mandir Chowk",
                rating = 4.7f,
                totalRatings = 310,
                deliveryTimeMin = 18,
                distanceKm = 1.1f,
                isVegOnly = true,
                minOrder = 80.0,
                deliveryFee = 15.0,
                offers = "FREE Sweets on orders > ₹300",
                isOpen = true,
                commissionRatePercent = 12.0,
                upiId = "shreekrishna@upi",
                bankAccount = "623849102834",
                bankIfsc = "PUNB0002931",
                bankHolderName = "Shree Krishna Bhojanalaya"
            )

            val rest4 = RestaurantEntity(
                id = "rest_chaska_rolls",
                name = "Chaska Fast Food & Kathi Rolls",
                tagline = "Crispy rolls, momos, burgers & chowmein",
                address = "College Gate, Hospital Road",
                area = "Hospital Campus",
                rating = 4.5f,
                totalRatings = 156,
                deliveryTimeMin = 15,
                distanceKm = 1.3f,
                isVegOnly = false,
                minOrder = 70.0,
                deliveryFee = 15.0,
                offers = "Buy 2 Rolls Get 1 Cold Drink",
                isOpen = true,
                commissionRatePercent = 15.0,
                upiId = "chaskafastfood@upi",
                bankAccount = "483920193847",
                bankIfsc = "UBIN0008472",
                bankHolderName = "Chaska Food Corner"
            )

            val rest5 = RestaurantEntity(
                id = "rest_mithai_bhandar",
                name = "Mithai Mahal & Chaat Bhandar",
                tagline = "Hot Samosas, Jalebi, Gulab Jamun & Rasmalai",
                address = "Purani Bazaar, Near Post Office",
                area = "Purani Bazaar",
                rating = 4.9f,
                totalRatings = 420,
                deliveryTimeMin = 12,
                distanceKm = 0.9f,
                isVegOnly = true,
                minOrder = 50.0,
                deliveryFee = 10.0,
                offers = "Freshly prepared every hour",
                isOpen = true,
                commissionRatePercent = 10.0,
                upiId = "mithaimahal@upi",
                bankAccount = "739201928374",
                bankIfsc = "ICIC0003829",
                bankHolderName = "Mithai Mahal Sweets"
            )

            db.restaurantDao().insertRestaurant(rest1)
            db.restaurantDao().insertRestaurant(rest2)
            db.restaurantDao().insertRestaurant(rest3)
            db.restaurantDao().insertRestaurant(rest4)
            db.restaurantDao().insertRestaurant(rest5)

            // Menu Items
            val menuItems = listOf(
                // Rest 1: Arkpur Biryani & Kebabs
                MenuItemEntity("m_101", rest1.id, "cat_biryani", "Special Chicken Dum Biryani", "Fragrant Basmati rice cooked with tender chicken pieces & secret Arkpur spices. Served with Raita.", 220.0, 199.0, false, true, "", 4.9f, 20),
                MenuItemEntity("m_102", rest1.id, "cat_biryani", "Mutton Dum Biryani (Double Gosht)", "Rich traditional Awadhi style mutton biryani with slow-cooked succulent meat.", 340.0, 310.0, false, true, "", 4.8f, 25),
                MenuItemEntity("m_103", rest1.id, "cat_chicken", "Chicken Reshmi Kebab (6 Pcs)", "Mouth-melting chicken kebabs marinated in cashew paste and cream.", 240.0, 219.0, false, true, "", 4.7f, 15),
                MenuItemEntity("m_104", rest1.id, "cat_biryani", "Hyderabadi Veg Biryani", "Layered basmati rice with freshly spiced garden vegetables and paneer cubes.", 170.0, 149.0, true, true, "", 4.6f, 18),
                MenuItemEntity("m_105", rest1.id, "cat_drinks", "Royal Shahi Firni", "Creamy ground rice pudding flavoured with cardamom and saffron in earthen clay cup.", 70.0, null, true, true, "", 4.9f, 5),

                // Rest 2: Royal Darbar
                MenuItemEntity("m_201", rest2.id, "cat_chicken", "Royal Butter Chicken (Boneless)", "Tender tandoori chicken cooked in velvety tomato, butter and cashew makhani gravy.", 280.0, 259.0, false, true, "", 4.8f, 20),
                MenuItemEntity("m_202", rest2.id, "cat_thali", "Royal Maharaja Non-Veg Thali", "Chicken Curry, Mutton Rogan Josh, Dal Makhani, 2 Butter Naan, Jeera Rice, Raita & Gulab Jamun.", 360.0, 329.0, false, true, "", 4.9f, 25),
                MenuItemEntity("m_203", rest2.id, "cat_thali", "Royal Shahi Paneer Special", "Fresh cottage cheese in sweet and tangy rich cashew almond sauce.", 210.0, 189.0, true, true, "", 4.6f, 15),
                MenuItemEntity("m_204", rest2.id, "cat_rolls", "Garlic Butter Naan (2 Pcs)", "Crispy tandoor-baked flatbread brushed with garlic and butter.", 80.0, null, true, true, "", 4.7f, 10),

                // Rest 3: Shree Krishna Pure Veg
                MenuItemEntity("m_301", rest3.id, "cat_thali", "Deluxe Shuddh Veg Thali", "Paneer Lababdar, Dal Tadka, Seasonal Sabzi, 4 Butter Roti, Steamed Rice, Raita, Salad & Sweet.", 190.0, 169.0, true, true, "", 4.8f, 18),
                MenuItemEntity("m_302", rest3.id, "cat_thali", "Kadai Paneer & Butter Tawa Roti Combo", "Spicy bell pepper and cottage cheese gravy with 3 fresh tawa rotis.", 180.0, 159.0, true, true, "", 4.7f, 15),
                MenuItemEntity("m_303", rest3.id, "cat_thali", "Dal Makhani Special (Slow Cooked)", "Black lentils cooked overnight with cream, butter and mild spices.", 150.0, 139.0, true, true, "", 4.9f, 12),
                MenuItemEntity("m_304", rest3.id, "cat_drinks", "Kulhad Masala Chaas", "Spiced refreshing buttermilk with roasted cumin and mint.", 40.0, null, true, true, "", 4.8f, 5),

                // Rest 4: Chaska Fast Food
                MenuItemEntity("m_401", rest4.id, "cat_rolls", "Double Egg Double Chicken Roll", "Crisp lachha paratha wrapped with juicy spiced chicken chunks and farm-fresh eggs.", 130.0, 119.0, false, true, "", 4.8f, 12),
                MenuItemEntity("m_402", rest4.id, "cat_rolls", "Paneer Tikka Kathi Roll", "Smoky roasted paneer tossed in onions and mint chutney wrapped in buttery paratha.", 110.0, 99.0, true, true, "", 4.7f, 10),
                MenuItemEntity("m_403", rest4.id, "cat_chinese", "Desi Veg Hakka Chowmein", "Stir-fried noodles with crunchy cabbage, capsicum, carrots in zesty sauce.", 100.0, 89.0, true, true, "", 4.6f, 12),
                MenuItemEntity("m_404", rest4.id, "cat_pizza", "Crispy Chicken Zinger Burger", "Crispy spiced fried chicken patty with mayonnaise and fresh lettuce in toasted bun.", 120.0, 109.0, false, true, "", 4.7f, 10),

                // Rest 5: Mithai Mahal
                MenuItemEntity("m_501", rest5.id, "cat_sweets", "Hot Samosa with Chutney (2 Pcs)", "Golden flaky pastry stuffed with aromatic spiced potato and green peas.", 30.0, null, true, true, "", 4.9f, 5),
                MenuItemEntity("m_502", rest5.id, "cat_sweets", "Ghee Desi Jalebi (250g)", "Crispy piping hot spiral jalebis dipped in pure saffron sugar syrup.", 90.0, 80.0, true, true, "", 4.9f, 8),
                MenuItemEntity("m_503", rest5.id, "cat_sweets", "Gulab Jamun (4 Pcs)", "Soft khoya dumplings soaked in fragrant rose and cardamom syrup.", 70.0, 60.0, true, true, "", 4.8f, 5),
                MenuItemEntity("m_504", rest5.id, "cat_sweets", "Kesaria Rasmalai (2 Pcs)", "Spongy cottage cheese patties in thick saffron flavoured rabdi with pistachios.", 80.0, 75.0, true, true, "", 5.0f, 5)
            )
            db.menuItemDao().insertAllMenuItems(menuItems)

            // Coupons
            val coupons = listOf(
                CouponEntity("c_arkpur50", "ARKPUR50", "50% OFF up to ₹100 on orders above ₹199", DiscountType.PERCENTAGE, 50.0, 199.0, 100.0),
                CouponEntity("c_minutes20", "MINUTES20", "Flat ₹20 OFF on orders above ₹149", DiscountType.FIXED, 20.0, 149.0, 20.0),
                CouponEntity("c_welcome100", "WELCOME100", "Flat ₹100 OFF for new customers on orders above ₹299", DiscountType.FIXED, 100.0, 299.0, 100.0)
            )
            db.couponDao().insertAllCoupons(coupons)

            // Initial Users
            val adminUser = UserEntity(
                id = "admin_user",
                role = UserRole.ADMIN,
                mobile = "9999999999",
                name = "Arkpur Platform Admin",
                email = "admin@arkpur.food",
                status = "ACTIVE"
            )

            val restPartner1 = UserEntity(
                id = "user_rest_1",
                role = UserRole.RESTAURANT,
                mobile = "9888888881",
                name = "Manager (Arkpur Biryani)",
                email = "partner@arkpurbiryani.com",
                restaurantId = rest1.id,
                status = "ACTIVE"
            )

            val restPartner2 = UserEntity(
                id = "user_rest_2",
                role = UserRole.RESTAURANT,
                mobile = "9888888882",
                name = "Manager (Royal Darbar)",
                email = "manager@royaldarbar.com",
                restaurantId = rest2.id,
                status = "ACTIVE"
            )

            val deliveryPartner1 = UserEntity(
                id = "rider_rahul",
                role = UserRole.DELIVERY,
                mobile = "9777777771",
                name = "Rahul Kumar",
                email = "rahul.rider@arkpur.food",
                vehicleType = "Motorcycle (Hero Splendor)",
                vehicleNumber = "BR-01-AB-1234",
                isOnline = true,
                status = "ACTIVE"
            )

            val deliveryPartner2 = UserEntity(
                id = "rider_amit",
                role = UserRole.DELIVERY,
                mobile = "9777777772",
                name = "Amit Sharma",
                email = "amit.delivery@arkpur.food",
                vehicleType = "EV Scooter (Ola S1)",
                vehicleNumber = "BR-01-CD-5678",
                isOnline = true,
                status = "ACTIVE"
            )

            val customerUser = UserEntity(
                id = "cust_sunny",
                role = UserRole.CUSTOMER,
                mobile = "9111111111",
                name = "Sunny Kumar",
                email = "sunny@example.com",
                status = "ACTIVE"
            )

            db.userDao().insertUser(adminUser)
            db.userDao().insertUser(restPartner1)
            db.userDao().insertUser(restPartner2)
            db.userDao().insertUser(deliveryPartner1)
            db.userDao().insertUser(deliveryPartner2)
            db.userDao().insertUser(customerUser)

            // Seed customer address
            db.addressDao().insertAddress(
                AddressEntity(
                    id = "addr_1",
                    customerId = customerUser.id,
                    name = "Sunny Kumar",
                    mobile = "9111111111",
                    villageArea = "Station Road",
                    fullAddress = "Flat 201, Shanti Niketan Apartment, Near Railway Station",
                    landmark = "Opposite State Bank ATM",
                    pincode = "800001",
                    type = "Home",
                    isDefault = true
                )
            )

            // Seed an initial demo active order to show real-time synchronization out-of-the-box
            val order1 = OrderEntity(
                id = "ord_1001",
                orderNumber = "ARK-8291",
                customerId = customerUser.id,
                restaurantId = rest1.id,
                deliveryPartnerId = deliveryPartner1.id,
                customerName = "Sunny Kumar",
                customerMobile = "9111111111",
                deliveryAddressText = "Flat 201, Shanti Niketan Apartment, Station Road",
                deliveryAddressLandmark = "Opp. SBI ATM",
                deliveryPincode = "800001",
                itemsSummary = "1x Special Chicken Dum Biryani, 1x Royal Shahi Firni",
                itemsDetailsJson = "[{\"name\":\"Special Chicken Dum Biryani\",\"qty\":1,\"price\":199.0},{\"name\":\"Royal Shahi Firni\",\"qty\":1,\"price\":70.0}]",
                subtotal = 269.0,
                deliveryFee = 20.0,
                platformFee = 5.0,
                tax = 13.45,
                couponDiscount = 50.0,
                couponCode = "ARKPUR50",
                finalAmount = 257.45,
                paymentMethod = PaymentMethod.ONLINE_UPI,
                paymentStatus = PaymentStatus.PAID,
                orderStatus = OrderStatus.OUT_FOR_DELIVERY,
                prepTimeMinutes = 20,
                restaurantShare = 228.65, // ~85% of items
                deliveryShare = 25.0, // Delivery pay
                platformCommission = 40.35,
                createdAt = System.currentTimeMillis() - 25 * 60 * 1000,
                updatedAt = System.currentTimeMillis() - 5 * 60 * 1000
            )
            db.orderDao().insertOrder(order1)

            // Order status history
            db.orderDao().insertHistory(OrderStatusHistoryEntity(0, order1.id, OrderStatus.PLACED, "Customer placed order via UPI", order1.createdAt))
            db.orderDao().insertHistory(OrderStatusHistoryEntity(0, order1.id, OrderStatus.CONFIRMED, "Restaurant accepted order (Prep time: 20 mins)", order1.createdAt + 2 * 60 * 1000))
            db.orderDao().insertHistory(OrderStatusHistoryEntity(0, order1.id, OrderStatus.PREPARING, "Chef is preparing authentic Biryani", order1.createdAt + 4 * 60 * 1000))
            db.orderDao().insertHistory(OrderStatusHistoryEntity(0, order1.id, OrderStatus.READY_FOR_PICKUP, "Order packed and hot in kitchen cloche", order1.createdAt + 16 * 60 * 1000))
            db.orderDao().insertHistory(OrderStatusHistoryEntity(0, order1.id, OrderStatus.DELIVERY_ASSIGNED, "Rahul Kumar assigned for delivery", order1.createdAt + 17 * 60 * 1000))
            db.orderDao().insertHistory(OrderStatusHistoryEntity(0, order1.id, OrderStatus.OUT_FOR_DELIVERY, "Rahul Kumar picked up parcel and is on the way", order1.createdAt + 20 * 60 * 1000))

            // Payment transaction
            db.paymentDao().insertPayment(
                PaymentTransactionEntity(
                    transactionId = "TXN_UPI_829101",
                    orderId = order1.id,
                    customerId = customerUser.id,
                    amount = order1.finalAmount,
                    paymentMethod = PaymentMethod.ONLINE_UPI,
                    status = PaymentStatus.PAID,
                    gatewayRefId = "UPI-NPCI-8291048392",
                    timestamp = order1.createdAt
                )
            )

            // Ledger Entries for order 1
            db.ledgerDao().insertLedgerEntry(
                LedgerEntryEntity(
                    id = "led_1",
                    transactionId = "TXN_UPI_829101",
                    orderId = order1.id,
                    entityType = "CUSTOMER",
                    entityId = customerUser.id,
                    type = LedgerType.ORDER_PAYMENT,
                    amount = order1.finalAmount,
                    isCredit = false,
                    status = "SETTLED",
                    referenceId = "UPI-NPCI-8291048392",
                    description = "Customer payment for Order #${order1.orderNumber}",
                    timestamp = order1.createdAt
                )
            )
            db.ledgerDao().insertLedgerEntry(
                LedgerEntryEntity(
                    id = "led_2",
                    transactionId = "TXN_UPI_829101",
                    orderId = order1.id,
                    entityType = "PLATFORM",
                    entityId = "admin_user",
                    type = LedgerType.COMMISSION,
                    amount = order1.platformCommission,
                    isCredit = true,
                    status = "SETTLED",
                    referenceId = "COMM-${order1.orderNumber}",
                    description = "Platform commission + platform fee for #${order1.orderNumber}",
                    timestamp = order1.createdAt
                )
            )

            // Sample notification
            db.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_1",
                    recipientRole = UserRole.CUSTOMER,
                    recipientId = customerUser.id,
                    title = "Order is on the way! 🛵",
                    message = "Rahul Kumar is delivering your hot Biryani in ~7 mins.",
                    type = "ORDER",
                    orderId = order1.id,
                    timestamp = System.currentTimeMillis() - 5 * 60 * 1000
                )
            )
        }
    }
}
