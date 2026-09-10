package com.msn.dataselectionviewpager.Fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.msn.dataselectionviewpager.Adapter.dataShowingAdapters.AudioAdapter
import com.msn.dataselectionviewpager.Adapter.FolderAdapters.AudioFolderAdapter
import com.msn.dataselectionviewpager.R
import com.msn.dataselectionviewpager.Utils.PermissionHelper
import com.msn.dataselectionviewpager.ViewModel.MediaViewModel
import com.msn.dataselectionviewpager.databinding.FragmentAudiosBinding


class AudiosFragment : Fragment(R.layout.fragment_audios) {

    private lateinit var binding: FragmentAudiosBinding
    private lateinit var viewModel: MediaViewModel

    private lateinit var audioFolderAdapter: AudioFolderAdapter
    private lateinit var audioAdapter: AudioAdapter
    private lateinit var permissionHelper: PermissionHelper
    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionHelper.handlePermissionResult(permissions)
        }

    private val requestManageStorageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            permissionHelper.handleManageStorageResult()
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAudiosBinding.inflate(inflater, container, false)
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
        // Initialize adapters
        audioFolderAdapter = AudioFolderAdapter (requireContext()){ folderWithAudioCount ->
            // Hide folder list and show audio files
            binding.recyclerView.visibility = View.GONE
            binding.recyclerViewAudios.visibility = View.VISIBLE

            // Load the audio files in the selected folder
            viewModel.loadAudiosFromFolder(folderWithAudioCount.folder)
        }

        audioAdapter = AudioAdapter(requireContext())

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = audioFolderAdapter
        }

        binding.recyclerViewAudios.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = audioAdapter
        }

        // Observe the list of audio folders and update the folder adapter
        viewModel.audioFolders.observe(viewLifecycleOwner) { folderWithAudioCounts ->
            // Ensure we only display audio folder data
            audioFolderAdapter.submitList(folderWithAudioCounts)
        }

        // Observe the list of audio files and update the audio adapter
        viewModel.audiosInFolder.observe(viewLifecycleOwner) { audios ->
            // Display the audio files in the selected folder
            audioAdapter.submitList(audios)
        }

        // Load audio folders
        viewModel.loadAudioFolders(requireContext())

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.recyclerViewAudios.visibility == View.VISIBLE) {
                        binding.recyclerViewAudios.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
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
            viewModel.loadAudioFolders(requireContext())
        }

    }
    private fun onStoragePermissionGranted() {
        binding.incPermLayout.storageroot.visibility = View.GONE
        viewModel.loadAudioFolders(requireContext())
        Toast.makeText(requireContext(), "Storage permission granted", Toast.LENGTH_SHORT).show()
    }
}
