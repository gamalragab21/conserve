package myappnew.com.conserve.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import myappnew.com.conserve.R
import myappnew.com.conserve.helper.Constants.SEARCH_TIME_DELAY
import myappnew.com.conserve.helper.EventObserver
import myappnew.com.conserve.helper.Resource
import myappnew.com.conserve.ui.adapters.NoteAdapter
import myappnew.com.conserve.ui.dialogs.AddUrlDialogs
import myappnew.com.conserve.ui.viewmodels.CreateNoteViewModel
import myappnew.com.conserve.ui.viewmodels.HomeViewModel
import myappnew.com.conserve.utils.snackbar
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment :Fragment(R.layout.home_fragment) {
    private  val TAG = "HomeFragment"

    val viewMode:HomeViewModel by viewModels()
    private val createNoteViewModel : CreateNoteViewModel by activityViewModels()

    @Inject
   lateinit var noteAdapter : NoteAdapter

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        add_note_main.setOnClickListener {

            val action =HomeFragmentDirections.actionHomeFragmentToCreateNoteFragment()
            findNavController().navigate(action)
        }

        var job : Job? = null
        searchNote.addTextChangedListener { editable ->
            job?.cancel()
            job = lifecycleScope.launch {
                delay(SEARCH_TIME_DELAY)
                editable?.let {
                    if (it.isEmpty()) {
                        viewMode.getAllNotes()
                    }else viewMode.searchUser(it.toString())
               }
           }
        }

        subscribeToObservers()
        setupRecyclerView()

        noteAdapter.setOnDeleteClickListener {note->

            viewMode.delete(note)
        }

        noteAdapter.setOnNoteClickListener {note->

           val action =HomeFragmentDirections.actionHomeFragmentToCreateNoteFragment(note)
            findNavController().navigate(action)
        }
        quickActions()


    }
    private fun quickActions() {

        ic_add_note.setOnClickListener {

            val action = HomeFragmentDirections.actionHomeFragmentToCreateNoteFragment()
              findNavController().navigate(action)
                 }

        ic_add_image.setOnClickListener {

            addImage()
        }

        ic_add_url_web.setOnClickListener {

            dialogAddUrl()
        }

    }

    private fun dialogAddUrl() {
        //  findNavController().navigate(R.id.addUrlDialog)
        AddUrlDialogs().apply {
            setPositiveAddUrlListener { url ,dialoge->
                createNoteViewModel.setWebLink(Resource.Success(url),dialoge)
                val action = HomeFragmentDirections.actionHomeFragmentToCreateNoteFragment()
                findNavController().navigate(action)
            }
        }.show(childFragmentManager, null)
    }

    private fun addImage() {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
                override fun onPermissionGranted(response : PermissionGrantedResponse?) { /* ... */
                    openGallery()
                }

                override fun onPermissionDenied(response : PermissionDeniedResponse?) { /* ... */
                    snackbar(getString(R.string.denied_message))
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0 : com.karumi.dexter.listener.PermissionRequest? ,
                    p1 : PermissionToken?
                ) {
                    p1?.let {
                        it.continuePermissionRequest()
                    }
                }

            }).check()

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun openGallery() {
        CropImage.startPickImageActivity(requireContext() , this@HomeFragment)
    }

    fun cropImage(uri : Uri?) {
        uri?.let { myuri ->
            CropImage.activity(uri)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setMultiTouchEnabled(true)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(requireContext() , this)
        }

    }

    @SuppressLint("CheckResult")
    override fun onActivityResult(requestCode : Int , resultCode : Int , data : Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        Log.i("here" , "onActivityResult: ")
        when (requestCode) {
            CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri = CropImage.getPickImageResultUri(requireContext() , data)
                    cropImage(uri)
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val imageUri = result.uri
                    //  createNoteViewModel.setCurImageUri( Resource.Success(imageUri))
                    createNoteViewModel.setCurImageUri(imageUri)
                    val action =HomeFragmentDirections.actionHomeFragmentToCreateNoteFragment()
                    findNavController().navigate(action)
                }
            }

        }

    }

    private fun subscribeToObservers() {

        viewMode.listNoteStatus.observe(viewLifecycleOwner,EventObserver(
            onLoading = {
                ic_progress_notes.isVisible=true

            },
            onError = {
                snackbar(it)
                ic_progress_notes.isVisible=false
            }
        ){notes->

            ic_progress_notes.isVisible=false
            noteAdapter.notes=notes
            Log.i(TAG , "subscribeToObservers: $notes")
        })

        viewMode.deleteNoteStatus.observe(viewLifecycleOwner,EventObserver(
            onLoading = {
                ic_progress_notes.isVisible=true
            },
            onError = {
                snackbar(it)
                ic_progress_notes.isVisible=false
            }
        ){coulm->
            ic_progress_notes.isVisible=false
            noteAdapter.notifyDataSetChanged()
            if (coulm>0) snackbar(getString(R.string.delete_note_success))
            viewMode.getAllNotes()

        })

        viewMode.searchResults.observe(viewLifecycleOwner , EventObserver(
            onError = {
                ic_progress_notes.isVisible = false
                snackbar(it)
            } ,
            onLoading = {
                ic_progress_notes.isVisible = true
            }
        ) { notes ->

            ic_progress_notes.isVisible = false
            noteAdapter.notes = notes

        })





    }
    private fun setupRecyclerView() =rvListNote.apply {
        itemAnimator=null
        isNestedScrollingEnabled = false
        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter=noteAdapter

    }
}