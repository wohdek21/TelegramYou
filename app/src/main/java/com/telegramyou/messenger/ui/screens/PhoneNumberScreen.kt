
package com.telegramyou.messenger.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PhoneNumberScreen(
    onSubmit: (String, String) -> Unit
) {
    var countryCode by rememberSaveable { mutableStateOf("+7") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val countryDigits = countryCode.count { it.isDigit() }
    val phoneDigits = phoneNumber.count { it.isDigit() }
    val maxPhoneDigits = (15 - countryDigits).coerceAtLeast(0)

    AuthScreenLayout(
        title = "Вход в Telegram",
        description = "Введите номер телефона, чтобы продолжить."
    ) {
        OutlinedTextField(
            value = countryCode,
            onValueChange = { input ->
                val filtered = input.filter {
                    it.isDigit() || it == '+' 
                }

                val normalized = if (filtered.contains('+')) {
                    "+" + filtered.filter { it.isDigit() }
                } else {
                    filtered.filter { it.isDigit() }
                }

                if (normalized.count { it.isDigit() } <= 3) {
                    countryCode = normalized
                }
            },
            label = { Text("Код страны") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            singleLine = true,
            enabled = !isLoading,
            shape = OutlinedTextFieldDefaults.shape,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { input ->
                val filtered = input.filter {
                    it.isDigit() || it == ' ' ||
                        it == '-' || it == '(' || it == ')'
                }

                if (filtered.count { it.isDigit() } <= maxPhoneDigits) {
                    phoneNumber = filtered
                }
            },
            label = { Text("Номер телефона") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            singleLine = true,
            enabled = !isLoading,
            shape = OutlinedTextFieldDefaults.shape,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                isLoading = true

                val cleanCountryCode =
                    countryCode.filter { it.isDigit() }

                val cleanPhoneNumber =
                    phoneNumber.filter { it.isDigit() }

                onSubmit(
                    "+$cleanCountryCode",
                    cleanPhoneNumber
                )
            },
            enabled = countryDigits in 1..3 &&
                phoneDigits in 1..maxPhoneDigits &&
                !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Продолжить")

                Box(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
