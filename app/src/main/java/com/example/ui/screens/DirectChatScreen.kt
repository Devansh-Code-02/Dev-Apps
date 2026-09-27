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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.local.entity.MessageEntity
import com.example.data.preferences.UIStyleTheme
import com.example.ui.components.E2eeBadge
import com.example.ui.components.E2eeFingerprintDialog
import com.example.ui.components.FileTransferCard
import com.example.ui.components.ThemeCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBgStart
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectChatScreen(
    chatTitle: String,
    chatId: String,
    messages: List<MessageEntity>,
    themeStyle: UIStyleTheme,
    isDark: Boolean,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendFile: (fileName: String, size: Long, type: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var showFingerprint by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showFingerprint) {
        E2eeFingerprintDialog(
            peerName = chatTitle,
            digest = "8F3A-42C9",
            publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQE...",
            onDismiss = { showFingerprint = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = chatTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "P2P Bluetooth • Encrypted Session",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    E2eeBadge(
                        keyDigest = "8F3A",
                        onClick = { showFingerprint = true }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBgStart)
            )
        },
        containerColor = DarkBgStart,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Message List
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(messages) { message ->
                    ChatMessageBubble(
                        message = message,
                        themeStyle = themeStyle,
                        isDark = isDark
                    )
                }
            }

            // Input Bar
            ThemeCard(
                styleTheme = themeStyle,
                isDark = isDark,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                onSendFile("photo_snapshot_${System.currentTimeMillis().toString().takeLast(4)}.jpg", 2450000L, "IMAGE")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Send Image",
                                tint = CyberCyan
                            )
                        }

                        IconButton(
                            onClick = {
                                onSendFile("document_mesh_spec.pdf", 1850000L, "FILE")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Send Document",
                                tint = CyberCyan
                            )
                        }

                        IconButton(
                            onClick = {
                                onSendFile("voice_note_${System.currentTimeMillis().toString().takeLast(4)}.aac", 320000L, "VOICE")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Send Voice Note",
                                tint = CyberCyan
                            )
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Offline encrypted message...", fontSize = 13.sp, color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    onSendMessage(inputText)
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CyberCyan)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: MessageEntity,
    themeStyle: UIStyleTheme,
    isDark: Boolean
) {
    val isMine = message.isMine
    val align = if (isMine) Alignment.End else Alignment.Start
    val bubbleBg = if (isMine) CyberCyan.copy(alpha = 0.25f) else NeonBlue.copy(alpha = 0.15f)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Column(
        horizontalAlignment = align,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 16.dp
                    )
                )
                .background(bubbleBg)
                .padding(12.dp)
        ) {
            Column {
                if (!isMine && message.isGroup) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                if (message.messageType == "FILE" || message.messageType == "IMAGE" || message.messageType == "VOICE") {
                    FileTransferCard(
                        fileName = message.fileName ?: "Shared Attachment",
                        fileSize = message.fileSize,
                        transferProgress = message.transferProgress,
                        messageType = message.messageType,
                        isMine = isMine
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.decryptedText,
                    fontSize = 14.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}
