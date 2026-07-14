package com.msn.dataselectionviewpager.Adapter.dataShowingAdapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.Utils.AppConstant.selectedPath
import com.msn.dataselectionviewpager.dataClass.DocumentModel
import java.io.File

class DocumentsAdapter(private val context: Context) : RecyclerView.Adapter<DocumentsAdapter.ViewHolder>() {

    private var documents: List<DocumentModel> = emptyList()

    fun submitList(newDocuments: List<DocumentModel>) {
        this.documents = newDocuments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val document = documents[position]
        val fileName = getFileName(document.name)
        val fileType = getFileType(document.name)

        holder.fileName.text = fileName
        holder.fileIcon.setImageResource(fileType) // Set the icon based on file type

        val contentUri = convertFilePathToContentUri(document.filePath)
        holder.bind(document.filePath) // Bind the content URI to the view holder
    }

    override fun getItemCount(): Int = documents.size

    private fun getFileName(filePath: String): String {
        return filePath.substringAfterLast("/")
    }

    private fun getFileType(fileName: String): Int {
        return when {
            fileName.endsWith(".pdf", ignoreCase = true) -> R.drawable.ic_pdf // PDF icon
            fileName.endsWith(".doc", ignoreCase = true) || fileName.endsWith(".docx", ignoreCase = true) -> R.drawable.ic_word // Word icon
            fileName.endsWith(".txt", ignoreCase = true) -> R.drawable.ic_text // Text file icon
            fileName.endsWith(".xls", ignoreCase = true) || fileName.endsWith(".xlsx", ignoreCase = true) -> R.drawable.ic_excel // Excel icon
            else -> R.drawable.ic_document // Default icon for other file types
        }
    }

    private fun convertFilePathToContentUri(filePath: String): Uri {
        val file = File(filePath)
        return FileProvider.getUriForFile(
            context,
            "com.msn.dataselectionviewpager.fileprovider", // Your provider authority
            file
        )
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.documentName)
        val fileIcon: ImageView = itemView.findViewById(R.id.documentIcon) // ImageView for the file icon
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        fun bind(uri: String) {
            // Set checkbox state based on whether the URI is in the selected list
//            checkBox.isChecked = SelectedItemsUriManager.getSelectedUris().contains(uri)

            // Handle checkbox click to add/remove URI from selected list
            checkBox.isChecked = selectedPath.contains(uri)
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
//                    SelectedItemsUriManager.addUri(uri)
                     selectedPath.add(uri)
                    Log.d("Selected URIs", "add URI: $uri")
                    Log.d("Selected URIs", "add selectedPath: $selectedPath")
                } else {
//                    SelectedItemsUriManager.removeUri(uri)
                    Log.d("Selected URIs", "Removed URI: $uri")
                }
            }
        }
    }
}