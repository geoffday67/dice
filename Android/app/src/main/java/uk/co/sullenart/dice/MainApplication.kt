package uk.co.sullenart.dice

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber.*
import timber.log.Timber.Forest.plant
import uk.co.sullenart.dice.settings.SettingsViewModel


class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        }
        startKoin {
            androidContext(this@MainApplication)
            modules(
                module {
                    viewModelOf(::SettingsViewModel)
                }
            )
        }
    }
}