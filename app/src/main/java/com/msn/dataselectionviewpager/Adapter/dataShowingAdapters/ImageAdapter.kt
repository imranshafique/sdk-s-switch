package com.msn.dataselectionviewpager.Adapter.dataShowingAdapters

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.Utils.AppConstant.selectedPath
import java.io.File




class ImageAdapter(private val context: Context) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private var uris = listOf<Uri>()
    private var paths = listOf<String>()

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        fun bind(uri: Uri) {
            // Convert file:// URI to content:// URI if necessary
            val contentUri = if (uri.scheme == "file") {
                convertFileUriToContentUri(uri)
            } else {
                uri // Already a content:// URI
            }

            // Use Glide for faster image loading
            Glide.with(context)
                .load(contentUri)
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(imageView)

            // Set checkbox state based on whether the URI is in the selected list
            checkBox.isChecked = selectedPath.contains(uri.path)

            // Handle checkbox click to add/remove URI from selected list
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                     selectedPath.add(uri.path!!)
                    Log.d("Selected URIs", "Added URI: $contentUri")
                    Log.d("Selected path", "Added selectedPath: $selectedPath")
                } else {
                    selectedPath.remove(uri.path)

                    Log.d("Selected URIs", "Removed URI: $contentUri")
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = uris[position]
//        val path = paths[position]
        Log.d("onBindViewHolder", "onBindViewHolder: uri: $uri, path: ")
        holder.bind(uri)
    }

    override fun getItemCount(): Int {
        return uris.size
    }

    fun submitList(uris: List<Uri>) {
        this.uris = uris
        notifyDataSetChanged()
    }
}
