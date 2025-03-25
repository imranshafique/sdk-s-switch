package com.msn.dataselectionviewpager.Adapter.FolderAdapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.dataClass.FolderWithVideoCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class VideosFolderAdapter(
    private val coroutineScope: CoroutineScope, // Pass CoroutineScope from Fragment/Activity
    private val glide: RequestManager, // Injected Glide instance from Fragment/Activity
    private val onItemClick: (FolderWithVideoCount) -> Unit
) : RecyclerView.Adapter<VideosFolderAdapter.FolderViewHolder>() {

    private var folders = listOf<FolderWithVideoCount>()
    private val selectedPaths = mutableSetOf<String>() // Store selected paths

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val folderNameTextView: TextView = itemView.findViewById(R.id.folderName)
        private val folderThumbnailImageView: ImageView = itemView.findViewById(R.id.folderIcon)
        private val checkBox: CheckBox = itemView.findViewById(R.id.folderCheckBox)

        fun bind(folderWithVideoCount: FolderWithVideoCount) {
            val folder = folderWithVideoCount.folder
            folderNameTextView.text = "${folder.name} (${folderWithVideoCount.videoCount})"

            // Load thumbnail
            glide.load(folderWithVideoCount.thumbnail)
                .placeholder(R.drawable.ic_launcher_background)
                .into(folderThumbnailImageView)

            val folderPath = folder.path
            Log.d("paths", "bind: folderPath: $folderPath")

            // Get all video paths asynchronously
            coroutineScope.launch {
                val allPaths = getAllFilePaths(folder)
                withContext(Dispatchers.Main) {
                    checkBox.isChecked = allPaths.all { selectedPaths.contains(it) }

                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedPaths.addAll(allPaths)
                            Log.d("Selected URIs", "Added Paths: $allPaths")
                        } else {
                            selectedPaths.removeAll(allPaths)
                            Log.d("Selected URIs", "Removed Paths: $allPaths")
                        }
                        Log.d("Selected Path List", "Current selectedPaths: $selectedPaths")
                    }
                }
            }

            itemView.setOnClickListener {
                onItemClick(folderWithVideoCount)
            }
        }

        private suspend fun getAllFilePaths(folder: File): List<String> {
            return withContext(Dispatchers.IO) {
                folder.listFiles()?.filter { it.isFile }?.map { it.absolutePath } ?: emptyList()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FolderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(folders[position])
    }

    override fun getItemCount(): Int = folders.size

    fun submitList(newFolders: List<FolderWithVideoCount>) {
        folders = newFolders
        notifyDataSetChanged()
    }
}

