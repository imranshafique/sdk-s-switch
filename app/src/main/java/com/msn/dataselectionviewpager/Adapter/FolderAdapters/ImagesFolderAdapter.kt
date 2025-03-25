package com.msn.dataselectionviewpager.Adapter.FolderAdapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.dataClass.FolderWithImageCount
import com.msn.smartswitch.Models.AppConstant.selectedPath
import com.msn.dataselectionviewpager.dataClass.FolderWithImageCount
import com.msn.dataselectionviewpager.Utils.AppConstant.selectedPath
import java.io.File
import androidx.core.content.FileProvider

class ImagesFolderAdapter(
    private val context: Context,
    private val onItemClick: (FolderWithImageCount) -> Unit
) : RecyclerView.Adapter<ImagesFolderAdapter.FolderViewHolder>()
{

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

            val folderPath = folderWithImageCount.folder.path
            Log.d("paths", "bind: folderPath: $folderPath")

            // Check if all paths in the folder are already selected
            val allPaths = getAllFilePaths(folderWithImageCount.folder)
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
                onItemClick(folderWithImageCount)
            }
        }

        // Helper function to get all file paths in the folder
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
