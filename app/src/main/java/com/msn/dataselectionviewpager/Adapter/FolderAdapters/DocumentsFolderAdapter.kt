package com.msn.dataselectionviewpager.Adapter.FolderAdapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msn.dataselectionviewpager.R
 import com.msn.dataselectionviewpager.dataClass.DocumentModel
import java.io.File
import androidx.core.content.FileProvider

class DocumentsFolderAdapter(
    private val context: Context,
    private val onFolderClick: (String, List<DocumentModel>) -> Unit
) : RecyclerView.Adapter<DocumentsFolderAdapter.ViewHolder>() {

    private var categorizedDocuments: Map<String, List<DocumentModel>> = emptyMap()
    private val categories: List<String> get() = categorizedDocuments.keys.toList()

    fun submitList(categorizedDocuments: Map<String, List<DocumentModel>>) {
        this.categorizedDocuments = categorizedDocuments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder_documents, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        val documents = categorizedDocuments[category] ?: emptyList()

        holder.categoryName.text = category
        holder.documentCount.text = "${documents.size} files"

        val mimeType = documents.firstOrNull()?.mimeType ?: "other"
        holder.documentIcon.setImageResource(getIconForMimeType(mimeType))

        // Get the first document's filePath and convert to URI
        val folderUri = documents.firstOrNull()?.filePath?.let { convertFileToUri(it) }

        if (folderUri != null) {
            Log.d("DocumentsFolderAdapter", "Generated URI: $folderUri")
        }

        // Ensure the checkbox reflects selection state

        // Handle checkbox click to add/remove URI from selected list
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (folderUri != null) {
                if (isChecked) {
                     Log.d("DocumentsFolderAdapter", "URI Added: $folderUri")
                } else {
                     Log.d("DocumentsFolderAdapter", "URI Removed: $folderUri")
                }
            }
        }

        // Handle folder click
        holder.itemView.setOnClickListener {
            onFolderClick(category, documents)
        }
    }

    override fun getItemCount(): Int = categories.size

    private fun getIconForMimeType(mimeType: String): Int {
        return when {
            mimeType.startsWith("application/pdf") -> R.drawable.ic_pdf
            mimeType.startsWith("application/msword") ||
                    mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document") -> R.drawable.ic_word
            mimeType.startsWith("text/plain") -> R.drawable.ic_text
            mimeType.startsWith("application/vnd.ms-excel") ||
                    mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") -> R.drawable.ic_excel
            else -> R.drawable.ic_document
        }
    }

    // ✅ Convert filePath (String) to Uri using FileProvider
    private fun convertFileToUri(filePath: String?): Uri? {
        return filePath?.let {
            val file = File(it) // Convert filePath to File
            if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "com.msn.dataselectionviewpager.fileprovider", // Replace with your actual authority
                    file
                )
            } else {
                Log.e("DocumentsFolderAdapter", "File does not exist: $filePath")
                null
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.folderName)
        val documentCount: TextView = itemView.findViewById(R.id.documentCount)
        val documentIcon: ImageView = itemView.findViewById(R.id.documentIcon)
        val checkBox: CheckBox = itemView.findViewById(R.id.folderCheckbox)
    }
}

