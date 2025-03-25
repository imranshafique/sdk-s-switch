package com.msn.dataselectionviewpager.Fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.ViewModel.MediaViewModel
import com.msn.dataselectionviewpager.databinding.FragmentVideosBinding

import com.msn.dataselectionviewpager.Adapter.dataShowingAdapters.VideoAdapter
import com.msn.dataselectionviewpager.Adapter.FolderAdapters.VideosFolderAdapter
import com.msn.dataselectionviewpager.Utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VideosFragment : Fragment(R.layout.fragment_videos) {

    private lateinit var viewModel: MediaViewModel
    private var _binding: FragmentVideosBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var glide: RequestManager // Inject Glide
    private lateinit var permissionHelper: PermissionHelper
    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionHelper.handlePermissionResult(permissions)
        }

    private val requestManageStorageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            permissionHelper.handleManageStorageResult()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVideosBinding.bind(view)
        viewModel = ViewModelProvider(requireActivity()).get(MediaViewModel::class.java)
        permissionHelper = PermissionHelper(
            requireContext(),
            requestStoragePermissionLauncher,
            requestManageStorageLauncher
        ) {
            onStoragePermissionGranted()
        }

        if (!permissionHelper.isStoragePermissionGranted()) {
            binding.incPermLayout.storageroot.visibility = View.VISIBLE
            binding.incPermLayout.grantpermission.setOnClickListener {
                permissionHelper.requestStoragePermissions()
            }
        }
        val videoAdapter = VideoAdapter(requireContext())

        val videosFolderAdapter = VideosFolderAdapter(lifecycleScope, glide) { folderWithVideoCount ->
            binding.videosRecyclerView.visibility = View.GONE
            binding.recyclerViewVideos.visibility = View.VISIBLE

            val videoUris = folderWithVideoCount.folder.listFiles { file ->
                file.isFile && file.extension in listOf("mp4", "mkv", "avi", "mov")
            }?.map { Uri.fromFile(it) } ?: emptyList()

            videoAdapter.submitList(videoUris)

         }

        binding.videosRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = videosFolderAdapter
        }

        binding.recyclerViewVideos.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = videoAdapter
        }

        viewModel.videoFolders.observe(viewLifecycleOwner) { folderWithVideoCounts ->
            videosFolderAdapter.submitList(folderWithVideoCounts)
        }

        viewModel.loadVideoFolders(requireContext())
//        binding.allselect.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                // Select all items
//                videoAdapter.selectAll()
//            } else {
//                // Deselect all items
//                videoAdapter.deselectAll()
//            }
//        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.recyclerViewVideos.visibility == View.VISIBLE) {
                        binding.recyclerViewVideos.visibility = View.GONE
                        binding.videosRecyclerView.visibility = View.VISIBLE
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {
        super.onResume()
        if (!permissionHelper.isStoragePermissionGranted()) {
            binding.incPermLayout.storageroot.visibility = View.VISIBLE
            binding.incPermLayout.grantpermission.setOnClickListener {
                permissionHelper.requestStoragePermissions()
            }
        }else{
            binding.incPermLayout.storageroot.visibility = View.GONE
            viewModel.loadVideoFolders(requireContext())
        }

    }
    private fun onStoragePermissionGranted() {
        binding.incPermLayout.storageroot.visibility = View.GONE
        viewModel.loadVideoFolders(requireContext())
        Toast.makeText(requireContext(), "Storage permission granted", Toast.LENGTH_SHORT).show()
    }
}

