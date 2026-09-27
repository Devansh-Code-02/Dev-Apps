package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PeerEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBgStart
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonBlue
import com.example.utils.QrCodeEncoder
import org.json.JSONObject

@Composable
fun QrCodeView(
    payloadText: String,
    modifier: Modifier = Modifier,
    qrSizeDp: Dp = 220.dp,
    moduleColor: Color = Color.White,
    backgroundColor: Color = Color(0xFF0F172A)
) {
    val matrix = remember(payloadText) {
        QrCodeEncoder.generateQrMatrix(payloadText, size = 29)
    }

    Box(
        modifier = modifier
            .size(qrSizeDp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.5.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(qrSizeDp - 32.dp)) {
            val rows = matrix.size
            val cols = matrix[0].size
            val moduleWidth = size.width / cols
            val moduleHeight = size.height / rows

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (matrix[r][c]) {
                        drawRoundRect(
                            color = moduleColor,
                            topLeft = Offset(c * moduleWidth, r * moduleHeight),
                            size = Size(moduleWidth * 0.92f, moduleHeight * 0.92f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
            }
        }

        // Central E2EE Logo Badge overlay
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CyberCyan)
                .border(2.dp, backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun QrCodeGeneratorDialog(
    localDeviceName: String,
    localMacAddress: String = "FA:8C:3B:9E:11:42",
    keyDigest: String = "8F3A-42C9",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val jsonPayload = remember(localDeviceName, localMacAddress, keyDigest) {
        JSONObject().apply {
            put("mac", localMacAddress)
            put("name", localDeviceName)
            put("key", keyDigest)
            put("type", "PHONE")
            put("protocol", "BT_P2P_E2EE")
        }.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBgStart,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "My P2P Security QR Code",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Have another peer scan this QR code to instantly exchange RSA-2048 public keys and pair offline.",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                QrCodeView(payloadText = jsonPayload)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Key Digest: $keyDigest • $localDeviceName",
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("P2P Payload", jsonPayload))
                    Toast.makeText(context, "P2P Public Key copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Payload", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.Gray)
            }
        }
    )
}

@Composable
fun QrCodeScannerDialog(
    onDismiss: () -> Unit,
    onScannedPeer: (PeerEntity) -> Unit
) {
    val context = LocalContext.current
    var manualInput by remember { mutableStateOf("") }
    var flashOn by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "LaserScan")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserY"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBgStart,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan Peer QR Code",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                IconButton(onClick = { flashOn = !flashOn }) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Torch",
                        tint = if (flashOn) CyberCyan else Color.Gray
                    )
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Point camera at peer device QR code or test with quick simulation nodes:",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Camera Scanner Simulation Viewfinder Box
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .border(
                            width = 2.dp,
                            color = if (flashOn) CyberCyan else Color.Gray.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(190.dp)) {
                        val stroke = 3.dp.toPx()
                        val len = 24.dp.toPx()

                        // Corner 1: Top-Left
                        drawLine(CyberCyan, Offset(0f, 0f), Offset(len, 0f), stroke)
                        drawLine(CyberCyan, Offset(0f, 0f), Offset(0f, len), stroke)

                        // Corner 2: Top-Right
                        drawLine(CyberCyan, Offset(size.width, 0f), Offset(size.width - len, 0f), stroke)
                        drawLine(CyberCyan, Offset(size.width, 0f), Offset(size.width, len), stroke)

                        // Corner 3: Bottom-Left
                        drawLine(CyberCyan, Offset(0f, size.height), Offset(len, size.height), stroke)
                        drawLine(CyberCyan, Offset(0f, size.height), Offset(0f, size.height - len), stroke)

                        // Corner 4: Bottom-Right
                        drawLine(CyberCyan, Offset(size.width, size.height), Offset(size.width - len, size.height), stroke)
                        drawLine(CyberCyan, Offset(size.width, size.height), Offset(size.width, size.height - len), stroke)

                        // Moving Laser Line
                        val y = size.height * laserY
                        drawLine(
                            color = EmeraldGreen,
                            start = Offset(10f, y),
                            end = Offset(size.width - 10f, y),
                            strokeWidth = 4.dp.toPx()
                        )
                    }

                    Text(
                        text = "Align QR Code inside box",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 150.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "QUICK SCAN TEST PEERS:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val peer = PeerEntity(
                                macAddress = "B4:E9:12:8A:4D:30",
                                deviceName = "Sarah's Pixel 7",
                                deviceType = "PHONE",
                                rssi = -48,
                                isSaved = true,
                                isOnline = true,
                                customAlias = "Sarah (QR Verified)"
                            )
                            onScannedPeer(peer)
                            Toast.makeText(context, "Scanned & Verified: Sarah's Pixel 7", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sarah's Phone", fontSize = 10.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            val peer = PeerEntity(
                                macAddress = "C8:3F:89:10:E2:5A",
                                deviceName = "Tactical Command Node",
                                deviceType = "TABLET",
                                rssi = -52,
                                isSaved = true,
                                isOnline = true,
                                customAlias = "Command Tab"
                            )
                            onScannedPeer(peer)
                            Toast.makeText(context, "Scanned & Verified: Command Tab", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Command Tab", fontSize = 10.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    placeholder = { Text("Or paste raw QR JSON string...", fontSize = 11.sp, color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (manualInput.isNotBlank()) {
                        try {
                            val json = JSONObject(manualInput)
                            val mac = json.optString("mac", "AA:BB:CC:DD:EE:FF")
                            val name = json.optString("name", "Scanned Node")
                            val type = json.optString("type", "PHONE")

                            val peer = PeerEntity(
                                macAddress = mac,
                                deviceName = name,
                                deviceType = type,
                                rssi = -55,
                                isSaved = true,
                                isOnline = true
                            )
                            onScannedPeer(peer)
                            Toast.makeText(context, "Scanned & Paired with $name!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Invalid QR JSON payload", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("Process String", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
