package com.dewnz.contactapps.viewModel

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.PositionalDataSource
import com.dewnz.contactapps.data.Contact

class ContactsViewModel(private val contentResolver: ContentResolver) : ViewModel() {

    lateinit var contactsList: LiveData<PagedList<Contact>>

    fun loadContacts() {
        val config = PagedList.Config.Builder()
                .setPageSize(20)
                .setEnablePlaceholders(false)
                .build()
        contactsList = LivePagedListBuilder<Int, Contact>(
            ContactsDataSourceFactory(
                contentResolver
            ), config).build()
    }

    fun addContact(name: String, contact: String) {
        val addContactsUri: Uri = ContactsContract.Data.CONTENT_URI;
        val contentValue = ContentValues()
        contentValue.put(ContactsContract.Data.RAW_CONTACT_ID, getRawContactId())
        contentValue.put(
            ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
        );
        // Put contact display name value.
        contentValue.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
        contentValue.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact)
        contentResolver.insert(addContactsUri, contentValue)
    }

    private fun getRawContactId(): Long {
        // Insert an empty contact.
        val contentValues = ContentValues()
        val rawContactUri =
            contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues)
        // Get the newly created contact raw id.
        return ContentUris.parseId(rawContactUri!!)
    }
}

class ContactsDataSourceFactory(private val contentResolver: ContentResolver) :
        DataSource.Factory<Int, Contact>() {

    override fun create(): DataSource<Int, Contact> {
        return ContactsDataSource(contentResolver)
    }
}

class ContactsDataSource(private val contentResolver: ContentResolver) :
        PositionalDataSource<Contact>() {

    companion object {
        private val PROJECTION = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Contact>) {
        callback.onResult(getContacts(params.requestedLoadSize, params.requestedStartPosition), 0)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Contact>) {
        callback.onResult(getContacts(params.loadSize, params.startPosition))
    }

    private fun getContacts(limit: Int, offset: Int): MutableList<Contact> {
        val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
            PROJECTION,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY +
                        " ASC LIMIT " + limit + " OFFSET " + offset)

        cursor?.moveToFirst()
        val contacts: MutableList<Contact> = mutableListOf()
        while (!cursor?.isAfterLast!!) {
            val id = cursor.getLong(cursor.getColumnIndex(PROJECTION[0]))
            val lookupKey = cursor.getString(cursor.getColumnIndex(PROJECTION[0]))
            val name = cursor.getString(cursor.getColumnIndex(PROJECTION[2]))
            contacts.add(
                Contact(
                    id,
                    lookupKey,
                    name ?: "no name"
                )
            )
            cursor.moveToNext()
        }
        cursor.close()
        return contacts
    }
}