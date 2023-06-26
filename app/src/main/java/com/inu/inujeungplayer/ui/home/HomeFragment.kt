package com.inu.inujeungplayer.ui.home

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.inu.inujeungplayer.R
import com.inu.inujeungplayer.adapter.MusicAdapter
import com.inu.inujeungplayer.constant.MusicConstants
import com.inu.inujeungplayer.constant.MusicDevice.isRadioOn
import com.inu.inujeungplayer.constant.MusicDevice.isVideo
import com.inu.inujeungplayer.constant.MusicDevice.musicIDList
import com.inu.inujeungplayer.constant.MusicDevice.musicList
import com.inu.inujeungplayer.constant.MusicDevice.oldMusic
import com.inu.inujeungplayer.constant.MusicDevice.songID
import com.inu.inujeungplayer.constant.PendinIntent
import com.inu.inujeungplayer.constant.PendinIntent.lPauseIntent
import com.inu.inujeungplayer.databinding.FragmentHomeBinding
import com.inu.inujeungplayer.model.Music
import com.inu.inujeungplayer.utils.Injector
import com.inu.inujeungplayer.utils.MyApplication
import com.inu.inujeungplayer.utils.fastscroller.FastScroller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import kotlin.system.exitProcess


private const val DELETE_PERMISSION_REQUEST = 0x1033
private const val UPDATE_PERMISSION_REQUEST = 0x1023
class HomeFragment : Fragment() {
    private lateinit var callback: OnBackPressedCallback
    //    val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
//    val homeViewModel: HomeViewModel by activityViewModels { ViewModelFactory(MyApplication.repository!!) }
    private val homeViewModel: HomeViewModel by viewModels {
        Injector.provideHomeListViewModelFactory(MyApplication.applicationContext())
    }
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    lateinit var permissions: Array<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var musicAdapter: MusicAdapter
    lateinit var handleView: ImageView
    lateinit var recyclerView: RecyclerView

    private var deletedMusic: Music? = null
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>

   /* companion object {
        private const val ARG_PATH: String = "com.inu.inujeungplayer"
        fun build(block: Builder.()-> Unit) = Builder().apply(block).build()
        val filesListFragment = build {
            path = Environment.getExternalStorageDirectory().absolutePath
        }
    }*/
    /*class Builder{
        var path : String = ""
        fun build(): FilesFragment{
            val fragment = FilesFragment()
            val args = Bundle()
            args.putString(HomeFragment.ARG_PATH, path)
            fragment.arguments = args
            return fragment
        }
    }*/
//    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

       Log.d("intentSenderLauncher1","size of musiclist = ${musicList.size}")
       musicList = emptyList()
       Log.d("intentSenderLauncher2","size of musiclist = ${musicList.size}")

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        recyclerView = binding.recyclerViewList
        val searchView = binding.searchViewMusic
        searchView.bringToFront() // 서티뷰 터치시 아래 안보이게

        musicAdapter =  MusicAdapter( { music, view -> menuActions(music, view) },
            { music -> itemClick(music) }, { music -> artistLongClick(music)})

        recyclerView.itemAnimator = null // notifiitemchanged 화면깜빡임 방지
        recyclerView.setHasFixedSize(true)// notifiitemchanged 화면깜빡임 방지

        handleView = binding.handleView
        handleView.clipToOutline = true
        handleView.bringToFront()
//        childFragManager = ChildFra
        recyclerView.also { view ->
            view.layoutManager = GridLayoutManager(requireContext(), 1)
            view.adapter = musicAdapter
        }
        FastScroller(handleView).bind(recyclerView) /*, bubbleListener*/

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                homeViewModel.searchQueryMusics("%$query%").observeOnce(requireActivity()) {
                    musicAdapter.submitList(it)
                    musicList = it
                    musicIDList.clear()
                    for (m in musicList) {
                        musicIDList.add(m.id)
                        if (m.isSelected == true  && m.id != songID) {
                            m.isSelected = false
                            homeViewModel.updateMusicFalse(m)
                        }
                    }
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        binding.buttonAllmusic.setOnClickListener {// 처음실행시 불러오는 것으로 하면 false 초기화됨
//            isVideo = false
//            Log.d("buttonAllmusic","songID=> ${musicList[musicIDList.indexOf(songID)].id}")
            homeViewModel.reLoadMusics().observeOnce(this, Observer<List<Music>>  {
                if(it != null){
                    musicAdapter.submitList(it)
                    musicList = it
                    musicIDList.clear()
                    for (m in musicList) {
                        musicIDList.add(m.id)
                        if (m.isSelected == true && m.id != songID) {
                            m.isSelected = false
                            homeViewModel.updateMusicFalse(m)
//                            Log.d("buttonAllmusic","m id=> ${m.id}")
                        }
                    }
                }
            })
        }

        binding.imageViewShuffle.setOnClickListener {
            if (!isRadioOn) {
                val temp = musicList.shuffled()
                musicAdapter.submitList(temp)
                musicList = temp
                musicIDList.clear()
                for (m in musicList) {
                    musicIDList.add(m.id)
                }
            }
        }

        binding.imageViewGoto.setOnClickListener {
            recyclerView.scrollToPosition(musicIDList.indexOf(songID))
        }

        binding.imageViewFile.setOnClickListener {
            isVideo = true
            updateVideo()
        }

        permissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO)
        }//, Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)

       requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
//                if (!isLoadAll) // 부팅시만 전체 로딩
//                    index = -1
                updateData()
                subscribeUi(musicAdapter)
                subscribeMusic(musicAdapter)

            } else {
                Toast.makeText(requireContext(), "권한 요청 실행해야지 앱 실행", Toast.LENGTH_SHORT).show()
                exitProcess(0)
            }
        }
        permissions.forEach { p ->
            requestPermissionLauncher.launch(p)
//            Log.d("FragmentListVM", " permissions.forEach, isLoadAll= $isLoadAll" )
        }

        MyApplication.musicRepository.songid.observe(viewLifecycleOwner) { id ->
            musicAdapter.submitList(musicList)
            Log.d("songidobserve","${id}")
            itempUpdate(id)
        }

       intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
           if(it.resultCode == RESULT_OK) {
               if(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                   lifecycleScope.launch {
                       deletePhotoFromExternalStorage(Uri.parse(deletedMusic?.contentUri) ?: return@launch)
                   }
               }
               homeViewModel.deleteMusic(deletedMusic!!)
               val mutableList = musicList as MutableList
                mutableList.removeAt(musicList.indexOf(deletedMusic))
               Toast.makeText(requireContext(), "Photo deleted successfully", Toast.LENGTH_SHORT).show()
//               musicAdapter.notifyChanged(musicIDList.indexOf(deletedMusic!!.id))

               Log.d("intentSenderLauncher","after deleted = ${musicList.size}")
               /*val temp = musicList.shuffled()
               musicAdapter.submitList(temp)
               musicList = temp
               musicIDList.clear()
               for (m in musicList) {
                   musicIDList.add(m.id)
               }*/

           } else {
               Toast.makeText(requireContext(), "Photo couldn't be deleted", Toast.LENGTH_SHORT).show()
           }
       }

        return root
    }

    /*******************************Fragment End******************************************/
    private fun deleteMusic(music: Music) {
        Log.d("performUpdateMusic","deleteMusic")
        lifecycleScope.launch {
            performDeleteMusic(music)
        }

        deletedMusic = music
    }
    private suspend fun performDeleteMusic(music: Music) {
        Log.d("performUpdateMusic","performDeleteMusic")
        withContext(Dispatchers.IO) {
            try {
                MyApplication.applicationContext().contentResolver.delete(
                    Uri.parse(music.contentUri),
                    "${MediaStore.Audio.Media._ID} = ?",
                    arrayOf(music.id.toString())
                )
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(MyApplication.applicationContext().contentResolver
                            , listOf(Uri.parse(music.contentUri))).intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else -> null
                }

                intentSender?.let { sender ->
                    intentSenderLauncher.launch(
                        IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }
    private suspend fun deletePhotoFromExternalStorage(photoUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                requireActivity().contentResolver.delete(photoUri, null, null)
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(requireActivity().contentResolver, listOf(photoUri)).intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else -> null
                }
                intentSender?.let { sender ->
                    intentSenderLauncher.launch(
                        IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }

    // Kotlin Extenstion Function (Int)
    fun getResourceUri(resId : String): Uri {
        return Uri.parse("android.resource://" + R::class.java.getPackage().name + "/" + resId)
    }
    fun fielsToMusics(files: Map<String, List<Music>>) {
        lateinit var m : Music
        val resid = "${R.drawable.folder}"
        val uri: Uri = getResourceUri(resid)
        /*val filesList = files.map {
            if (it.path == (File("/storage/emulated/0").parentFile?.path ?: "/storage/emulated/0")) {
                Log.d("filestoraggg", "if: ${renderParentLink(requireContext())}")
                renderParentLink(requireContext())
            } else {
                Log.d("filestoraggg", "else: ${renderItem(requireContext(), it)}")
                renderItem(requireContext(), it)
            }
        }*/
        val mList = mutableListOf<Music>()
        val pathList = files.keys.toHashSet()
        // List를 Set으로 변경
        val set :Set<String> = HashSet<String>(pathList)
        // Set을 List로 변경
        val newList : List<String>  = ArrayList<String>(set)
        Log.d("fielsToMusics", "${pathList}")

        for ((i, f) in pathList.withIndex()) {
            m = Music(i,"", f, "", null, 5000, uri.toString(),"","",false,false,"",i)
            mList.add(m)
        }
        musicAdapter.submitList(mList)
    }
 
    private fun updateData() { // mp3 추가시
        CoroutineScope(Dispatchers.Default).launch {
//            homeViewModel.setMusicsData()
            musicList = homeViewModel.setMusicsData()
//            Log.d("dddddddddddddd","updateData(home), ${musicList.size} ")
            musicIDList.clear()
            for (m in musicList) {
                musicIDList.add(m.id)
                if (m.isSelected == true && m.id != songID) { // 처음 로딩시 일찍 음악 클릭하면 그것까지 false 제외
                    m.isSelected = false
                    homeViewModel.updateMusicFalse(m)
                }
            }
            musicAdapter.submitList(musicList) // 처음 설치때 위해 submitlist
        }
    }
    private fun updateVideo() { // vedio 추가시
        CoroutineScope(Dispatchers.Default).launch {
            musicList = homeViewModel.setVideosData()
            musicIDList.clear()
            for (m in musicList) {
                musicIDList.add(m.id)
                if (m.isSelected == true && m.id != songID) { // 처음 로딩시 일찍 음악 클릭하면 그것까지 false 제외
                    m.isSelected = false
                    homeViewModel.updateMusicFalse(m)
                }
            }
            musicAdapter.submitList(musicList) // 처음 설치때 위해 submitlist
/*
            var i = 0
            for (v in musicList) {
                val file = File(v.path)
                if (file.exists()) {
                    i+=1
                }
            }
            Log.d("fileexist:","$i,  ${musicList.size}")*/
        }
    }
    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }
    /*fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
        var firstObservation = true
        observe(owner, object: Observer<T> {
            override fun onChanged(value: T) {
                if(firstObservation)
                {
                    firstObservation = false
                }
                else
                {
                    removeObserver(this)
                    observer(value)
                }
            }
        })
    }*/
    private fun subscribeUi(adapter: MusicAdapter) { // 처음 실행시 모두 false
        homeViewModel.musics.observeOnce(this, Observer<List<Music>>  {
            if(it != null){
                adapter.submitList(it)
                musicList = it
                musicIDList.clear()
                for (m in musicList) {
                    musicIDList.add(m.id)
                    if (m.isSelected == true) {
                        m.isSelected = false
                        homeViewModel.updateMusicFalse(m)
                    }
                }
            }
        })
    }

    // 위 _songid observer 용
    private fun itemClick(music: Music) {
//        MyApplication.musicRepository._songid.value = music.id
    }
    private fun itempUpdate(id: Int) {
//        Log.d("itempUpdate(iss))", " music: $")
        val i = musicIDList.indexOf(id)
        if (i != -1) {
            val m = musicList[i]
            if (id != oldMusic?.id) {
                oldMusic?.isSelected = false
                if (oldMusic != null) homeViewModel.updateMusicFalse(oldMusic!!)
                musicAdapter.notifyChanged(musicIDList.indexOf(oldMusic?.id))

                m.isSelected = true
                homeViewModel.updateMusicTrue(m) // 룸변경시 전체로드됨, postValue만 함
//            musicAdapter.notifyChanged(m.id)
//                   Log.d("itemClicks(if))", " music: ${music.isSelected}: ${music.title}, ${oldMusic?.isSelected}:${oldMusic?.title},  ")
            } else {
                m.isSelected = false
                homeViewModel.updateMusicTrue(m)
//                   Log.d("itemClicks(else)", " music: ${music.isSelected}: ${music.title}, ${oldMusic?.isSelected}:${oldMusic?.title},  ")
            }
        }
    }

    private fun subscribeMusic(adapter: MusicAdapter) {
        homeViewModel.music.observe(viewLifecycleOwner) {
            adapter.notifyChanged(musicIDList.indexOf(it.id))
//            Log.d("itemClick(subscribeMusic)","${it.title}")
        }
    }

    private fun menuActions(music: Music, view: View) {

        val pop= PopupMenu(view.context, view)
        pop.inflate(R.menu.itemlist_menu)
        pop.setOnMenuItemClickListener { item->
            when(item.itemId)
            {
                R.id.action_info->{
                    homeViewModel.showInfo(view, music)
                }
                R.id.action_modify->{
                    Log.d("performUpdateMusic","R.id.action_modify")
                    val updateView =  View.inflate(view.context, R.layout.update_dialog, null)
                    val editTextArtist = updateView.findViewById<EditText>(R.id.editTextArtistName)
                    val editTextTitle = updateView.findViewById<EditText>(R.id.editTextTitle)
                    editTextArtist.setText(music.artist, TextView.BufferType.SPANNABLE)
                    editTextTitle.setText(music.title, TextView.BufferType.SPANNABLE)
                    val dlg = MaterialAlertDialogBuilder(requireContext())
                    dlg.setView(updateView)
                        .setPositiveButton(R.string.update_dialog_positive, DialogInterface.OnClickListener
                        { dialog, which ->
                            try {
                                Files.delete(FileSystems.getDefault().getPath(music.path))
                                println("performUpdateMusic: Deletion succeeded.")
                            } catch (e: IOException) {
                                println("performUpdateMusic: Deletion failed. ${music.path}")
                                e.printStackTrace()
                            }
                            Log.d("performUpdateMusic","R.id.action_delete?")
                            Log.d("performUpdateMusic", "currentposition=> ${music.currentPosition}, ${editTextArtist.text}, ${editTextTitle.text}")
                            music.title = editTextTitle.text.toString()
                            music.artist = editTextArtist.text.toString()
//                            homeViewModel.updateMusic(music)

                        })
                        .setNegativeButton(R.string.delete_dialog_negative, DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        })
                        .show()
                }
                R.id.action_delete-> {
                    Log.d("intentSenderLauncher","action_deleted = ${musicList.size}, deletedIndex= ${musicList.indexOf(music)}")
                    Log.d("performUpdateMusic","R.id.action_delete")
                    val builder = MaterialAlertDialogBuilder(requireContext())
                    builder
                        .setTitle(R.string.delete_dialog_title)
                        .setMessage(getString(R.string.delete_dialog_message, music.title))
                        .setPositiveButton(R.string.delete_dialog_positive,  DialogInterface.OnClickListener
                        { _, _ ->
                            deleteMusic(music)
                            Log.d("performUpdateMusic"," Deletion succeeded.")
//                            println("performUpdateMusic: Deletion succeeded.")
                            /*try {
                                Files.delete(FileSystems.getDefault().getPath(music.path))
                                println("performUpdateMusic: Deletion succeeded.")
                            } catch (e: IOException) {
                                println("performUpdateMusic: Deletion failed.")
                                e.printStackTrace()
                            }*/
                        })
                        .setNegativeButton(R.string.delete_dialog_negative, DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                            println("performUpdateMusic: Deletion succeeded.")
                            dialog.dismiss()
                        })
                        .show()
                }
            }
            true
        }
        pop.show()
    }


    private fun artistLongClick(music: Music) {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                activity?.supportFragmentManager?.popBackStack("file", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                childFragmentManager.popBackStack(Environment.getExternalStorageDirectory().absolutePath, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                Log.d("aaaaaaa", "occur back pressed event!!")
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
        // 화면 올리면 서비스 프로세스 종료
        lPauseIntent.action = MusicConstants.ACTION.STOP_ACTION
//        MusicDevice.isPlaying = false
        val lPendingPauseIntent = PendingIntent.getService(requireContext(),0, PendinIntent.lPauseIntent, PendingIntent.FLAG_IMMUTABLE)
        try {
            lPendingPauseIntent.send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
        Log.d("MusicUpdate_rootView(else)", " onDetach")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



// show the spinner when [spinner] is true
/*homeViewModel.spinner.observe(viewLifecycleOwner) { show ->
    binding.spinner.visibility = if (show) View.VISIBLE else View.GONE
}
// Show a snackbar whenever the [snackbar] is updated a non-null value
homeViewModel.snackbar.observe(viewLifecycleOwner) { text ->
    text?.let {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
        homeViewModel.onSnackbarShown()
    }
}*/
/*homeViewModel.musicInNoti.observe(viewLifecycleOwner) {// 서비스 oncompletion시 변경 주입
    itempUpdate(it.id)
    musicAdapter.submitList(musicList)
    Log.i("musicInNoti", "fraf) ${id}")
}*/