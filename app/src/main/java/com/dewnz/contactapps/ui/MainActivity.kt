package com.dewnz.contactapps.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.dewnz.contactapps.ContactAdapter
import com.dewnz.contactapps.R
import com.dewnz.contactapps.ViewModelFactory
import com.dewnz.contactapps.data.Contact
import com.dewnz.contactapps.viewModel.ContactsViewModel
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ContactAdapter
    private val viewModel: ContactsViewModel by viewModels {
        ViewModelFactory(contentResolver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpAdapter()
        checkContactReadPermission()
    }

    private fun subscribeContactList() {
        viewModel.loadContacts()
        viewModel.contactsList.observe(this, Observer {
            adapter.submitList(it)
            contactsEmptyText.visibility = if (adapter.itemCount > 0) {
                View.GONE
            } else {
                View.VISIBLE
            }
        })
    }

    // setUp Adapter
    private fun setUpAdapter() {
        adapter = ContactAdapter(diffCallback)
        rv.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL,
            false
        )
        rv.adapter = adapter
    }

    private fun checkContactReadPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                subscribeContactList()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                showInContextUI(getString(R.string.read_deny_msg),1)
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_CONTACTS
                    ), 104
                )
            }
        }
    }

    private fun checkContactWritePermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                addContactDialog()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                showInContextUI(getString(R.string.write_deny_msg),2)
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_CONTACTS
                    ), 105
                )
            }
        }
    }

    private fun showInContextUI(any: String?,value:Int) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setMessage(any)
        builder.setTitle(R.string.info)

        //performing cancel action
        builder.setNegativeButton("close") { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            104 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    subscribeContactList()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    showInContextUI(getString(R.string.read_deny_msg),1)
                }
                return
            }

            105 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    addContactDialog()

                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    showInContextUI(getString(R.string.write_deny_msg),2)
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun addContact(view: View) {
        checkContactWritePermission()
    }

    private fun addContactDialog() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.add_contact_dialog, null, false)
        builder.setView(view)
        val contactEd = view.findViewById(R.id.contact_ed) as EditText
        val nameEd = view.findViewById(R.id.name_ed) as EditText

        //set title for alert dialog
        builder.setTitle(R.string.dialogTitle)

        //performing positive action
        builder.setPositiveButton("Add") { dialogInterface, which ->
            when {
                nameEd.text?.isEmpty()!! -> {
                    showToast("Name should not be black or empty")
                }
                contactEd.text?.length != 10 -> {
                    showToast("Contact length should be 10 digit")
                }
                else -> {
                    viewModel?.addContact(nameEd.text?.toString()!!, contactEd.text?.toString()!!)
                }
            }
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }
    }
}