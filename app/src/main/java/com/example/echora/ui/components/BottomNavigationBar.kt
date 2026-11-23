package com.example.echora.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.echora.ui.theme.EchoraPrimary
import com.example.echora.ui.theme.EchoraSurface
import com.example.echora.ui.theme.EchoraTextSecondary

@Composable
fun BottomNavigationBar(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        Triple("home", "Home", Icons.Default.Home),
        Triple("search", "Search", Icons.Default.Search),
        Triple("library", "Library", Icons.Default.LibraryMusic),
        Triple("profile", "Profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = EchoraSurface,
        contentColor = Color.White
    ) {
        items.forEach { (route, label, icon) ->
            val selected = currentScreen == route
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = selected,
                onClick = { onNavigate(route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = EchoraPrimary,
                    selectedTextColor = EchoraPrimary,
                    unselectedIconColor = EchoraTextSecondary,
                    unselectedTextColor = EchoraTextSecondary,
                    indicatorColor = EchoraSurface
                )
            )
        }
    }
}
