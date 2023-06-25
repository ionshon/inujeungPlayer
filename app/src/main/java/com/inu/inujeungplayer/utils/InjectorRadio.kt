package com.inu.inujeungplayer.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.inu.inujeungplayer.MediaStoreService
import com.inu.inujeungplayer.repository.RadioRepo
import com.inu.inujeungplayer.repository.db.MusicRoomDatabase
import com.inu.inujeungplayer.ui.radio.RadioViewModelFactory

interface RadioViewModelFactoryProvider {
    fun provideRadioListViewModelFactory(context: Context): RadioViewModelFactory
}

val InjectorRadio: RadioViewModelFactoryProvider
    get() = currentInjector

private object RadioViewModelProvider: RadioViewModelFactoryProvider {
    private fun getPlantRepository(context: Context): RadioRepo {
        return RadioRepo.getInstance(
            radioDao(context)
        )
    }

//    private fun musicService() = MediaStoreService()

    private fun radioDao(context: Context) =
        MusicRoomDatabase.getInstance(context.applicationContext).radioDao()

        override fun provideRadioListViewModelFactory(context: Context): RadioViewModelFactory {
        val repository = getPlantRepository(context)
        return RadioViewModelFactory(repository)
    }
}

private object Lock

@Volatile private var currentInjector: RadioViewModelFactoryProvider =
    RadioViewModelProvider


@VisibleForTesting
private fun setInjectorForTesting(injector: RadioViewModelFactoryProvider?) {
    synchronized(Lock) {
        currentInjector = injector ?: RadioViewModelProvider
    }
}

@VisibleForTesting
private fun resetInjector() =
    setInjectorForTesting(null)