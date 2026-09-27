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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PeerEntity
import com.example.data.preferences.UIStyleTheme
import com.example.ui.components.EditNameDialog
import com.example.ui.components.SignalStrengthMeter
import com.example.ui.components.ThemeCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonBlue

@Composable
fun ContactListScreen(
    contacts: List<PeerEntity>,
    themeStyle: UIStyleTheme,
    isDark: Boolean,
    onOpenChat: (PeerEntity) -> Unit,
    onUpdateAlias: (mac: String, alias: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var editingPeer by remember { mutableStateOf<PeerEntity?>(null) }

    val filteredContacts = contacts.filter {
        it.getDisplayName().contains(searchQuery, ignoreCase = true) ||
                it.macAddress.contains(searchQuery, ignoreCase = true)
    }

    if (editingPeer != null) {
        EditNameDialog(
            title = "Edit Peer Alias",
            currentValue = editingPeer?.getDisplayName() ?: "",
            onDismiss = { editingPeer = null },
            onConfirm = { newAlias ->
                editingPeer?.let { onUpdateAlias(it.macAddress, newAlias) }
                editingPeer = null
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search P2P contacts or devices...", color = Color.Gray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SAVED P2P CONTACTS (${filteredContacts.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyberCyan,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (filteredContacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "No Contacts",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No saved P2P contacts found",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredContacts) { contact ->
                    ContactCard(
                        peer = contact,
                        styleTheme = themeStyle,
                        isDark = isDark,
                        onOpenChat = { onOpenChat(contact) },
                        onEditAlias = { editingPeer = contact }
                    )
                }
            }
        }
    }
}

@Composable
fun ContactCard(
    peer: PeerEntity,
    styleTheme: UIStyleTheme,
    isDark: Boolean,
    onOpenChat: () -> Unit,
    onEditAlias: () -> Unit
) {
    val deviceIcon = when (peer.deviceType) {
        "SPEAKER" -> Icons.Default.Speaker
        "TABLET" -> Icons.Default.Tablet
        "LAPTOP" -> Icons.Default.Laptop
        "HEADSET" -> Icons.Default.Headset
        else -> Icons.Default.PhoneAndroid
    }

    ThemeCard(
        styleTheme = styleTheme,
        isDark = isDark,
        onClick = onOpenChat,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(EmeraldGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = deviceIcon,
                    contentDescription = peer.deviceType,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = peer.getDisplayName(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (peer.isOnline) EmeraldGreen else Color.Gray)
                    )
                }

                Text(
                    text = "Hardware Name: ${peer.deviceName}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                SignalStrengthMeter(rssi = peer.rssi)
            }

            IconButton(onClick = onEditAlias) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Alias",
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onOpenChat) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chat",
                    tint = CyberCyan
                )
            }
        }
    }
}
