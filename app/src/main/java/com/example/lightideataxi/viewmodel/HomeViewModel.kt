package com.example.lightideataxi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lightideataxi.model.CustomerModel
import com.example.lightideataxi.repository.HomeRepository
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {
    private lateinit var customerLiveData: MutableLiveData<ArrayList<CustomerModel>>
    private var customers = ArrayList<CustomerModel>()

    // movie-------------------------------------------------------------------------------------//
    fun getCustomerListLiveData(): LiveData<ArrayList<CustomerModel>> {
        if (!::customerLiveData.isInitialized) {
            customerLiveData = MutableLiveData<ArrayList<CustomerModel>>()
            getCustomerList()
        }
        return customerLiveData
    }

    fun getCustomerList() {
        viewModelScope.launch {
            val result = repository.getCustomerList()
            result.forEach { customers.add(it.toObject()!!) }
            customerLiveData.postValue(customers)
        }
    }

    fun getFilterList(value: String) = viewModelScope.launch {
        repository.getFilterList(value)
    }
}

