package com.inu.inujeungplayer.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.inu.inujeungplayer.MediaStoreService
import com.inu.inujeungplayer.repository.HomeRepository
import com.inu.inujeungplayer.repository.db.MusicRoomDatabase
import com.inu.inujeungplayer.ui.home.HomeViewModelFactory

interface HomeViewModelFactoryProvider {
    fun provideHomeListViewModelFactory(context: Context): HomeViewModelFactory
}

val Injector: HomeViewModelFactoryProvider
    get() = currentInjector

private object DefaultViewModelProvider: HomeViewModelFactoryProvider {
    private fun getPlantRepository(context: Context): HomeRepository {
        return HomeRepository.getInstance(
            musicDao(context)
        )
    }

    private fun musicService() = MediaStoreService()

    private fun musicDao(context: Context) =
        MusicRoomDatabase.getInstance(context.applicationContext).musicDao()

        override fun provideHomeListViewModelFactory(context: Context): HomeViewModelFactory {
        val repository = getPlantRepository(context)
        return HomeViewModelFactory(repository)
    }
}

private object Lock2

@Volatile private var currentInjector: HomeViewModelFactoryProvider =
    DefaultViewModelProvider


@VisibleForTesting
private fun setInjectorForTesting(injector: HomeViewModelFactoryProvider?) {
    synchronized(Lock2) {
        currentInjector = injector ?: DefaultViewModelProvider
    }
}

@VisibleForTesting
private fun resetInjector() =
    setInjectorForTesting(null)