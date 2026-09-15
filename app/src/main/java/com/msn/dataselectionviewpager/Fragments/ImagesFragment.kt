package com.msn.dataselectionviewpager.Fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.msn.dataselectionviewpager.Adapter.dataShowingAdapters.ImageAdapter
import com.msn.dataselectionviewpager.Adapter.FolderAdapters.ImagesFolderAdapter
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.ViewModel.MediaViewModel
import com.msn.dataselectionviewpager.databinding.FragmentImagesBinding

class ImagesFragment : Fragment(R.layout.fragment_images) {

    private lateinit var viewModel: MediaViewModel
    private var _binding: FragmentImagesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentImagesBinding.bind(view)
        viewModel = ViewModelProvider(requireActivity()).get(MediaViewModel::class.java)

        val imageAdapter = ImageAdapter(requireContext())

        val imagesFolderAdapter = ImagesFolderAdapter(requireContext()) { folderWithImageCount ->
            binding.recyclerView.visibility = View.GONE
            binding.recyclerViewImages.visibility = View.VISIBLE

            val imageUris = folderWithImageCount.folder.listFiles { file ->
                file.isFile && file.extension in listOf("jpg", "jpeg", "png", "gif")
            }?.map { Uri.fromFile(it) } ?: emptyList()

            imageAdapter.submitList(imageUris)

            Toast.makeText(requireContext(), "Showing images for folder: ${folderWithImageCount.folder.name}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = imagesFolderAdapter
        }

        binding.recyclerViewImages.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = imageAdapter
        }

        viewModel.imageFolders.observe(viewLifecycleOwner) { folderWithImageCounts ->
            if (folderWithImageCounts != null) {
                imagesFolderAdapter.submitList(folderWithImageCounts)
            }
        }

        viewModel.loadImageFolders(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.recyclerViewImages.visibility == View.VISIBLE) {
                        binding.recyclerViewImages.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
