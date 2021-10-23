package myappnew.com.conserve.ui.viewmodels

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import myappnew.com.conserve.R
import myappnew.com.conserve.entiteis.Note
import myappnew.com.conserve.helper.Constants.MAX_SUBTITLE_LENGTH
import myappnew.com.conserve.helper.Constants.MAX_TITLENOTE_LENGTH
import myappnew.com.conserve.helper.Constants.MIN_SUBTITLE_LENGTH
import myappnew.com.conserve.helper.Constants.MIN_TITLENOTE_LENGTH
import myappnew.com.conserve.helper.Event
import myappnew.com.conserve.helper.Resource
import myappnew.com.conserve.repositories.DefaultHomeRepository
import javax.inject.Inject


@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val repository : DefaultHomeRepository ,
    private val context : Context ,
    private val dispatcher : CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _createNoteStatus = MutableLiveData<Event<Resource<Long>>>()
    val createNoteStatus : LiveData<Event<Resource<Long>>> = _createNoteStatus

    private val _updateNoteStatus = MutableLiveData<Event<Resource<Int>>>()
    val updateNoteStatus : LiveData<Event<Resource<Int>>> = _updateNoteStatus


    private val _deleteNoteStatus = MutableLiveData<Event<Resource<Int>>>()
    val deleteNoteStatus : LiveData<Event<Resource<Int>>> = _deleteNoteStatus

    private val _timeStatus = MutableLiveData<Event<Resource<String>>>()
    val timeStatus : LiveData<Event<Resource<String>>> = _timeStatus


    private val _curImageUri = MutableLiveData<Uri>()
    val curImageUri : LiveData<Uri> = _curImageUri

    private val _colorIndicatorStatus = MutableLiveData<Event<Resource<String>>>()
    val colorIndicatorStatus : LiveData<Event<Resource<String>>> = _colorIndicatorStatus

    private val _webLinkStatus = MutableLiveData<Event<Resource<String>>>()
    val webLinkStatus : LiveData<Event<Resource<String>>> = _webLinkStatus

    fun setColorIndicatorStatus(color : String) {
        _colorIndicatorStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.setColorIndicator(color)
            _colorIndicatorStatus.postValue(Event(result))
        }
    }


    fun setWebLink(url : Resource.Success<String> , dialoge : Dialog?) {
          url.data?.let {
              if (url.data.trim().isEmpty()) {
                  val error = context.getString(R.string.web_link_empty)
                  _webLinkStatus.postValue(Event(Resource.Error(error)))
              } else if (! Patterns.WEB_URL.matcher(url.data.toString()).matches()) {
                  val error = context.getString(R.string.invalid_web_link)
                  _webLinkStatus.postValue(Event(Resource.Error(error)))
              } else {
                  _webLinkStatus.postValue(Event(Resource.Loading()))
                  viewModelScope.launch(dispatcher) {
                      _webLinkStatus.postValue(Event(url))
                  }
                  dialoge?.dismiss()
              }
          }
    }
    fun setWebLink2(url : Resource.Success<String> , dialoge : Dialog?) {
        url.data?.let {
                _webLinkStatus.postValue(Event(Resource.Loading()))
                viewModelScope.launch(dispatcher) {
                    _webLinkStatus.postValue(Event(url))
                }
                dialoge?.dismiss()
        }
    }

    fun setCurImageUri(uri : Uri?) {
        uri?.let {uri->
            viewModelScope.launch(dispatcher) {
                _curImageUri.postValue(uri)

            }
        }

    }

    fun getTimeAndData() {
        _timeStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.getTimeAndData()
            _timeStatus.postValue(Event(result))
        }

    }


    fun saveNote(inputTitle : EditText? , inputSubtitle : EditText? , input_note : EditText) {
        when {
            inputTitle?.text.toString().trim().isEmpty() -> {
                val error = context.getString(R.string.input_title_empty)
                _createNoteStatus.postValue(Event(Resource.Error(error)))
                inputTitle?.requestFocus()
            }
            inputSubtitle?.text.toString().trim().isEmpty() -> {
                val error = context.getString(R.string.input_subtitle_empty)
                _createNoteStatus.postValue(Event(Resource.Error(error)))
                inputSubtitle?.requestFocus()
            }
            inputTitle?.text.toString().length < MIN_TITLENOTE_LENGTH -> {
                val error =
                    context.getString(R.string.error_titlenote_too_short , MIN_TITLENOTE_LENGTH)
                _createNoteStatus.postValue(Event(Resource.Error(error)))
                inputTitle?.requestFocus()
            }
            inputTitle?.text.toString().length > MAX_TITLENOTE_LENGTH -> {
                val error =
                    context.getString(R.string.error_titlenote_too_long , MAX_TITLENOTE_LENGTH)
                _createNoteStatus.postValue(Event(Resource.Error(error)))
                inputTitle?.requestFocus()
            }
            inputSubtitle?.text.toString().length < MIN_SUBTITLE_LENGTH -> {
                val error =
                    context.getString(R.string.error_subtille_too_short , MIN_SUBTITLE_LENGTH)
                _createNoteStatus.postValue(Event(Resource.Error(error)))
                inputSubtitle?.requestFocus()
            }
            inputSubtitle?.text.toString().length > MAX_SUBTITLE_LENGTH -> {
                val error =
                    context.getString(R.string.error_subtitle_too_long , MAX_SUBTITLE_LENGTH)
                _createNoteStatus.postValue(Event(Resource.Error(error)))
                inputSubtitle?.requestFocus()
            }
            input_note?.text.toString().trim().isEmpty() -> {
                val error =
                    context.getString(R.string.input_note_empty)
                _createNoteStatus.postValue(Event(Resource.Error(error)))
                inputSubtitle?.requestFocus()
            }
            else -> {
                Log.i("aly" , "saveNote: ${webLinkStatus.value?.peekContent()?.data}")
                _createNoteStatus.postValue(Event(Resource.Loading()))
                viewModelScope.launch(dispatcher) {
                    val note = Note(
                        title = inputTitle?.text.toString() ,
                        dateTime = timeStatus.value?.peekContent()?.data.toString() ,
                        subTitle = inputSubtitle?.text.toString() ,
                        color = colorIndicatorStatus.value?.peekContent()?.data.toString() ,
                        // imagePath = curImageUri.value?.peekContent()?.data.toString()
                        imagePath = curImageUri.value?.toString() ,
                        webLink = webLinkStatus.value?.peekContent()?.data,
                        note_text = input_note.text.toString()
                    )

                    val result = repository.insert(note)
                    _createNoteStatus.postValue(Event(result))
                }
            }
        }
    }

    fun updateNote(
        id:Int,
        inputTitle : EditText? ,
        inputSubtitle : EditText? ,
        input_note : EditText
    ) {
        when {
            inputTitle?.text.toString().trim().isEmpty() -> {
                val error = context.getString(R.string.input_title_empty)
                _updateNoteStatus.postValue(Event(Resource.Error(error)))
                inputTitle?.requestFocus()
            }
            inputSubtitle?.text.toString().trim().isEmpty() -> {
                val error = context.getString(R.string.input_subtitle_empty)
                _updateNoteStatus.postValue(Event(Resource.Error(error)))
                inputSubtitle?.requestFocus()
            }
            inputTitle?.text.toString().length < MIN_TITLENOTE_LENGTH -> {
                val error =
                    context.getString(R.string.error_titlenote_too_short , MIN_TITLENOTE_LENGTH)
                _updateNoteStatus.postValue(Event(Resource.Error(error)))
                inputTitle?.requestFocus()
            }
            inputTitle?.text.toString().length > MAX_TITLENOTE_LENGTH -> {
                val error =
                    context.getString(R.string.error_titlenote_too_long , MAX_TITLENOTE_LENGTH)
                _updateNoteStatus.postValue(Event(Resource.Error(error)))
                inputTitle?.requestFocus()
            }
            inputSubtitle?.text.toString().length < MIN_SUBTITLE_LENGTH -> {
                val error =
                    context.getString(R.string.error_subtille_too_short , MIN_SUBTITLE_LENGTH)
                _updateNoteStatus.postValue(Event(Resource.Error(error)))
                inputSubtitle?.requestFocus()
            }
            inputSubtitle?.text.toString().length > MAX_SUBTITLE_LENGTH -> {
                val error =
                    context.getString(R.string.error_subtitle_too_long , MAX_SUBTITLE_LENGTH)
                _updateNoteStatus.postValue(Event(Resource.Error(error)))
                inputSubtitle?.requestFocus()
            }
            input_note?.text.toString().trim().isEmpty() -> {
                val error =
                    context.getString(R.string.input_note_empty)
                _updateNoteStatus.postValue(Event(Resource.Error(error)))
                inputSubtitle?.requestFocus()
            }
            else -> {
                Log.i("aly" , "saveNote: ${webLinkStatus.value?.peekContent()?.data}")
                _updateNoteStatus.postValue(Event(Resource.Loading()))
                viewModelScope.launch(dispatcher) {
                    val note = Note(
                        title = inputTitle?.text.toString() ,
                        dateTime = timeStatus.value?.peekContent()?.data.toString() ,
                        subTitle = inputSubtitle?.text.toString() ,
                        color = colorIndicatorStatus.value?.peekContent()?.data.toString() ,
                        // imagePath = curImageUri.value?.peekContent()?.data.toString()
                        imagePath = curImageUri.value?.toString() ?: "" ,
                        webLink = webLinkStatus.value?.peekContent()?.data ?: "" ,
                        note_text = input_note.text.toString()
                    )

                    note.id=id

                    val result = repository.update(note)
                    _updateNoteStatus.postValue(Event(result))
                }
            }
        }
    }

    fun delete(note: Int) {
        _deleteNoteStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
        note?.let {
            val result =  repository.delete(it)
            _updateNoteStatus.postValue(Event(result))
        }

    }
    }

    init {
        getTimeAndData()
    }

    override fun onCleared() {
        super.onCleared()

    }




}