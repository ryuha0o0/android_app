package com.moa.finance.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moa.finance.ui.components.LocalShowMessage
import com.moa.finance.ui.screens.SecurityLabScreen
import com.moa.finance.ui.screens.MainBoardScreen
import com.moa.finance.security.LocalSession
import com.moa.finance.data.MoaRepository
import androidx.compose.runtime.LaunchedEffect
import com.moa.finance.ui.screens.AccountManageScreen
import com.moa.finance.ui.screens.CardScreen
import com.moa.finance.ui.screens.EditProfileScreen
import com.moa.finance.ui.screens.HomeScreen
import com.moa.finance.ui.screens.InquiryScreen
import com.moa.finance.ui.screens.LoginScreen
import com.moa.finance.ui.screens.MyPageScreen
import com.moa.finance.ui.screens.SignUpScreen
import com.moa.finance.ui.screens.TransferScreen
import com.moa.finance.ui.theme.MoaBackground
import com.moa.finance.ui.theme.MoaBlue
import com.moa.finance.ui.theme.MoaSurface
import com.moa.finance.ui.theme.MoaTextTertiary
import kotlinx.coroutines.launch

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val HOME = "home"
    const val INQUIRY = "inquiry"
    const val TRANSFER = "transfer"
    const val CARD = "card"
    const val MY = "my"
    const val EDIT_PROFILE = "my/profile"
    const val ACCOUNT_MANAGE = "my/account"
    const val MAIN_BOARD = "main"
    const val SECURITY_LAB = "my/security-lab"

    fun inquiry(tab: Int = 0, accountId: String? = null) =
        "$INQUIRY?tab=$tab" + (accountId?.let { "&account=$it" } ?: "")

    fun transfer(tab: Int = 0, accountId: String? = null) =
        "$TRANSFER?tab=$tab" + (accountId?.let { "&account=$it" } ?: "")

    fun card(tab: Int = 0) = "$CARD?tab=$tab"
}

private enum class BottomTab(val label: String, val icon: ImageVector, val routePrefix: String, val route: String) {
    Home("홈", Icons.Outlined.Home, Routes.HOME, Routes.HOME),
    Inquiry("조회", Icons.Outlined.BarChart, Routes.INQUIRY, Routes.inquiry()),
    Transfer("이체", Icons.Outlined.SwapHoriz, Routes.TRANSFER, Routes.transfer()),
    Card("카드", Icons.Outlined.CreditCard, Routes.CARD, Routes.card()),
    My("마이", Icons.Outlined.Person, Routes.MY, Routes.MY),
}

@Composable
fun MoaApp(deepLinkHost: String? = null) {
    val navController = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val session = LocalSession.current
    val showMessage: (String) -> Unit = remember {
        { message ->
            scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar(message)
            }
        }
    }

    // 자동로그인 시도 + 딥링크 강제 진입 처리 (최초 1회)
    LaunchedEffect(Unit) {
        val autologin = session.tryAutoLogin()
        when {
            deepLinkHost == "password-change" -> {
                // 화면 강제실행: 로그인 없이 비밀번호 변경 화면으로 진입 (인증단계 우회 실습)
                navController.navigate(Routes.EDIT_PROFILE) { popUpTo(Routes.LOGIN) { inclusive = true } }
            }
            autologin -> navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
        }
    }

    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route.orEmpty()
    val currentTab = BottomTab.entries.firstOrNull { route.startsWith(it.routePrefix) }

    CompositionLocalProvider(LocalShowMessage provides showMessage) {
        Scaffold(
            containerColor = MoaBackground,
            contentWindowInsets = WindowInsets.navigationBars,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                if (currentTab != null) {
                    MoaBottomBar(currentTab) { tab -> navController.navigateToTab(tab.route) }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.LOGIN,
                modifier = Modifier.padding(padding),
            ) {
                composable(Routes.LOGIN) {
                    LoginScreen(
                        onLogin = {
                            session.login(MoaRepository.user.name)
                            session.enableAutoLogin(MoaRepository.user.name)
                            navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                        },
                        onSignUp = { navController.navigate(Routes.SIGN_UP) },
                        onBack = { showMessage("Labbit에 오신 걸 환영해요") },
                    )
                }
                composable(Routes.SIGN_UP) {
                    SignUpScreen(
                        onBack = { navController.popBackStack() },
                        onComplete = {
                            session.login(MoaRepository.user.name)
                            navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                        },
                    )
                }
                composable(Routes.HOME) {
                    HomeScreen(
                        onAccounts = { navController.navigateToTab(Routes.inquiry()) },
                        onTransfer = { id -> navController.navigate(Routes.transfer(accountId = id)) },
                        onWithdraw = { navController.navigate(Routes.transfer(tab = 1)) },
                        onCards = { navController.navigate(Routes.card(tab = 1)) },
                        onAccountDetail = { id -> navController.navigate(Routes.inquiry(tab = 1, accountId = id)) },
                    )
                }
                composable(
                    "${Routes.INQUIRY}?tab={tab}&account={account}",
                    arguments = listOf(
                        navArgument("tab") { type = NavType.IntType; defaultValue = 0 },
                        navArgument("account") { type = NavType.StringType; nullable = true; defaultValue = null },
                    ),
                ) { entry ->
                    InquiryScreen(
                        initialTab = entry.arguments?.getInt("tab") ?: 0,
                        initialAccountId = entry.arguments?.getString("account"),
                        onBack = { navController.navigateUpOrHome() },
                        onTransfer = { id -> navController.navigate(Routes.transfer(accountId = id)) },
                    )
                }
                composable(
                    "${Routes.TRANSFER}?tab={tab}&account={account}",
                    arguments = listOf(
                        navArgument("tab") { type = NavType.IntType; defaultValue = 0 },
                        navArgument("account") { type = NavType.StringType; nullable = true; defaultValue = null },
                    ),
                ) { entry ->
                    TransferScreen(
                        initialTab = entry.arguments?.getInt("tab") ?: 0,
                        initialAccountId = entry.arguments?.getString("account"),
                        onBack = { navController.navigateUpOrHome() },
                        onTransferDone = {
                            navController.navigate(Routes.inquiry(tab = 2)) {
                                popUpTo(Routes.HOME)
                            }
                        },
                    )
                }
                composable(
                    "${Routes.CARD}?tab={tab}",
                    arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 }),
                ) { entry ->
                    CardScreen(
                        initialTab = entry.arguments?.getInt("tab") ?: 0,
                        onBack = { navController.navigateUpOrHome() },
                    )
                }
                composable(Routes.MY) {
                    MyPageScreen(
                        onBack = { navController.navigateUpOrHome() },
                        onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                        onManageAccount = { navController.navigate(Routes.ACCOUNT_MANAGE) },
                        onSecurityLab = { navController.navigate(Routes.SECURITY_LAB) },
                        onLogout = {
                            session.logout()
                            navController.navigate(Routes.LOGIN) { popUpTo(navController.graph.id) { inclusive = true } }
                        },
                    )
                }
                composable(Routes.EDIT_PROFILE) {
                    EditProfileScreen(onBack = { navController.navigateUpOrHome() })
                }
                composable(Routes.ACCOUNT_MANAGE) {
                    AccountManageScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.MAIN_BOARD) {
                    MainBoardScreen(onBack = { navController.navigateUpOrHome() })
                }
                composable(Routes.SECURITY_LAB) {
                    SecurityLabScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME)
        launchSingleTop = true
    }
}

/** Top-bar back: pop if there is somewhere to go, otherwise land on home. */
private fun NavHostController.navigateUpOrHome() {
    if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        navigate(Routes.HOME) { popUpTo(graph.id) { inclusive = true } }
    }
}

@Composable
private fun MoaBottomBar(current: BottomTab, onSelect: (BottomTab) -> Unit) {
    NavigationBar(containerColor = MoaSurface, tonalElevation = 0.dp) {
        BottomTab.entries.forEach { tab ->
            val selected = tab == current
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = {
                    Text(tab.label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MoaBlue,
                    selectedTextColor = MoaBlue,
                    unselectedIconColor = MoaTextTertiary,
                    unselectedTextColor = MoaTextTertiary,
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
}
