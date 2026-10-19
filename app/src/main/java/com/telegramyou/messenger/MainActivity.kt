
package com.telegramyou.messenger

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.drinkless.tdlib.TdApi
import com.telegramyou.messenger.engine.TelegramManager
import com.telegramyou.messenger.ui.screens.AuthLoadingScreen
import com.telegramyou.messenger.ui.screens.PasswordScreen
import com.telegramyou.messenger.ui.screens.PhoneNumberScreen
import com.telegramyou.messenger.ui.screens.VerificationCodeScreen
import com.telegramyou.messenger.ui.screens.ChatListScreen

class MainActivity : ComponentActivity() {

    private lateinit var telegramManager: TelegramManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        telegramManager = TelegramManager(applicationContext)
        telegramManager.initClient()

        setContent {
            val isDark = isSystemInDarkTheme()

            val colorScheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (isDark) {
                        dynamicDarkColorScheme(this@MainActivity)
                    } else {
                        dynamicLightColorScheme(this@MainActivity)
                    }
                }

                isDark -> darkColorScheme()
                else -> lightColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authState by telegramManager.authState.collectAsState()

                    Crossfade(
                        targetState = authState,
                        label = "Auth Transition"
                    ) { state ->
                        AuthRouter(state, telegramManager)
                    }
                }
            }
        }
    }
}

@Composable
fun AuthRouter(
    authState: TdApi.AuthorizationState?,
    manager: TelegramManager
) {
    when (authState?.constructor) {
        TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
            PhoneNumberScreen(
                onSubmit = { countryCode, phoneNumber ->
                    manager.sendPhoneNumber(countryCode + phoneNumber)
                }
            )
        }

        TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
            VerificationCodeScreen(
                onSubmit = { code ->
                    manager.sendVerificationCode(code)
                }
            )
        }

        TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
            PasswordScreen(
                onSubmit = { password ->
                    manager.sendPassword(password)
                }
            )
        }

        TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
            ChatListScreen(manager)
        }

        else -> {
            AuthLoadingScreen()
        }
    }
}
