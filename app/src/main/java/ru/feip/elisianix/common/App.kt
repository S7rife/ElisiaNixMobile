package ru.feip.elisianix.common

import android.app.Application
import android.content.SharedPreferences
import androidx.room.Room
import ru.feip.elisianix.R
import ru.feip.elisianix.common.db.AppDataBase
import kotlin.properties.Delegates

class App : Application() {

    companion object {
        lateinit var INSTANCE: App
        lateinit var sharedPreferences: SharedPreferences
        var AUTH by Delegates.notNull<Boolean>()
    }

    val db by lazy {
        Room.databaseBuilder(
            this,
            AppDataBase::class.java,
            "app_database"
        ).allowMainThreadQueries().build()
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        AUTH = sharedPreferences.contains("token")
    }
}