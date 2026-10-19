
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
fun PhoneNumberScreen(
    onSubmit: (String, String) -> Unit
) {
    var countryCode by remember { mutableStateOf("+7") }
    var phoneNumber by remember { mutableStateOf("") }

    AuthScreenLayout(
        title = "Добро пожаловать :3",
        description = "Введите номер телефона, чтобы войти в Telegram You."
    ) {
        OutlinedTextField(
            value = countryCode,
            onValueChange = { countryCode = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Код страны") },
            placeholder = { Text("+7") },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            )
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Номер телефона") },
            placeholder = { Text("900 123-45-67") },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            )
        )

        Button(
            onClick = {
                onSubmit(countryCode, phoneNumber)
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(Modifier),
            enabled = phoneNumber.isNotBlank() &&
                countryCode.isNotBlank(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Продолжить")
        }
    }
}
