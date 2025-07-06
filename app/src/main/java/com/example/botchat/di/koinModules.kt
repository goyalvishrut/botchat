package com.example.botchat.di

import androidx.room.Room
import com.example.botchat.data.local.AppDatabase
import com.example.botchat.data.local.ChatLocalDataSource
import com.example.botchat.data.local.ChatLocalDataSourceImpl
import com.example.botchat.data.remote.ChatRemoteDataSource
import com.example.botchat.data.repository.ChatRepository
import com.example.botchat.data.repository.ChatRepositoryImpl
import com.example.botchat.presentation.MainViewModel
import com.example.botchat.presentation.chatlist.ChatListViewModel
import com.example.botchat.presentation.conversationlist.ConversationListViewModel
import com.example.botchat.util.AndroidNetworkMonitor
import com.example.botchat.util.NetworkMonitor
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        single {
            Room
                .databaseBuilder(androidContext(), AppDatabase::class.java, "chat-db")
                .fallbackToDestructiveMigration()
                .build()
        }
        single { get<AppDatabase>().messageDao() }
        single { get<AppDatabase>().conversationDao() }
        single<NetworkMonitor> { AndroidNetworkMonitor(androidContext()) }
        single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    }

val networkModule =
    module {
        single {
            HttpClient(CIO) {
                install(WebSockets)
                install(ContentNegotiation) {
                    json(
                        Json {
                            prettyPrint = true
                            isLenient = true
                            ignoreUnknownKeys = true
                        },
                    )
                }
            }
        }
        single { ChatRemoteDataSource(get()) }
    }

val repositoryModule =
    module {
        single<ChatLocalDataSource> { ChatLocalDataSourceImpl(get(), get()) }
        single<ChatRepository> { ChatRepositoryImpl(get(), get(), get(), get()) }
    }

val viewModelModule =
    module {
        viewModel { ChatListViewModel(get()) }
        viewModel { ConversationListViewModel(get()) }
        viewModel { MainViewModel(get()) }
    }
