package com.example.mymoneynotes.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FinanceDarkColorScheme = darkColorScheme(
    primary = FinanceTeal,
    secondary = FinanceTealDark,
    tertiary = IncomeGreen,
    background = FinanceBackground,
    surface = FinanceSurface,
    surfaceVariant = FinanceSurfaceVariant,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White
)

@Composable
fun MyMoneyNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force custom theme
    content: @Composable () -> Unit
) {
    val colorScheme = FinanceDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}