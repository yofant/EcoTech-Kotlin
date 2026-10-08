package com.example.ecotech

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

enum class Screen {
    Welcome,
    Login,
    Register,
    ForgotPassword,
    CustomerHome,
    Catalog,
    CollectionPoint,
    OrderSummary,
    Payment,
    OrderConfirmation,
    Profile,
    Notifications,
    Chat,
    SellerHome,
    SoldProducts,
    BoughtProducts,
    CustomerRecords,
    SellerStats,
    AdminHome,
    AuditorHome,
    OperatorHome,
    TechnicianHome,
    TechnicalInspections,
    DeliveryTracking,
    UserList,
    UserOptions,
    CollectionEvents,
    ConfirmationSuccess,
    ConfirmationDelete,
    ConfirmationDeactivate,
    RolePortal,
}

@Composable
fun App(darkTheme: Boolean? = null) {
    EcoTheme(darkTheme = darkTheme) {
        var screen by remember { mutableStateOf(Screen.Welcome) }
        var currentUser by remember { mutableStateOf<UserResponse?>(null) }
        val backStack = remember { ArrayDeque<Screen>() }

        fun navigateTo(target: Screen) {
            backStack.addLast(screen)
            screen = target
        }

        fun goBack() {
            if (backStack.isNotEmpty()) screen = backStack.removeLast()
        }

        fun homeForRole(user: UserResponse?): Screen = Screen.RolePortal

        when (screen) {
            Screen.Welcome -> WelcomeScreen(
                onContinue = { navigateTo(Screen.Login) },
            )
            Screen.Login -> LoginScreen(
                onBack = { goBack() },
                onNavigateToRegister = { navigateTo(Screen.Register) },
                onForgotPassword = { navigateTo(Screen.ForgotPassword) },
                onLoginSuccess = { user ->
                    currentUser = user
                    screen = homeForRole(user)
                }
            )
            Screen.Register -> RegisterScreen(
                onBackToLogin = { goBack() },
                onRegisterSuccess = { user ->
                    currentUser = user
                    screen = homeForRole(user)
                }
            )
            Screen.ForgotPassword -> ForgotPasswordScreen(
                onBack = { goBack() },
                onLogin = { goBack() },
            )
            Screen.CustomerHome -> CustomerHomeScreen(
                user = currentUser,
                onLogout = { AuthApi.clearSession(); currentUser = null; screen = Screen.Login },
                onOpenCatalog = { navigateTo(Screen.Catalog) },
                onSelectCollectionPoint = { navigateTo(Screen.CollectionPoint) },
                onCheckout = { navigateTo(Screen.OrderSummary) },
                onOpenProfile = { navigateTo(Screen.Profile) },
                onOpenNotifications = { navigateTo(Screen.Notifications) },
                onOpenChat = { navigateTo(Screen.Chat) },
            )
            Screen.Catalog -> CatalogScreen(
                onBack = { goBack() },
                onCheckout = { navigateTo(Screen.OrderSummary) },
            )
            Screen.CollectionPoint -> CollectionPointScreen(
                onBack = { goBack() },
                onContinue = { navigateTo(Screen.OrderSummary) },
            )
            Screen.OrderSummary -> OrderSummaryScreen(
                onBack = { goBack() },
                onContinue = { navigateTo(Screen.Payment) },
            )
            Screen.Payment -> PaymentScreen(
                onBack = { goBack() },
                onConfirm = { navigateTo(Screen.OrderConfirmation) },
            )
            Screen.OrderConfirmation -> OrderConfirmationScreen(
                onHome = { screen = homeForRole(currentUser) },
                onOpenProfile = { navigateTo(Screen.Profile) },
            )
            Screen.Profile -> ProfileScreen(
                user = currentUser,
                onBack = { goBack() },
                onOpenNotifications = { navigateTo(Screen.Notifications) },
                onOpenChat = { navigateTo(Screen.Chat) },
                onLogout = { AuthApi.clearSession(); currentUser = null; screen = Screen.Login },
            )
            Screen.Notifications -> NotificationsScreen(
                onBack = { goBack() },
                onOpenChat = { navigateTo(Screen.Chat) },
            )
            Screen.Chat -> ChatScreen(
                onBack = { goBack() },
                variant = if (currentUser?.role?.lowercase() in listOf("usuario", "cliente")) 2 else 1,
            )
            Screen.SellerHome -> SellerHomeScreen(
                user = currentUser,
                onLogout = { AuthApi.clearSession(); currentUser = null; screen = Screen.Login },
                onOpenSold = { navigateTo(Screen.SoldProducts) },
                onOpenBought = { navigateTo(Screen.BoughtProducts) },
                onOpenRecords = { navigateTo(Screen.CustomerRecords) },
                onOpenStats = { navigateTo(Screen.SellerStats) },
                onOpenChat = { navigateTo(Screen.Chat) },
                onOpenNotifications = { navigateTo(Screen.Notifications) },
            )
            Screen.SoldProducts -> SoldProductsScreen(
                onBack = { goBack() },
            )
            Screen.BoughtProducts -> BoughtProductsScreen(
                onBack = { goBack() },
            )
            Screen.CustomerRecords -> CustomerRecordsScreen(
                onBack = { goBack() },
            )
            Screen.SellerStats -> SellerStatsScreen(
                onBack = { goBack() },
            )
            Screen.AdminHome -> AdminHomeScreen(
                user = currentUser,
                onLogout = { AuthApi.clearSession(); currentUser = null; screen = Screen.Login },
                onOpenUsers = { navigateTo(Screen.UserList) },
                onOpenTracking = { navigateTo(Screen.DeliveryTracking) },
                onOpenStats = { navigateTo(Screen.SellerStats) },
                onOpenEvents = { navigateTo(Screen.CollectionEvents) },
                onOpenChat = { navigateTo(Screen.Chat) },
                onOpenNotifications = { navigateTo(Screen.Notifications) },
            )
            Screen.AuditorHome -> AuditorHomeScreen(
                user = currentUser,
                onLogout = { AuthApi.clearSession(); currentUser = null; screen = Screen.Login },
                onOpenUsers = { navigateTo(Screen.UserList) },
                onOpenStats = { navigateTo(Screen.SellerStats) },
                onOpenEvents = { navigateTo(Screen.CollectionEvents) },
            )
            Screen.OperatorHome -> OperatorHomeScreen(
                user = currentUser,
                onLogout = { currentUser = null; screen = Screen.Login },
                onOpenTracking = { navigateTo(Screen.DeliveryTracking) },
                onOpenEvents = { navigateTo(Screen.CollectionEvents) },
                onOpenChat = { navigateTo(Screen.Chat) },
                onOpenNotifications = { navigateTo(Screen.Notifications) },
            )
            Screen.TechnicianHome -> TechnicianHomeScreen(
                user = currentUser,
                onLogout = { currentUser = null; screen = Screen.Login },
                onOpenInspections = { navigateTo(Screen.TechnicalInspections) },
                onOpenChat = { navigateTo(Screen.Chat) },
                onOpenNotifications = { navigateTo(Screen.Notifications) },
            )
            Screen.TechnicalInspections -> TechnicalInspectionsScreen(
                onBack = { goBack() },
            )
            Screen.DeliveryTracking -> DeliveryTrackingScreen(
                onBack = { goBack() },
            )
            Screen.UserList -> UserListScreen(
                onBack = { goBack() },
                onOpenUserOptions = { navigateTo(Screen.UserOptions) },
            )
            Screen.UserOptions -> UserOptionsScreen(
                onBack = { goBack() },
                onSuccess = { navigateTo(Screen.ConfirmationSuccess) },
                onDelete = { navigateTo(Screen.ConfirmationDelete) },
                onDeactivate = { navigateTo(Screen.ConfirmationDeactivate) },
            )
            Screen.CollectionEvents -> CollectionEventsScreen(
                onBack = { goBack() },
            )
            Screen.ConfirmationSuccess -> ConfirmationScreen(
                type = ConfirmationType.Success,
                onDone = { screen = homeForRole(currentUser) },
            )
            Screen.ConfirmationDelete -> ConfirmationScreen(
                type = ConfirmationType.Delete,
                onDone = { screen = homeForRole(currentUser) },
            )
            Screen.ConfirmationDeactivate -> ConfirmationScreen(
                type = ConfirmationType.Deactivate,
                onDone = { screen = homeForRole(currentUser) },
            )
            Screen.RolePortal -> RolePortalScreen(
                user = currentUser,
                onLogout = { currentUser = null; screen = Screen.Login },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    App()
}