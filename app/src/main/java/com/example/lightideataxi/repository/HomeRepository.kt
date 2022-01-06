package com.example.lightideataxi.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HomeRepository(private val context: Context, private val fireStore: FirebaseFirestore) {

    suspend fun getCustomerList() = fireStore.collection("Customer").get().await().documents

    suspend fun getFilterList(value: String) {

        val result = fireStore.collection("Customer").orderBy("name").whereGreaterThanOrEqualTo(
            "name",
            value.toUpperCase()
        ).whereLessThanOrEqualTo("name", "${value.toUpperCase()}\uf8ff").get().await().documents
        result
    }

}