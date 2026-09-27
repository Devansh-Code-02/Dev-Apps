package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GroupRoomEntity
import com.example.data.preferences.UIStyleTheme
import com.example.ui.components.CreateGroupRoomDialog
import com.example.ui.components.ThemeCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBgStart
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonBlue

@Composable
fun GroupRoomsScreen(
    groups: List<GroupRoomEntity>,
    themeStyle: UIStyleTheme,
    isDark: Boolean,
    isCreatingGroup: Boolean,
    onOpenGroupChat: (GroupRoomEntity) -> Unit,
    onStartCreateGroup: () -> Unit,
    onConfirmCreateGroup: (name: String, desc: String) -> Unit,
    onDismissCreateGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isCreatingGroup) {
        CreateGroupRoomDialog(
            onDismiss = onDismissCreateGroup,
            onConfirm = onConfirmCreateGroup
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartCreateGroup,
                containerColor = CyberCyan,
                contentColor = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Group Room"
                )
            }
        },
        containerColor = DarkBgStart,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "OFFLINE P2P GROUP CHAT ROOMS (${groups.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (groups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "No Groups",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active P2P group rooms",
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
                    items(groups) { room ->
                        GroupRoomCard(
                            room = room,
                            styleTheme = themeStyle,
                            isDark = isDark,
                            onClick = { onOpenGroupChat(room) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupRoomCard(
    room: GroupRoomEntity,
    styleTheme: UIStyleTheme,
    isDark: Boolean,
    onClick: () -> Unit
) {
    ThemeCard(
        styleTheme = styleTheme,
        isDark = isDark,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(NeonBlue.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Room",
                    tint = NeonBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = room.roomName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "E2EE Group",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Text(
                    text = room.description,
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}
