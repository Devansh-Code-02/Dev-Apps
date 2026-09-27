package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan

@Composable
fun FileTransferCard(
    fileName: String,
    fileSize: Long,
    transferProgress: Float,
    messageType: String,
    isMine: Boolean,
    modifier: Modifier = Modifier
) {
    val icon = when (messageType) {
        "IMAGE" -> Icons.Default.Image
        "VOICE" -> Icons.Default.Mic
        else -> Icons.Default.AttachFile
    }

    val formattedSize = if (fileSize > 1024 * 1024) {
        "%.1f MB".format(fileSize / (1024.0 * 1024.0))
    } else {
        "%.1f KB".format(fileSize / 1024.0)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoxIcon(icon)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (transferProgress >= 1.0f) "$formattedSize • Transferred" else "$formattedSize • Sending ${(transferProgress * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }
            if (transferProgress >= 1.0f) {
                IconButton(onClick = { /* Open file */ }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Open File",
                        tint = CyberCyan
                    )
                }
            }
        }

        if (transferProgress < 1.0f) {
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { transferProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = CyberCyan,
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun BoxIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CyberCyan.copy(alpha = 0.2f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyberCyan,
            modifier = Modifier
                .padding(6.dp)
                .size(24.dp)
        )
    }
}
