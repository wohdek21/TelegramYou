package com.telegramyou.messenger

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import org.drinkless.tdlib.TdApi
import com.telegramyou.messenger.engine.TelegramManager
import com.telegramyou.messenger.ui.screens.ChatListScreen

class MainActivity : ComponentActivity() {

    private lateinit var telegramManager: TelegramManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        telegramManager = TelegramManager(applicationContext)
        telegramManager.initClient()

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authState by telegramManager.authState.collectAsState()

                    Crossfade(targetState = authState, label = "Auth Transition") { state ->
                        AuthRouter(state, telegramManager)
                    }
                }
            }
        }
    }
}

@Composable
fun AuthRouter(authState: TdApi.AuthorizationState?, manager: TelegramManager) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (authState?.constructor) {
            TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                var phoneNumber by remember { mutableStateOf("") }
                    var countrycode by remember { mutableStateOf("")}

                Text("Войти в аккаунт Telegram", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = countrycode,
                    onValueChange = { countrycode = it },
                    label = { Text("Код страны") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Номер телефона") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { manager.sendPhoneNumber(countrycode + phoneNumber)
                            Log.d("TelegramYou", "sendCodePressed")},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = phoneNumber.isNotBlank()
                ) {
                    Text("Отправить код")
                }
            }

            TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                var code by remember { mutableStateOf("") }
                Log.d("TelegramYou", "CodePage")
                Text("Введите код", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "Мы отправили вам код верификации в Telegram",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Код верификации") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { manager.sendVerificationCode(code) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = code.isNotBlank()
                ) {
                    Text("Войти")
                }
            }

            TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                var password by remember { mutableStateOf("") }

                Text("Двухфакторная аутентификация", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "Ваш аккаунт защищен дополнительным паролем.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Введите пароль") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { manager.sendPassword(password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = password.isNotBlank()
                ) {
                    Text("Отправить")
                }
            }

            TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                // Pointing directly to the Chat List
                ChatListScreen(manager)
            }

            else -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Загружаем движок датабазы...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}