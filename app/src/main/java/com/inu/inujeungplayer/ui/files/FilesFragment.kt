package com.inu.inujeungplayer.ui.files

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.inu.inujeungplayer.R
import com.inu.inujeungplayer.databinding.FragmentFilesBinding
import java.io.File

const val MANAGE_EXTERNAL_STORAGE_PERMISSION_REQUEST = 1
const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 2
class FilesFragment : Fragment() {

    private var hasPermission = false
    private var _binding: FragmentFilesBinding? = null
    private lateinit var currentDirectory: File
    private lateinit var filesList: List<File>
    private lateinit var adapter: ArrayAdapter<String>
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFilesBinding.inflate(inflater, container, false)
        binding.toolbar.inflateMenu(R.menu.file_manager_menu)

        setupUi()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        hasPermission = checkStoragePermission(activity)
        if (hasPermission) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                if (!Environment.isExternalStorageLegacy()) {
                    binding.rationaleView.visibility = View.GONE
                    binding.legacyStorageView.visibility = View.VISIBLE
                    return
                }
            }

            binding.rationaleView.visibility = View.GONE
            binding.filesTreeView.visibility = View.VISIBLE

            // TODO: Use getStorageDirectory instead https://developer.android.com/reference/android/os/Environment.html#getStorageDirectory()
            open(getExternalStorageDirectory())
        } else {
            binding.rationaleView.visibility = View.VISIBLE
            binding.filesTreeView.visibility = View.GONE
        }
    }

    fun setupUi() {
        binding.toolbar.setOnMenuItemClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
            true
        }

        binding.permissionButton.setOnClickListener {
            requestStoragePermission(activity)
        }

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf<String>())

        binding.filesTreeView.adapter = adapter
        binding.filesTreeView.setOnItemClickListener { _, _, position, _ ->
            Log.d("filestoraggg","setonClick")

            val selectedItem = filesList[position]
            open(selectedItem)

        }
    }

    private fun open(selectedItem: File) {
        if (selectedItem.isFile) {
            return openFile(requireContext(), selectedItem)
        }

        currentDirectory = selectedItem
        filesList = getFilesList(currentDirectory)

        adapter.clear()
        adapter.addAll(filesList.map {
            if (it.path == (selectedItem.parentFile?.path ?: "/storage/emulated/0")) {
                Log.d("filestoraggg","if: ${renderParentLink(requireContext())}")
                renderParentLink(requireContext())
            } else {
                Log.d("filestoraggg","else: ${renderItem(requireContext(), it)}")
                renderItem(requireContext(), it)
            }
        })

        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/*childFragmentManager.beginTransaction()
                .add( R.id.fragHome_container, filesListFragment)
                .setPrimaryNavigationFragment(filesListFragment)
                .setReorderingAllowed(true)
                .addToBackStack( Environment.getExternalStorageDirectory().absolutePath)
                .commit()*/