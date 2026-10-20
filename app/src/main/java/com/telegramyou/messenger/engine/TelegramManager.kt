@file:Suppress("SpellCheckingInspection")

package com.telegramyou.messenger.engine

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi

class TelegramManager(private val context: Context) : Client.ResultHandler {

    companion object {
        init {
            System.loadLibrary("tdjni")
        }
    }

    private var client: Client? = null

    private val _authState = MutableStateFlow<TdApi.AuthorizationState?>(null)
    val authState: StateFlow<TdApi.AuthorizationState?> = _authState.asStateFlow()

    private val _chatIds = MutableStateFlow<List<Long>>(emptyList())
    val chatIds: StateFlow<List<Long>> = _chatIds.asStateFlow()

    private val _chats = MutableStateFlow<Map<Long, TdApi.Chat>>(emptyMap())
    val chats: StateFlow<Map<Long, TdApi.Chat>> = _chats.asStateFlow()

    private val _downloadedFiles = MutableStateFlow<Map<Int, String>>(emptyMap())
    val downloadedFiles: StateFlow<Map<Int, String>> = _downloadedFiles.asStateFlow()

    private val _messages = MutableStateFlow<Map<Long, List<TdApi.Message>>>(emptyMap())
    val messages: StateFlow<Map<Long, List<TdApi.Message>>> = _messages.asStateFlow()
    private val _selectedChatId = MutableStateFlow<Long?>(null)
    val selectedChatId: StateFlow<Long?> = _selectedChatId.asStateFlow()
    private val _sendingChatIds = MutableStateFlow<Set<Long>>(emptySet())
    val sendingChatIds: StateFlow<Set<Long>> = _sendingChatIds.asStateFlow()

    // TODO: REPLACE WITH YOUR ACTUAL API ID AND HASH BEFORE RUNNING LOCALLY
    private val apiId   = 34130445
    private val apiHash = "bba4d6bd278c3597ad4e18760f242077"

    fun initClient() {
        Client.execute(TdApi.SetLogVerbosityLevel(0))
        client = Client.create(this, null, null)
    }

    override fun onResult(result: TdApi.Object) {
        Log.d(
            "TDLIB",
            "Result: ${result.javaClass.simpleName} (${result.constructor})"
        )
        when (result.constructor) {
            TdApi.UpdateAuthorizationState.CONSTRUCTOR -> {
                val update = result as TdApi.UpdateAuthorizationState
                onAuthorizationStateUpdated(update.authorizationState)
            }

            TdApi.UpdateNewChat.CONSTRUCTOR -> {
                val chat = (result as TdApi.UpdateNewChat).chat

                Log.d(
                    "TDLIB",
                    "New chat '${chat.title}', lastMessage=${chat.lastMessage}"
                )

                _chats.value = _chats.value + (chat.id to chat)
                if (chat.id !in _chatIds.value) _chatIds.value = _chatIds.value + chat.id
            }

            TdApi.UpdateChatLastMessage.CONSTRUCTOR -> {
                val update = result as TdApi.UpdateChatLastMessage

                Log.d(
                    "TDLIB",
                    "UpdateChatLastMessage: chat=${update.chatId}"
                )

                val chat = _chats.value[update.chatId]

                if (chat != null) {
                    chat.lastMessage = update.lastMessage

                    _chats.value = _chats.value + (chat.id to chat)
                }
            }

            TdApi.UpdateNewMessage.CONSTRUCTOR -> {
                val message = (result as TdApi.UpdateNewMessage).message
                addOrReplaceMessage(message)
                val chat = _chats.value[message.chatId]
                if (chat != null) {
                    chat.lastMessage = message
                    _chats.value = _chats.value + (chat.id to chat)
                }
            }

            TdApi.UpdateFile.CONSTRUCTOR -> {
                val file = (result as TdApi.UpdateFile).file
                if (file.local.isDownloadingCompleted) {
                    _downloadedFiles.value = _downloadedFiles.value + (file.id to file.local.path)
                }
            }

            else -> {
                // Exhaustive branch to suppress "missing case" warnings
            }
        }
    }

    private fun onAuthorizationStateUpdated(state: TdApi.AuthorizationState) {
        _authState.value = state

        when (state.constructor) {
            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                val request = TdApi.SetTdlibParameters(
                    true,
                    context.filesDir.absolutePath + "/tdlib",
                    null,
                    null,
                    true,
                    true,
                    true,
                    true,
                    apiId,
                    apiHash,
                    "en",
                    "Android",
                    "Unknown",
                    "1.0"
                )
                client?.send(request, this)
            }

            TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                Log.d("TDLib", "Engine ready! Fetching chats...")
                loadChats()
            }

            TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                Log.d("TDLib", "Engine completely closed after logout. Rebooting...")
                client = null
                initClient()
            }

            else -> {
                // Exhaustive branch to suppress "missing case" warnings
            }
        }
    }

    fun sendPhoneNumber(phoneNumber: String) {
        client?.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), this)
    }

    fun sendVerificationCode(code: String) {
        client?.send(TdApi.CheckAuthenticationCode(code), this)
    }

    fun sendPassword(password: String) {
        client?.send(TdApi.CheckAuthenticationPassword(password), this)
    }

    fun logout() {
        client?.send(TdApi.LogOut()) {}
    }

    fun downloadFile(fileId: Int) {
        client?.send(TdApi.DownloadFile(fileId, 1, 0, 0, true)) {}
    }

    fun openChat(chatId: Long) {
        _selectedChatId.value = chatId
        client?.send(TdApi.OpenChat(chatId)) { result ->
            if (result.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("TDLIB", "OpenChat failed: ${(result as TdApi.Error).message}")
            }
        }
        loadChatHistory(chatId)
    }

    fun closeChat(chatId: Long) {
        client?.send(TdApi.CloseChat(chatId)) {}
        if (_selectedChatId.value == chatId) _selectedChatId.value = null
    }

    fun loadChatHistory(chatId: Long, fromMessageId: Long = 0L, limit: Int = 50) {
        client?.send(TdApi.GetChatHistory(chatId, fromMessageId, 0, limit, false)) { result ->
            if (result.constructor == TdApi.Messages.CONSTRUCTOR) {
                val loaded = (result as TdApi.Messages).messages.toList().reversed()
                _messages.value = _messages.value + (chatId to mergeMessages(_messages.value[chatId].orEmpty(), loaded))
            } else if (result.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("TDLIB", "GetChatHistory failed: ${(result as TdApi.Error).message}")
            }
        }
    }

    fun sendTextMessage(chatId: Long, text: String) {
        val cleanText = text.trim()
        if (cleanText.isEmpty() || chatId in _sendingChatIds.value) return
        _sendingChatIds.value = _sendingChatIds.value + chatId
        val content = TdApi.InputMessageText(TdApi.FormattedText(cleanText, null), null, true)
        client?.send(TdApi.SendMessage(chatId, null, null, null, null, content)) { result ->
            _sendingChatIds.value = _sendingChatIds.value - chatId
            if (result.constructor == TdApi.Message.CONSTRUCTOR) {
                addOrReplaceMessage(result as TdApi.Message)
            } else if (result.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("TDLIB", "SendMessage failed: ${(result as TdApi.Error).message}")
            }
        } ?: run { _sendingChatIds.value = _sendingChatIds.value - chatId }
    }

    private fun addOrReplaceMessage(message: TdApi.Message) {
        val current = _messages.value[message.chatId].orEmpty()
        _messages.value = _messages.value + (message.chatId to mergeMessages(current, listOf(message)))
    }

    private fun mergeMessages(old: List<TdApi.Message>, new: List<TdApi.Message>): List<TdApi.Message> =
        (old + new).distinctBy { it.id }.sortedBy { it.date }

    private fun loadChats() {
        client?.send(TdApi.LoadChats(TdApi.ChatListMain(), 100)) { result ->
            when (result.constructor) {
                TdApi.Ok.CONSTRUCTOR    -> getChatIds()
                TdApi.Error.CONSTRUCTOR -> Log.e("TDLib", "Failed to load chats")
                else -> {} // Suppress missing case warning
            }
        }
    }

    private fun getChatIds() {
        client?.send(TdApi.GetChats(TdApi.ChatListMain(), 100)) { result ->
            if (result.constructor == TdApi.Chats.CONSTRUCTOR) {
                val ids = (result as TdApi.Chats).chatIds.toList()
                _chatIds.value = ids
                ids.forEach { id ->
                    if (_chats.value[id] == null) {
                        client?.send(TdApi.GetChat(id)) { chatResult ->
                            if (chatResult.constructor == TdApi.Chat.CONSTRUCTOR) {
                                val chat = chatResult as TdApi.Chat
                                _chats.value = _chats.value + (chat.id to chat)
                            }
                        }
                    }
                }
            }
        }
    }
}
