package com.msn.smartswitch.AppPrefs


import android.content.Context
import android.net.Uri
import android.widget.Toast

object SelectedItemsUriManager {
    // This holds the list of selected image URIs
    val listOfSelectedItemsUris: ArrayList<Uri> = ArrayList()
    val listOfSelectedItemsPaths: ArrayList<String> = ArrayList()

    // Add URI to the list if it's not already present
    fun addUri(uri: Uri) {
        if (!listOfSelectedItemsUris.contains(uri)) {
            listOfSelectedItemsUris.add(uri)
        }
    }
    fun addPath(path:String){
        if (!listOfSelectedItemsPaths.contains(path)) {
            listOfSelectedItemsPaths.add(path)
        }
    }

    // Remove URI from the list
    fun removeUri(uri: Uri) {
        listOfSelectedItemsUris.remove(uri)
    }

    // Clear the list of selected URIs
    fun clearSelectedUris() {
        listOfSelectedItemsUris.clear()
    }

    // Get the list of selected URIs
    fun getSelectedUris(): List<Uri> {
        return listOfSelectedItemsUris
    }

    // TODO:   Remove this method as it is not used
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
