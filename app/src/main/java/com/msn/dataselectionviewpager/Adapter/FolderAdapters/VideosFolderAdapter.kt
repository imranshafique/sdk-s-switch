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
import com.bumptech.glide.Glide
import com.msn.dataselectionviewpager.Utils.AppConstant.selectedPath

class VideosFolderAdapter(
    private val context: Context,
    private val onItemClick: (FolderWithVideoCount) -> Unit
) : RecyclerView.Adapter<VideosFolderAdapter.FolderViewHolder>() {

    private var folders = listOf<FolderWithVideoCount>()

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val folderNameTextView: TextView = itemView.findViewById(R.id.folderName)
        val folderThumbnailImageView: ImageView = itemView.findViewById(R.id.folderIcon)
        val checkBox: CheckBox = itemView.findViewById(R.id.folderCheckBox)

        fun bind(folderWithVideoCount: FolderWithVideoCount) {
            folderNameTextView.text = "${folderWithVideoCount.folder.name} (${folderWithVideoCount.videoCount})"

            if (folderWithVideoCount.thumbnail != null) {
                Glide.with(context)
                    .load(folderWithVideoCount.thumbnail)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(folderThumbnailImageView)
            } else {
                folderThumbnailImageView.setImageResource(R.drawable.ic_launcher_background)
            }

            val folderPath = folderWithVideoCount.folder.path
            Log.d("paths", "bind: folderPath: $folderPath")

            // Get all video paths in the folder
            val allPaths = getAllFilePaths(folderWithVideoCount.folder)
            checkBox.isChecked = allPaths.all { selectedPath.contains(it) }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedPath.addAll(allPaths)
                    Log.d("Selected URIs", "Added Paths: $allPaths")
                } else {
                    selectedPath.removeAll(allPaths)
                    Log.d("Selected URIs", "Removed Paths: $allPaths")
                }
                Log.d("Selected Path List", "Current selectedPath: $selectedPath")
            }

            itemView.setOnClickListener {
                onItemClick(folderWithVideoCount)
            }
        }

        // Helper function to get all video file paths in the folder
        private fun getAllFilePaths(folder: File): List<String> {
            val files = folder.listFiles()
            return files?.filter { it.isFile }?.map { it.absolutePath } ?: emptyList()
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