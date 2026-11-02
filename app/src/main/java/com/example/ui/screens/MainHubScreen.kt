package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.model.Student
import com.example.data.model.TrainingGroup
import com.example.ui.auth.signOutUser
import com.example.ui.viewmodel.SmartLabViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

enum class ScreenTab(val title: String, val icon: ImageVector) {
    DASHBOARD("الرئيسية", Icons.Default.Dashboard),
    GROUPS("المجاميع", Icons.Default.Groups),
    STUDENTS("الطلاب", Icons.Default.Person),
    FINANCE("المالية", Icons.Default.AccountBalanceWallet),
    SCHEDULE("الجدول والأرشيف", Icons.Default.CalendarMonth)
}

sealed interface NavDestination {
    data class Tab(val tab: ScreenTab) : NavDestination
    data class GroupDetail(val group: TrainingGroup) : NavDestination
    data class StudentProfile(val student: Student) : NavDestination
    data class GroupPrint(val group: TrainingGroup) : NavDestination
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHubScreen(
    viewModel: SmartLabViewModel,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onSignOutRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var currentDestination by remember { mutableStateOf<NavDestination>(NavDestination.Tab(ScreenTab.DASHBOARD)) }
    var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }

    val actionMsg by viewModel.actionMessage.collectAsState()
    LaunchedEffect(actionMsg) {
        actionMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    val studentsState by viewModel.studentsState.collectAsState()
    val currentUser = Firebase.auth.currentUser

    // Handle Android system Back button
    BackHandler(enabled = currentDestination !is NavDestination.Tab) {
        currentDestination = NavDestination.Tab(currentTab)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SMARTLAB Management",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "متصل سحابياً: ${currentUser?.displayName ?: currentUser?.email ?: "المركز"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onToggleDarkTheme,
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkTheme) "الوضع النهاري" else "الوضع الليلي",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                signOutUser(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onSignOutComplete = onSignOutRequested,
                                    scope = coroutineScope
                                )
                            },
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "تسجيل الخروج",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!isExpandedScreen && currentDestination is NavDestination.Tab) {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        ScreenTab.values().forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = {
                                    currentTab = tab
                                    currentDestination = NavDestination.Tab(tab)
                                },
                                icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                                label = { Text(tab.title, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen && currentDestination is NavDestination.Tab) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ScreenTab.values().forEach { tab ->
                            NavigationRailItem(
                                selected = currentTab == tab,
                                onClick = {
                                    currentTab = tab
                                    currentDestination = NavDestination.Tab(tab)
                                },
                                icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                                label = { Text(tab.title, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (val dest = currentDestination) {
                        is NavDestination.Tab -> {
                            when (dest.tab) {
                                ScreenTab.DASHBOARD -> {
                                    DashboardScreen(
                                        viewModel = viewModel,
                                        onNavigateToGroups = {
                                            currentTab = ScreenTab.GROUPS
                                            currentDestination = NavDestination.Tab(ScreenTab.GROUPS)
                                        },
                                        onNavigateToStudents = {
                                            currentTab = ScreenTab.STUDENTS
                                            currentDestination = NavDestination.Tab(ScreenTab.STUDENTS)
                                        },
                                        onNavigateToFinance = {
                                            currentTab = ScreenTab.FINANCE
                                            currentDestination = NavDestination.Tab(ScreenTab.FINANCE)
                                        },
                                        onSelectGroup = { group ->
                                            currentDestination = NavDestination.GroupDetail(group)
                                        },
                                        onSelectStudent = { student ->
                                            currentDestination = NavDestination.StudentProfile(student)
                                        }
                                    )
                                }
                                ScreenTab.GROUPS -> {
                                    GroupsScreen(
                                        viewModel = viewModel,
                                        onSelectGroup = { group ->
                                            currentDestination = NavDestination.GroupDetail(group)
                                        }
                                    )
                                }
                                ScreenTab.STUDENTS -> {
                                    StudentsScreen(
                                        viewModel = viewModel,
                                        onSelectStudent = { student ->
                                            currentDestination = NavDestination.StudentProfile(student)
                                        }
                                    )
                                }
                                ScreenTab.FINANCE -> {
                                    FinanceScreen(
                                        viewModel = viewModel
                                    )
                                }
                                ScreenTab.SCHEDULE -> {
                                    ScheduleAndArchiveScreen(
                                        viewModel = viewModel,
                                        onSelectGroup = { group ->
                                            currentDestination = NavDestination.GroupDetail(group)
                                        },
                                        onSelectStudent = { student ->
                                            currentDestination = NavDestination.StudentProfile(student)
                                        }
                                    )
                                }
                            }
                        }
                        is NavDestination.GroupDetail -> {
                            GroupDetailScreen(
                                group = dest.group,
                                viewModel = viewModel,
                                onBack = { currentDestination = NavDestination.Tab(currentTab) },
                                onPrintList = { currentDestination = NavDestination.GroupPrint(dest.group) },
                                onSelectStudent = { student ->
                                    currentDestination = NavDestination.StudentProfile(student)
                                }
                            )
                        }
                        is NavDestination.StudentProfile -> {
                            StudentProfileScreen(
                                student = dest.student,
                                viewModel = viewModel,
                                onBack = { currentDestination = NavDestination.Tab(currentTab) }
                            )
                        }
                        is NavDestination.GroupPrint -> {
                            val allStudents = (studentsState as? com.example.ui.viewmodel.UiState.Success)?.data ?: emptyList()
                            val groupStudents = allStudents.filter { it.groupId == dest.group.id && !it.isArchived }
                            GroupPrintScreen(
                                group = dest.group,
                                students = groupStudents,
                                onBack = { currentDestination = NavDestination.GroupDetail(dest.group) }
                            )
                        }
                    }
                }
            }
        }
    }
}
