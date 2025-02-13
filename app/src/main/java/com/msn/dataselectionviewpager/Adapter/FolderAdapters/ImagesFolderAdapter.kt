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
import com.msn.smartswitch.AppPrefs.SelectedItemsUriManager
import com.msn.dataselectionviewpager.dataClass.FolderWithImageCount
import java.io.File
import androidx.core.content.FileProvider

class ImagesFolderAdapter(private val context: Context, private val onItemClick: (FolderWithImageCount) -> Unit) :
    RecyclerView.Adapter<ImagesFolderAdapter.FolderViewHolder>() {

    private var folders = listOf<FolderWithImageCount>()

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderNameTextView: TextView = itemView.findViewById(R.id.folderName)
        val folderThumbnailImageView: ImageView = itemView.findViewById(R.id.folderIcon)
        val checkBox: CheckBox = itemView.findViewById(R.id.folderCheckBox)

        fun bind(folderWithImageCount: FolderWithImageCount) {
            folderNameTextView.text = folderWithImageCount.folder.name + "(${folderWithImageCount.imageCount})"

            if (folderWithImageCount.thumbnail != null) {
                folderThumbnailImageView.setImageBitmap(folderWithImageCount.thumbnail)
            } else {
                folderThumbnailImageView.setImageResource(R.drawable.ic_launcher_background) // Default image
            }

            // Convert File to Uri
            val folderUri = convertFileToUri(folderWithImageCount.folder)
            val path = folderWithImageCount.folder.path
            Log.d("paths", "bind: folderUri: $folderUri")
            Log.d("paths", "bind: path: $path")

            // Set checkbox state based on whether the folder URI is in the selected list
            checkBox.isChecked = SelectedItemsUriManager.getSelectedUris().contains(folderUri)

            // Handle checkbox click to add/remove URI from selected list
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    SelectedItemsUriManager.addUri(folderUri)
                    SelectedItemsUriManager.addPath(path)
                    Log.d("Selected URIs", "Added URI: ${folders}")
                    Log.d("Selected URIs", "Added Path: ${path}")
                } else {
                    SelectedItemsUriManager.removeUri(folderUri)
                    Log.d("Selected URIs", "Removed URI: $folderUri")
                }
            }

            itemView.setOnClickListener {
                onItemClick(folderWithImageCount)
            }
        }

        // Helper function to convert File to Uri using FileProvider
        private fun convertFileToUri(file: File): Uri {
            return FileProvider.getUriForFile(
                context,
                "com.msn.dataselectionviewpager.fileprovider", // Replace with your actual file provider authorities
                file
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FolderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folderWithImageCount = folders[position]
        holder.bind(folderWithImageCount)
    }

    override fun getItemCount(): Int {
        return folders.size
    }

    fun submitList(folders: List<FolderWithImageCount>) {
        this.folders = folders
        notifyDataSetChanged()
    }
}
