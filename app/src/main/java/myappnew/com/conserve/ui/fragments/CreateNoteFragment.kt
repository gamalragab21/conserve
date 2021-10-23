package myappnew.com.conserve.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
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
import kotlinx.android.synthetic.main.item_container_note.*
import kotlinx.android.synthetic.main.layout_dialog_add_url.*
import kotlinx.android.synthetic.main.layout_persistent_bottom_sheet.*
import myappnew.com.conserve.R
import myappnew.com.conserve.helper.EventObserver
import myappnew.com.conserve.helper.Resource
import myappnew.com.conserve.ui.dialogs.AddUrlDialogs
import myappnew.com.conserve.ui.viewmodels.CreateNoteViewModel
import myappnew.com.conserve.utils.hideKeyboard
import myappnew.com.conserve.utils.snackbar
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class CreateNoteFragment : Fragment(R.layout.fragment_create_note) {

    private val TAG = "CreateNoteFragment"
    private lateinit var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>

    private var selecteColorNote : String = "#333333"

    private val createNoteViewModel : CreateNoteViewModel by viewModels()

    @Inject
    lateinit var glide : RequestManager


    private val args:CreateNoteFragmentArgs by navArgs()
//    private var note:Note?=null

    private var isUpdate=false

     private var idOfNoteUpdated: Int?=null

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)

       args.note?.let { noteUpdate ->
           isUpdate=true
           idOfNoteUpdated=noteUpdate.id
          // note=noteUpdate
           input_title.setText(noteUpdate.title)
           text_data_time.text = noteUpdate.dateTime
           input_subTitle.setText(noteUpdate.subTitle)
           input_note.setText(noteUpdate.note_text)
      //     glide.load(note?.imagePath).into(imageNote)
          // textUrl.text = note?.webLink
           setNoteColorIndicator()
           noteUpdate.webLink?.let {url->
               createNoteViewModel.setWebLink(Resource.Success(url),null)
           }
           noteUpdate.imagePath?.let {uri->
               createNoteViewModel.setCurImageUri(Uri.parse(uri))
           }

           noteUpdate.color?.let {color->
                selecteColorNote=color
            }
       }

        // foundations
        subscribeToObservers()
        initBottomSheet()
        setNoteColorIndicator()

        createNoteViewModel.setColorIndicatorStatus(selecteColorNote)


        // clicks
        ic_back.setOnClickListener {
            it.hideKeyboard()
            findNavController().popBackStack()
        }

        ic_save.setOnClickListener {
            it.hideKeyboard()
          if (!isUpdate) {
              createNoteViewModel.saveNote(input_title , input_subTitle,input_note)
          } else {
              idOfNoteUpdated?.let {
                  createNoteViewModel.updateNote(it,input_title , input_subTitle,input_note)
              }
            }

        }
    }

    private fun subscribeToObservers() {


        // for time and data
        createNoteViewModel.timeStatus.observe(viewLifecycleOwner , EventObserver(
            onError = {
                snackbar(it)
                ic_progress_create_note.isVisible = false
            } ,
            onLoading = {
                ic_progress_create_note.isVisible = true

            }
        ) { time_data ->
            ic_progress_create_note.isVisible = false
            text_data_time.apply {
                if (!isUpdate)   text = time_data
            }
        })
        // for color indicator
        createNoteViewModel.colorIndicatorStatus.observe(
            viewLifecycleOwner ,
            EventObserver(onError = {
                snackbar(it)
                ic_progress_create_note.isVisible = false
            } , onLoading = {
                ic_progress_create_note.isVisible = true
            } , { colorIndicator ->
                ic_progress_create_note.isVisible = false
                colorIndicator?.let {
                    setColorIndicator(colorIndicator)
                }
            })
        )

        // for save note
        createNoteViewModel.createNoteStatus.observe(viewLifecycleOwner , EventObserver(
            onError = {
                snackbar(it)
                ic_progress_create_note.isVisible = false
            } ,
            onLoading = {
                ic_progress_create_note.isVisible = true

            }
        ) { coulmInser ->
            ic_progress_create_note.isVisible = false
            if (coulmInser > 0) {
                snackbar(getString(R.string.save_note))

                    findNavController().navigate(CreateNoteFragmentDirections.actionCreateNoteFragmentToHomeFragment())

            }
        })
        // for update note
        createNoteViewModel.updateNoteStatus.observe(viewLifecycleOwner , EventObserver(
            onError = {
                snackbar(it)
                Log.i(TAG , "subscribeToObservers: $it")
                ic_progress_create_note.isVisible = false
            } ,
            onLoading = {
                ic_progress_create_note.isVisible = true

            }
        ) { coulmUpdate ->
            Log.i(TAG , "subscribeToObservers: $coulmUpdate")
            ic_progress_create_note.isVisible = false
            if (coulmUpdate > 0) {
                snackbar(getString(R.string.update_note))

                findNavController().navigate(CreateNoteFragmentDirections.actionCreateNoteFragmentToHomeFragment())

            }
        })
        // for delete note
        createNoteViewModel.deleteNoteStatus.observe(viewLifecycleOwner , EventObserver(
            onError = {
                snackbar(it)
                ic_progress_create_note.isVisible = false
            } ,
            onLoading = {
                ic_progress_create_note.isVisible = true

            }
        ) { coulmUpdate ->
            ic_progress_create_note.isVisible = false
            if (coulmUpdate > 0) {

                snackbar(getString(R.string.delete_note_success))

                findNavController().navigate(CreateNoteFragmentDirections.actionCreateNoteFragmentToHomeFragment())

            }
        })
        // for add url
        createNoteViewModel.webLinkStatus.observe(viewLifecycleOwner , EventObserver(
            onError = {
                snackbar(it)
                ic_progress_create_note.isVisible = false
            } ,
            onLoading = {
                ic_progress_create_note.isVisible = true
            }
        ) { url ->
            ic_progress_create_note.isVisible = false
            textUrl.apply {
                 text = url
            }
        })
        // set uri image
        createNoteViewModel.curImageUri.observe(viewLifecycleOwner , {
           glide.load(it).into(imageNote)
        })

    }

    private fun initBottomSheet() {

        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)

        Miscellaneous.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet : View , newState : Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    delete_note.isVisible = idOfNoteUpdated != null
                    setNoteColorIndicator()
                    bottomSheet.hideKeyboard()
                }
            }

            override fun onSlide(bottomSheet : View , slideOffset : Float) {

            }
        })

        ic_add_image_bottom_sheet.setOnClickListener {
            addImage()
        }
        ic_add_url_bottom_sheet.setOnClickListener {
            dialogAddUrl()
        }

        ic_copy.setOnClickListener {
            copyTextNote()
        }

        delete_note.setOnClickListener {
            dialogDeleteNote()
        }
    }

    private fun dialogDeleteNote() {
        idOfNoteUpdated?.let {
            createNoteViewModel.delete(it)

        }
    }

    private fun copyTextNote() {
        val clipBoard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Text Note", input_note.text.toString())
        if (clipBoard != null && clipData != null && input_note.text.toString().trim().isNotEmpty()) {
            clipBoard.setPrimaryClip(clipData)
            snackbar(getString(R.string.text_copied))
        }
    }

    private fun dialogAddUrl() {
     //  findNavController().navigate(R.id.addUrlDialog)
        AddUrlDialogs().apply {
            setPositiveAddUrlListener { url ,dialoge->
                createNoteViewModel.setWebLink(Resource.Success(url),dialoge)
            }
        }.show(childFragmentManager, null)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

    }

    private fun setNoteColorIndicator() {
        noteDefault1.setOnClickListener {
            ic_done_note1.visibility = View.VISIBLE
            ic_done_note2.visibility = View.GONE
            ic_done_note3.visibility = View.GONE
            ic_done_note4.visibility = View.GONE
            ic_done_note5.visibility = View.GONE
            selecteColorNote = "#333333"
            createNoteViewModel.setColorIndicatorStatus(selecteColorNote)
        }
        note2.setOnClickListener {
            ic_done_note2.visibility = View.VISIBLE
            ic_done_note1.visibility = View.GONE
            ic_done_note3.visibility = View.GONE
            ic_done_note4.visibility = View.GONE
            ic_done_note5.visibility = View.GONE
            selecteColorNote = "#FDBE3B"
            createNoteViewModel.setColorIndicatorStatus(selecteColorNote)

        }
        note3.setOnClickListener {
            ic_done_note3.visibility = View.VISIBLE
            ic_done_note1.visibility = View.GONE
            ic_done_note2.visibility = View.GONE
            ic_done_note4.visibility = View.GONE
            ic_done_note5.visibility = View.GONE
            selecteColorNote = "#2196F3"
            createNoteViewModel.setColorIndicatorStatus(selecteColorNote)
        }
        note4.setOnClickListener {
            ic_done_note4.visibility = View.VISIBLE
            ic_done_note1.visibility = View.GONE
            ic_done_note3.visibility = View.GONE
            ic_done_note2.visibility = View.GONE
            ic_done_note5.visibility = View.GONE
            selecteColorNote = "#3A52Fc"
            createNoteViewModel.setColorIndicatorStatus(selecteColorNote)

        }
        note5.setOnClickListener {
            ic_done_note5.visibility = View.VISIBLE
            ic_done_note1.visibility = View.GONE
            ic_done_note3.visibility = View.GONE
            ic_done_note4.visibility = View.GONE
            ic_done_note2.visibility = View.GONE
            selecteColorNote = "#FFFFFF"
            createNoteViewModel.setColorIndicatorStatus(selecteColorNote)

        }

        when (selecteColorNote) {
            "#333333" -> {
                ic_done_note1.visibility = View.VISIBLE
                ic_done_note2.visibility = View.GONE
                ic_done_note3.visibility = View.GONE
                ic_done_note4.visibility = View.GONE
                ic_done_note5.visibility = View.GONE
            }
            "#FDBE3B" -> {
                ic_done_note2.visibility = View.VISIBLE
                ic_done_note1.visibility = View.GONE
                ic_done_note3.visibility = View.GONE
                ic_done_note4.visibility = View.GONE
                ic_done_note5.visibility = View.GONE
            }
            "#2196F3" -> {
                ic_done_note3.visibility = View.VISIBLE
                ic_done_note1.visibility = View.GONE
                ic_done_note2.visibility = View.GONE
                ic_done_note4.visibility = View.GONE
                ic_done_note5.visibility = View.GONE
            }
            "#3A52Fc" -> {
                ic_done_note4.visibility = View.VISIBLE
                ic_done_note1.visibility = View.GONE
                ic_done_note3.visibility = View.GONE
                ic_done_note2.visibility = View.GONE
                ic_done_note5.visibility = View.GONE
            }
            "#FFFFFF" -> {
                ic_done_note5.visibility = View.VISIBLE
                ic_done_note1.visibility = View.GONE
                ic_done_note3.visibility = View.GONE
                ic_done_note4.visibility = View.GONE
                ic_done_note2.visibility = View.GONE
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setColorIndicator(selecteColorNote : String) {
        val drawable = view_indicator.background as Drawable
        val color = Color.parseColor(selecteColorNote)
        drawable.setColorFilter(color , PorterDuff.Mode.SRC)
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

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun openGallery() {
        CropImage.startPickImageActivity(requireContext() , this@CreateNoteFragment)
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
                if (resultCode == RESULT_OK) {
                    val uri = CropImage.getPickImageResultUri(requireContext() , data)
                    cropImage(uri)
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == RESULT_OK) {
                    val imageUri = result.uri
                    //  createNoteViewModel.setCurImageUri( Resource.Success(imageUri))
                    createNoteViewModel.setCurImageUri(imageUri)
                }
            }

        }

    }


}