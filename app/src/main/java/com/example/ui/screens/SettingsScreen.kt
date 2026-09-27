package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UIStyleTheme
import com.example.ui.components.EditNameDialog
import com.example.ui.components.ThemeCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonBlue

@Composable
fun SettingsScreen(
    themeStyle: UIStyleTheme,
    isDark: Boolean,
    localDeviceName: String,
    isDiscoverable: Boolean,
    onSelectThemeStyle: (UIStyleTheme) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onUpdateDeviceName: (String) -> Unit,
    onToggleDiscoverable: (Boolean) -> Unit,
    isEditingName: Boolean,
    onSetEditingName: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    if (isEditingName) {
        EditNameDialog(
            title = "Edit My Device Name",
            currentValue = localDeviceName,
            onDismiss = { onSetEditingName(false) },
            onConfirm = { newName ->
                onUpdateDeviceName(newName)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // UI Visual Theme Selection Section
        Text(
            text = "VISUAL UI THEME STYLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemeStyleCard(
                title = "Glassmorphism",
                subtitle = "Acrylic translucency & specular glow",
                isSelected = themeStyle == UIStyleTheme.GLASSMORPHISM,
                onClick = { onSelectThemeStyle(UIStyleTheme.GLASSMORPHISM) },
                modifier = Modifier.weight(1f)
            )

            ThemeStyleCard(
                title = "Neomorphism",
                subtitle = "Soft extruded dual light shadows",
                isSelected = themeStyle == UIStyleTheme.NEOMORPHISM,
                onClick = { onSelectThemeStyle(UIStyleTheme.NEOMORPHISM) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance
        ThemeCard(
            styleTheme = themeStyle,
            isDark = isDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Theme Mode",
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Dark Mode Canvas",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isDark) "Enabled" else "Light Canvas",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Switch(
                    checked = isDark,
                    onCheckedChange = onToggleDarkMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = CyberCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bluetooth Device Name Section
        Text(
            text = "BLUETOOTH & P2P NETWORK IDENTIFIER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ThemeCard(
            styleTheme = themeStyle,
            isDark = isDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = "Bluetooth Device",
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Local Device Name",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = localDeviceName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }

                    IconButton(onClick = { onSetEditingName(true) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Local Device Name",
                            tint = CyberCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Discoverable in Offline Environment",
                        fontSize = 13.sp,
                        color = Color.White
                    )

                    Switch(
                        checked = isDiscoverable,
                        onCheckedChange = onToggleDiscoverable,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = CyberCyan
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Security & Cryptography
        Text(
            text = "END-TO-END ENCRYPTION KEYS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ThemeCard(
            styleTheme = themeStyle,
            isDark = isDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Keys",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Local RSA-2048 Keypair Active",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "AES-GCM-256 cipher initialized for offline channels",
                        fontSize = 11.sp,
                        color = EmeraldGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ThemeStyleCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) CyberCyan.copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.6f))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isSelected) CyberCyan else Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.LightGray
            )
        }
    }
}
