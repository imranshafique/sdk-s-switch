package com.msn.dataselectionviewpager.Adapter.dataShowingAdapters


import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msn.dataselectionviewpager.R

import androidx.core.content.FileProvider
import com.msn.dataselectionviewpager.Utils.AppConstant.selectedPath
import com.msn.dataselectionviewpager.dataClass.AudioModel
import java.io.File

class AudioAdapter(private val context: Context) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    private var uris = listOf<AudioModel>()

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val audioTitleTextView: TextView = itemView.findViewById(R.id.fileName)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

        fun bind(uri: AudioModel) {
            val audioTitle = uri.name
            audioTitleTextView.text = audioTitle
            val contentUri = uri.filePath

            checkBox.isChecked = selectedPath.contains(uri.filePath)
            // Set checkBox listener to add or remove URIs from the selected list
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                 selectedPath.add(uri.filePath)
                    Log.d("Selected URIs", "Added URI: $contentUri")
                    Log.d("Selected URIs", "Added selectedPath: $selectedPath")
                } else {
//                    SelectedItemsUriManager.removeUri(contentUri)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_audio, parent, false)
        return AudioViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val uri = uris[position]
        holder.bind(uri)
    }

    override fun getItemCount(): Int {
        return uris.size
    }

    fun submitList(uris: List<AudioModel>) {
        this.uris = uris
        notifyDataSetChanged()
    }
}


