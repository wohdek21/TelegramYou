
package com.telegramyou.messenger.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun VerificationCodeScreen(
    onSubmit: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    AuthScreenLayout(
        title = "Проверьте сообщения",
        description = "Введите код подтверждения, который Telegram отправил вам."
    ) {
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Код подтверждения") },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        Button(
            onClick = { onSubmit(code) },
            modifier = Modifier.fillMaxWidth(),
            enabled = code.isNotBlank(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Подтвердить")
        }
    }
}
