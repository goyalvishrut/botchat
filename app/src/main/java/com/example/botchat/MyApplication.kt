package com.example.botchat

import android.app.Application
import com.example.botchat.di.appModule
import com.example.botchat.di.networkModule
import com.example.botchat.di.repositoryModule
import com.example.botchat.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApplication)
            modules(
                appModule,
                networkModule,
                repositoryModule,
                viewModelModule,
            )
        }
    }
}
