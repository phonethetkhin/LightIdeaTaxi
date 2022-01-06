package com.example.lightideataxi.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomerModel(
    val name: String = "",
    val phone: String = "",
    val lat: String = "",
    val log: String = "",
    val photourl: String = ""
) : Parcelable