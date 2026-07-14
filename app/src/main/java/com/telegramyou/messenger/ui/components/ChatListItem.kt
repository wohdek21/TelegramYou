package com.telegramyou.messenger.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.telegramyou.messenger.engine.TelegramManager
import org.drinkless.tdlib.TdApi

fun TdApi.Message?.previewText(): String {

    if (this == null) return "No messages"

    return when (val content = content) {
        is TdApi.MessageText ->
            content.text.text

        is TdApi.MessagePhoto ->
            "\uD83D\uDDBC Фото"

        is TdApi.MessageVideo ->
            "\uD83C\uDFA5 Видео"

        is TdApi.MessageAnimation ->
            "\uD83C\uDF9E GIF"

        is TdApi.MessageSticker ->
            "\uD83D\uDE03 Стикер"

        is TdApi.MessageVoiceNote ->
            "\uD83C\uDFA4 Голосовое сообщение"

        is TdApi.MessageAudio ->
            "\uD83C\uDFB5 Аудио"

        is TdApi.MessageDocument ->
            "\uD83D\uDCC4 ${content.document.fileName}"

        is TdApi.MessageLocation ->
            "\uD83D\uDCCD Локация"

        is TdApi.MessageContact ->
            "\uD83D\uDC64 Контакт"

        is TdApi.MessagePoll ->
            "\uD83D\uDCCA ${content.poll.question}"

        else ->
            content.javaClass.simpleName
    }
}
@Composable
fun ChatListItem(chat: TdApi.Chat, manager: TelegramManager) {
    // Observe the downloaded files map
    val downloadedFiles by manager.downloadedFiles.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- AVATAR LOGIC ---
        val photoFile = chat.photo?.small
        var imagePath: String? = null

        if (photoFile != null) {
            if (photoFile.local.isDownloadingCompleted) {
                // Image is already on device
                imagePath = photoFile.local.path
            } else {
                // Check if it just finished downloading in our state map
                imagePath = downloadedFiles[photoFile.id]
                // If not, tell the C++ engine to start downloading it right now
                if (imagePath == null) {
                    manager.downloadFile(photoFile.id)
                }
            }
        }

        if (imagePath != null) {
            // Render the downloaded local file using Coil
            AsyncImage(
                model = imagePath,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback Placeholder while downloading or if no photo exists
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = chat.title.take(1).uppercase(),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))
        Log.d("TDLIB", "chat=${chat.title} lastMessage=${chat.lastMessage}")
        // --- TEXT LOGIC ---
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )

            Text(
                text = chat.lastMessage.previewText(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}