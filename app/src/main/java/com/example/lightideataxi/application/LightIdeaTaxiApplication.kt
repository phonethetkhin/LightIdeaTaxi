package com.example.lightideataxi.application

import android.app.Application
import com.example.lightideataxi.repository.HomeRepository
import com.example.lightideataxi.viewmodel.HomeViewModel
import com.example.lightideataxi.viewmodel.ViewModelFactory
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.kodein.di.*
import org.kodein.di.android.x.androidXModule

class LightIdeaTaxiApplication : Application(), DIAware {
    override val di by DI.lazy {
        import(androidXModule(this@LightIdeaTaxiApplication))

        bindSingleton { Firebase.firestore }
        bindSingleton { ViewModelFactory(di.direct) }

        bindSingleton { HomeRepository(instance(), instance()) }
        bind<HomeViewModel>(HomeViewModel::class.java.simpleName) with provider {
            HomeViewModel(
                instance()
            )
        }

    }
}