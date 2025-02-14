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
 import com.msn.dataselectionviewpager.dataClass.FolderWithVideoCount
import java.io.File
import androidx.core.content.FileProvider

class VideosFolderAdapter(private val context: Context, private val onItemClick: (FolderWithVideoCount) -> Unit) :
    RecyclerView.Adapter<VideosFolderAdapter.FolderViewHolder>() {

    private var folders = listOf<FolderWithVideoCount>()

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderNameTextView: TextView = itemView.findViewById(R.id.folderName)
        val folderThumbnailImageView: ImageView = itemView.findViewById(R.id.folderIcon)
        val checkBox: CheckBox = itemView.findViewById(R.id.folderCheckBox)

        fun bind(folderWithVideoCount: FolderWithVideoCount) {
            folderNameTextView.text = folderWithVideoCount.folder.name + " (${folderWithVideoCount.videoCount})"

            // Set the thumbnail or a default image
            if (folderWithVideoCount.thumbnail != null) {
                folderThumbnailImageView.setImageBitmap(folderWithVideoCount.thumbnail)
            } else {
                folderThumbnailImageView.setImageResource(R.drawable.ic_launcher_background) // Default image
            }

            // Convert File to Uri
            val folderUri = convertFileToUri(folderWithVideoCount.folder)

            // Set checkbox state based on whether the folder URI is in the selected list

            // Handle checkbox click to add/remove URI from selected list
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                     Log.d("Selected URIs", "Added URI: $folderUri")
                } else {
                     Log.d("Selected URIs", "Removed URI: $folderUri")
                }
            }

            itemView.setOnClickListener {
                onItemClick(folderWithVideoCount)
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
        val folderWithVideoCount = folders[position]
        holder.bind(folderWithVideoCount)
    }

    override fun getItemCount(): Int {
        return folders.size
    }

    fun submitList(folders: List<FolderWithVideoCount>) {
        this.folders = folders
        notifyDataSetChanged()
    }
}
