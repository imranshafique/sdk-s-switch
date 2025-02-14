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
 import com.msn.dataselectionviewpager.dataClass.FolderWithAudioCount
import java.io.File
import androidx.core.content.FileProvider

class AudioFolderAdapter(private val context: Context, private val onItemClick: (FolderWithAudioCount) -> Unit) :
    RecyclerView.Adapter<AudioFolderAdapter.FolderViewHolder>() {

    private var folders = listOf<FolderWithAudioCount>()

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderNameTextView: TextView = itemView.findViewById(R.id.folderName)
        val folderThumbnailImageView: ImageView = itemView.findViewById(R.id.folderIcon)
        val checkBox: CheckBox = itemView.findViewById(R.id.folderCheckBox)

        fun bind(folderWithAudioCount: FolderWithAudioCount) {
            folderNameTextView.text = folderWithAudioCount.folder.name + "(${folderWithAudioCount.audioCount})"
            // Set a placeholder or icon for audio folders (no thumbnail logic for audio)
            folderThumbnailImageView.setImageResource(R.drawable.ic_launcher_background)

            // Convert File to Uri
            val folderUri = convertFileToUri(folderWithAudioCount.folder)
            val paths = folderWithAudioCount.folder.path
            Log.d("AudioPaths", "bind: path:$paths")
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
                onItemClick(folderWithAudioCount)
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
        val folderWithAudioCount = folders[position]
        holder.bind(folderWithAudioCount)
    }

    override fun getItemCount(): Int {
        return folders.size
    }

    fun submitList(folders: List<FolderWithAudioCount>) {
        this.folders = folders
        notifyDataSetChanged()
    }
}
