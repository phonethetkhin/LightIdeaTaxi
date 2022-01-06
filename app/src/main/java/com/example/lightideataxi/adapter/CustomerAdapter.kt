package com.example.lightideataxi.adapter

import adapterViewBinding
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lightideataxi.R
import com.example.lightideataxi.databinding.ListItemCustomerBinding
import com.example.lightideataxi.model.CustomerModel
import com.example.lightideataxi.ui.activity.MapActivity

class CustomerAdapter(private val context: Context) :
    ListAdapter<CustomerModel, CustomerAdapter.CustomerViewHolder>(diffCallback) {
    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<CustomerModel>() {
            override fun areItemsTheSame(oldItem: CustomerModel, newItem: CustomerModel): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: CustomerModel,
                newItem: CustomerModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    inner class CustomerViewHolder(val binding: ListItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val v = parent.adapterViewBinding(ListItemCustomerBinding::inflate)
        return CustomerViewHolder(v)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = getItem(position)
        holder.binding.cslUser.setOnClickListener {
            val i = Intent(context, MapActivity::class.java)
            val b = Bundle()
            b.putParcelable("customerModel", customer)
            i.putExtras(b)
            context.startActivity(i)
        }
        setData(customer, holder)

    }

    private fun setData(customer: CustomerModel, holder: CustomerViewHolder) {
        with(holder)
        {
            Log.d("livedata", "adapter: $customer")
            Glide.with(context).load(customer.photourl)
                .placeholder(R.drawable.ic_account_circle_black_24dp).into(binding.imgProfile)
            binding.txtName.text = customer.name
            binding.txtPhone.text = customer.phone
        }
    }
}