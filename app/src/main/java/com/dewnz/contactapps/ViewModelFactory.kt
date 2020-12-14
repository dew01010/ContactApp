package com.dewnz.contactapps

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dewnz.contactapps.viewModel.ContactsViewModel

open class ViewModelFactory(private val contentResolver: ContentResolver):ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ContactsViewModel(contentResolver) as T
    }

}