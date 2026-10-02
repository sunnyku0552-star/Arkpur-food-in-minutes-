package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.admin.AdminScreen
import com.example.ui.common.NotificationsBottomSheet
import com.example.ui.common.RoleSwitcherDialog
import com.example.ui.customer.CustomerScreen
import com.example.ui.delivery.DeliveryScreen
import com.example.ui.restaurant.RestaurantScreen
import com.example.ui.theme.ArkpurFoodTheme
import com.example.ui.theme.BackgroundLight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArkpurFoodTheme {
                val viewModel: MainViewModel = viewModel()
                val currentPanel by viewModel.currentPanel.collectAsState()
                val notifications by viewModel.customerNotifications.collectAsState()

                var showRoleSwitcher by remember { mutableStateOf(false) }
                var showNotificationsSheet by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = BackgroundLight
                ) {
                    when (currentPanel) {
                        UserRole.CUSTOMER -> {
                            CustomerScreen(
                                viewModel = viewModel,
                                onOpenRoleSwitcher = { showRoleSwitcher = true },
                                onOpenNotifications = { showNotificationsSheet = true }
                            )
                        }
                        UserRole.RESTAURANT -> {
                            RestaurantScreen(
                                viewModel = viewModel,
                                onOpenRoleSwitcher = { showRoleSwitcher = true },
                                onOpenNotifications = { showNotificationsSheet = true }
                            )
                        }
                        UserRole.DELIVERY -> {
                            DeliveryScreen(
                                viewModel = viewModel,
                                onOpenRoleSwitcher = { showRoleSwitcher = true },
                                onOpenNotifications = { showNotificationsSheet = true }
                            )
                        }
                        UserRole.ADMIN -> {
                            AdminScreen(
                                viewModel = viewModel,
                                onOpenRoleSwitcher = { showRoleSwitcher = true },
                                onOpenNotifications = { showNotificationsSheet = true }
                            )
                        }
                    }
                }

                if (showRoleSwitcher) {
                    RoleSwitcherDialog(
                        currentPanel = currentPanel,
                        viewModel = viewModel,
                        onDismiss = { showRoleSwitcher = false }
                    )
                }

                if (showNotificationsSheet) {
                    NotificationsBottomSheet(
                        notifications = notifications,
                        onDismiss = { showNotificationsSheet = false },
                        onMarkRead = { notifId -> viewModel.markNotificationRead(notifId) }
                    )
                }
            }
        }
    }
}
