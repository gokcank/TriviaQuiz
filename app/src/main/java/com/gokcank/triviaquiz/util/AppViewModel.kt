package com.gokcank.triviaquiz.util

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Nav girdisine bağlı ViewModelStore, CreationExtras'a APPLICATION_KEY koymadığı için
 * AndroidViewModel'ler varsayılan fabrikayla oluşturulamıyor. Application'ı
 * LocalContext'ten alıp initializer ile kurar.
 */
@Composable
inline fun <reified VM : ViewModel> appViewModel(crossinline create: (Application) -> VM): VM {
    val app = LocalContext.current.applicationContext as Application
    return viewModel { create(app) }
}
