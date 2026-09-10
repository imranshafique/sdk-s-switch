package com.msn.dataselectionviewpager.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.msn.dataselectionviewpager.Adapter.dataShowingAdapters.DocumentsAdapter
import com.msn.dataselectionviewpager.Adapter.FolderAdapters.DocumentsFolderAdapter
import com.msn.dataselectionviewpager.Utils.PermissionHelper
import com.msn.dataselectionviewpager.ViewModel.MediaViewModel
import com.msn.dataselectionviewpager.dataClass.DocumentModel
import com.msn.dataselectionviewpager.databinding.FragmentDocumentsBinding

class DocumentsFragment : Fragment() {

    private lateinit var documentViewModel: MediaViewModel
    private lateinit var folderAdapter: DocumentsFolderAdapter
    private lateinit var documentAdapter: DocumentsAdapter
    private var _binding: FragmentDocumentsBinding? = null
    private val binding get() = _binding!!
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
    ): View? {
         _binding = FragmentDocumentsBinding.inflate(inflater, container, false)
        documentViewModel = ViewModelProvider(requireActivity()).get(MediaViewModel::class.java)
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
        // Setup folder RecyclerView
        binding.documentsRecyclerView.layoutManager = GridLayoutManager(context, 2)
        folderAdapter = DocumentsFolderAdapter(requireContext()) { folderName, documents ->
            showDocuments(folderName, documents)
        }
        binding.documentsRecyclerView.adapter = folderAdapter

        // Setup document RecyclerView
        binding.recyclerViewDocuments.layoutManager = LinearLayoutManager(context)
        documentAdapter = DocumentsAdapter(requireContext())
        binding.recyclerViewDocuments.adapter = documentAdapter

        documentViewModel.categorizedDocuments.observe(viewLifecycleOwner, Observer { categorizedDocs ->
            folderAdapter.submitList(categorizedDocs)
        })

        documentViewModel.fetchAndCategorizeDocuments(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.recyclerViewDocuments.visibility == View.VISIBLE) {
                        binding.recyclerViewDocuments.visibility = View.GONE
                        binding.documentsRecyclerView.visibility = View.VISIBLE
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun showDocuments(folderName: String, documents: List<DocumentModel>) {
        binding.documentsRecyclerView.visibility = View.GONE
        binding.recyclerViewDocuments.visibility = View.VISIBLE
        documentAdapter.submitList(documents)
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
            documentViewModel.loadVideoFolders(requireContext())
        }

    }
    private fun onStoragePermissionGranted() {
        binding.incPermLayout.storageroot.visibility = View.GONE
        documentViewModel.fetchAndCategorizeDocuments(requireContext())
        Toast.makeText(requireContext(), "Storage permission granted", Toast.LENGTH_SHORT).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
         _binding = null
    }
}
