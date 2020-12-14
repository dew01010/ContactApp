package com.dewnz.contactapps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dewnz.contactapps.data.Contact

class ContactAdapter(diffUtil: DiffUtil.ItemCallback<Contact>):PagedListAdapter<Contact, ContactAdapter.ContactViewHolder>(diffUtil) {

     class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent,
                false))
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.textView.text = getItem(position)?.name
    }
}