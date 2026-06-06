package com.sindicato.aplicacionaccesible.ui.signlanguage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sindicato.aplicacionaccesible.data.AppDatabase
import com.sindicato.aplicacionaccesible.data.SignLanguageEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SignLanguageViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).signLanguageDao()
    
    val signItems: StateFlow<List<SignLanguageEntity>> = dao.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
