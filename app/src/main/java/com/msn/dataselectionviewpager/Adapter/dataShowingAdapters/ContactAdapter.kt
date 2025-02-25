package com.msn.dataselectionviewpager.Adapter.dataShowingAdapters

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msn.dataselectionviewpager.Fragments.ContactFragment
import com.msn.dataselectionviewpager.R
 import com.msn.dataselectionviewpager.dataClass.Contact
import com.msn.smartswitch.Models.AppConstant.selectedPath
import java.io.File
import java.io.FileWriter
import java.io.IOException

class ContactAdapter(private val context: Context, private val contacts: List<Contact>) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
    var file:File? = null

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.contact_name)
        val phoneTextView: TextView = itemView.findViewById(R.id.contact_phone_number)
        val checkBox: CheckBox = itemView.findViewById(R.id.contact_checkbox)

        fun bind(contact: Contact) {
            nameTextView.text = contact.name
            phoneTextView.text = contact.phoneNumber

            // Check if the contact URI is already selected
            checkBox.isChecked = selectedPath.contains(contact.contactUri.path)


            Log.d("ContactAdapter", "bind: file: $file")
            // Handle checkbox selection
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                contacts.forEach {
                    file = convertContactsToVcf(context, it.contactUri)
                }
                if (isChecked) {
                     selectedPath.add(file!!.path)
                    Log.d("Selected URIs", "Added URI: ${contact.contactUri}")
                    Log.d("Selected URIs", "Added URI selectedPath : ${selectedPath}")

                } else {
                    if (file?.let { selectedPath.contains(it.path) } == true) {
                        selectedPath.remove(file?.path)
                        if (file?.exists()!!) {
                            val deleted = file?.delete()
                            Log.d("Selected URIs", "Removed URI: ${contact.contactUri}, File deleted: $deleted")
                        }
                    }
                    Log.d("Selected URIs", "remove URI: ${contact.contactUri}")

                }
            }
        }
    }
    private fun convertContactsToVcf(context: Context, contactUri: Uri): File? {
        val vcfFile = File(context.cacheDir, "Contact_${System.currentTimeMillis()}.vcf") // Unique file name
        Log.d("ContactAdapter", "convertContactsToVcf: vcfFile: $vcfFile")


        try {
            FileWriter(vcfFile).use { writer ->
                val cursor: Cursor? = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  // Correct URI for phone numbers
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactUri.lastPathSegment),
                    null
                )

                cursor?.use {
                    while (it.moveToNext()) {
                        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                        if (nameIndex != -1 && numberIndex != -1) {
                            val contactName = it.getString(nameIndex)
                            val phoneNumber = it.getString(numberIndex)

                            writer.write("BEGIN:VCARD\n")
                            writer.write("VERSION:3.0\n")
                            writer.write("FN:$contactName\n")
                            writer.write("TEL:$phoneNumber\n")
                            writer.write("END:VCARD\n")
                        }
                    }
                }
            }
            return vcfFile
        } catch (e: IOException) {
            Log.e("ContactAdapter", "Error writing VCF file", e)
            return null
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
    }

    override fun getItemCount(): Int = contacts.size

}
