package com.msn.dataselectionviewpager.Fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.msn.dataselectionviewpager.Adapter.dataShowingAdapters.ContactAdapter
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.SelectedURis.ConstantVariables
import com.msn.smartswitch.AppPrefs.SelectedItemsUriManager
import com.msn.dataselectionviewpager.dataClass.Contact
import com.msn.dataselectionviewpager.databinding.FragmentContactBinding

class ContactFragment : Fragment() {

    private lateinit var binding: FragmentContactBinding
    private lateinit var contactAdapter: ContactAdapter
    private val contacts = mutableListOf<Contact>()
    var constantVariables: ConstantVariables?=null
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                binding.incContactPermission.storageroot.visibility = View.GONE
                fetchContacts()
            } else {
                constantVariables!!.permissionCounter++
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         binding = FragmentContactBinding.inflate(inflater, container, false)
        constantVariables=ConstantVariables(requireContext())

         binding.recyclerViewContact.layoutManager = LinearLayoutManager(context)

        contactAdapter = ContactAdapter(requireContext(),contacts)
        binding.recyclerViewContact.adapter = contactAdapter

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            binding.incContactPermission.storageroot.visibility = View.GONE
            fetchContacts()
        } else {
            binding.incContactPermission.storageroot.visibility = View.VISIBLE
            binding.incContactPermission.grantpermission.setOnClickListener {
                if( constantVariables!!.permissionCounter>=2){
                    openAppSettings()
                }else{
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }

            }

        }

        // Set up button click listener with binding reference
        binding.btnExportVcf.setOnClickListener {
            exportSelectedContacts()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            binding.incContactPermission.storageroot.visibility = View.GONE

        } else {
            binding.incContactPermission.storageroot.visibility = View.VISIBLE
            binding.incContactPermission.tvHeader.text=getString(R.string.contact_permission)
            binding.incContactPermission.topimage.setImageResource(R.drawable.contactpermissionicon)
            binding.incContactPermission.grantpermission.setOnClickListener {
                if( constantVariables!!.permissionCounter>=2){
                    openAppSettings()
                }else{
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }

            }

        }
    }
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }
    private fun fetchContacts() {
        val contactListUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        )

        val cursor = requireContext().contentResolver.query(
            contactListUri,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.let {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                val contactId = it.getString(contactIdIndex)

                val contactUri = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_URI, contactId
                )

                contacts.add(Contact(name, number, contactUri))
            }
            it.close()
        }

        contactAdapter.notifyDataSetChanged()
    }

    private fun exportSelectedContacts() {
        val selectedContacts = contactAdapter.getSelectedContacts()
        if (selectedContacts.isEmpty()) {
            Toast.makeText(requireContext(), "No contacts selected", Toast.LENGTH_SHORT).show()
            return
        }

        selectedContacts.forEach { contact ->
            SelectedItemsUriManager.addUri(contact.contactUri)
        }

        Toast.makeText(requireContext(), "Contacts saved!", Toast.LENGTH_SHORT).show()
    }
}
