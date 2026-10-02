package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ArkpurDatabase
import com.example.data.model.*
import com.example.data.repository.ArkpurRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ArkpurPlatformTest {

    private lateinit var db: ArkpurDatabase
    private lateinit var repository: ArkpurRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ArkpurDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ArkpurRepository(db)
        runBlocking {
            ArkpurDatabase.seedInitialData(db)
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testOtpGenerationAndVerification() = runBlocking {
        val mobile = "9876543210"
        val otpResult = repository.requestOtp(mobile)
        assertTrue(otpResult.isSuccess)
        val otpCode = otpResult.getOrThrow()
        assertEquals(6, otpCode.length)

        // Test wrong OTP
        val wrongVerify = repository.verifyOtp(mobile, "000000")
        assertTrue(wrongVerify.isFailure)

        // Test correct OTP
        val correctVerify = repository.verifyOtp(mobile, otpCode)
        assertTrue(correctVerify.isSuccess)
        val user = correctVerify.getOrThrow()
        assertEquals(mobile, user.mobile)
        assertEquals(UserRole.CUSTOMER, user.role)
    }

    @Test
    fun testOrderPlacementAndLedgerEntry() = runBlocking {
        // Add item to cart
        val cartItem = CartItemEntity(
            id = "c_1",
            customerId = "cust_sunny",
            restaurantId = "rest_biryani_mahal",
            menuItemId = "m_101",
            name = "Special Chicken Dum Biryani",
            price = 199.0,
            quantity = 2,
            isVeg = false
        )
        db.cartDao().insertCartItem(cartItem)

        // Place order
        val orderResult = repository.placeOrder(
            customerId = "cust_sunny",
            addressId = "addr_1",
            paymentMethod = PaymentMethod.ONLINE_UPI,
            couponCode = "ARKPUR50"
        )
        assertTrue(orderResult.isSuccess)
        val order = orderResult.getOrThrow()
        assertEquals(OrderStatus.PLACED, order.orderStatus)
        assertEquals(PaymentStatus.PAID, order.paymentStatus)
        assertTrue(order.finalAmount > 0.0)

        // Verify ledger entry created
        val ledgerEntries = db.ledgerDao().getLedgerEntriesForOrder(order.id)
        assertNotNull(ledgerEntries)

        // Transition: Restaurant Accept
        val acceptResult = repository.restaurantAcceptOrder(order.id, 20)
        assertTrue(acceptResult.isSuccess)
        val acceptedOrder = db.orderDao().getOrderByIdSync(order.id)
        assertEquals(OrderStatus.CONFIRMED, acceptedOrder?.orderStatus)

        // Transition: Kitchen Preparing
        repository.restaurantStartPreparing(order.id)
        val prepOrder = db.orderDao().getOrderByIdSync(order.id)
        assertEquals(OrderStatus.PREPARING, prepOrder?.orderStatus)

        // Transition: Ready for Pickup
        repository.restaurantMarkReady(order.id)
        val readyOrder = db.orderDao().getOrderByIdSync(order.id)
        assertEquals(OrderStatus.READY_FOR_PICKUP, readyOrder?.orderStatus)

        // Transition: Delivery Partner Accept
        repository.deliveryPartnerAcceptOrder(order.id, "rider_rahul")
        val assignedOrder = db.orderDao().getOrderByIdSync(order.id)
        assertEquals(OrderStatus.DELIVERY_ASSIGNED, assignedOrder?.orderStatus)
        assertEquals("rider_rahul", assignedOrder?.deliveryPartnerId)

        // Transition: Pickup & Out for Delivery
        repository.deliveryPartnerPickup(order.id)
        val outOrder = db.orderDao().getOrderByIdSync(order.id)
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, outOrder?.orderStatus)

        // Transition: Delivery Complete
        repository.deliveryPartnerDeliver(order.id)
        val deliveredOrder = db.orderDao().getOrderByIdSync(order.id)
        assertEquals(OrderStatus.DELIVERED, deliveredOrder?.orderStatus)
    }
}
