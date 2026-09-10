package com.msn.dataselectionviewpager.Adapter.dataShowingAdapters

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.msn.dataselectionviewpager.R
 import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.msn.dataselectionviewpager.Utils.AppConstant.selectedPath
import java.io.File



class VideoAdapter(private val context: Context) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private var uris = listOf<Uri>()
    private var TAG = javaClass.simpleName

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoThumbnail: ImageView = itemView.findViewById(R.id.imageView)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        fun bind(uri: Uri) {
            val contentUri = if (uri.scheme == "file") {
                convertFileUriToContentUri(uri)
            } else {
                uri
            }
            Glide.with(context)
                .load(contentUri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_launcher_background)
                .into(videoThumbnail)
            val fileSize = getFileSize(contentUri)

            // Set checkbox state
            checkBox.isChecked = selectedPath.contains(uri.path)

            // Handle checkbox click
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!selectedPath.contains(uri.path)) {
                        selectedPath.add(uri.path!!)
                        Log.d(TAG, "bind: selectedPath: $selectedPath ")
                    }
                    Log.d(TAG, "Added: $contentUri, File Size: $fileSize")
                } else {
                    selectedPath.remove(uri.path)
                    Log.d(TAG, "Removed: $contentUri")
                }
            }
        }
    }

    // Convert file URI to content URI using FileProvider
    private fun convertFileUriToContentUri(fileUri: Uri): Uri {
        val file = File(fileUri.path ?: "")
        return FileProvider.getUriForFile(
            context,
            "com.msn.dataselectionviewpager.fileprovider", // Update with your FileProvider authority
            file
        )
    }

    // Get file size in bytes
    private fun getFileSize(uri: Uri): Long {
        var size: Long = 0
        val cursor: Cursor? = context.contentResolver.query(uri, arrayOf(MediaStore.Video.Media.SIZE), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                size = it.getLong(0)
            }
        }
        return size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return VideoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val uri = uris[position]
        holder.bind(uri)
    }

    override fun getItemCount(): Int = uris.size

    fun submitList(uris: List<Uri>) {
        this.uris = uris
        notifyDataSetChanged()
    }
}



/*class VideoAdapter(private val context: Context) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private var uris = listOf<Uri>()
    // This is the method that will be called when the "Select All" checkbox is clicked
    fun selectAll() {
        uris.forEach { uri ->
            val contentUri = convertFileUriToContentUri(uri) // Convert to content URI if it's a file URI
            // Add the content URI to the selected set
            SelectedItemsUriManager.addUri(contentUri)
            Log.d("ImageAdapter", "Added URI for Select All: $contentUri")
        }
        notifyDataSetChanged()
    }

    // This method will deselect all items in the current folder
    fun deselectAll() {
        // Remove all selected items from the selection manager
        SelectedItemsUriManager.clearSelectedUris()
        notifyDataSetChanged() // Refresh the RecyclerView to reflect the changes
        Log.d("ImageAdapter", "All items deselected")
    }
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoThumbnail: ImageView = itemView.findViewById(R.id.imageView)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        fun bind(uri: Uri) {
            val bitmap: Bitmap? = ThumbnailUtils.createVideoThumbnail(uri.path!!, MediaStore.Video.Thumbnails.MINI_KIND)

            bitmap?.let {
                videoThumbnail.setImageBitmap(it)
            }
             val contentUri = if (uri.scheme == "file") {
                convertFileUriToContentUri(uri)
            } else {
                uri
            }


            // Set checkbox state based on whether the URI is in the selected list
            checkBox.isChecked = SelectedItemsUriManager.getSelectedUris().contains(contentUri)

            // Handle checkbox click to add/remove URI from selected list
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    SelectedItemsUriManager.addUri(contentUri)
                    Log.d("Selected URIs", "Added URI: $contentUri")
                } else {
                    SelectedItemsUriManager.removeUri(contentUri)
                    Log.d("Selected URIs", "Removed URI: $contentUri")
                }
            }
        }



    }
    // Helper function to convert file URI to content URI using FileProvider
    private fun convertFileUriToContentUri(fileUri: Uri): Uri {
        val file = File(fileUri.path ?: "")
        return FileProvider.getUriForFile(
            context,
            "com.msn.dataselectionviewpager.fileprovider", // Replace with your own provider authorities
            file
        )
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return VideoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val uri = uris[position]
        holder.bind(uri)
    }

    override fun getItemCount(): Int = uris.size

    fun submitList(uris: List<Uri>) {
        this.uris = uris
        notifyDataSetChanged()
    }
}*/
