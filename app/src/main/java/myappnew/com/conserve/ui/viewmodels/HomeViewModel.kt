package myappnew.com.conserve.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import myappnew.com.conserve.entiteis.Note
import myappnew.com.conserve.helper.Event
import myappnew.com.conserve.helper.Resource
import myappnew.com.conserve.repositories.DefaultHomeRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject constructor(
        private val repository : DefaultHomeRepository,
        private val dispatcher : CoroutineDispatcher=Dispatchers.Main
    ): ViewModel() {

    private val _listNoteStatus= MutableLiveData<Event<Resource<List<Note>>>>()
    val listNoteStatus: LiveData<Event<Resource<List<Note>>>> =_listNoteStatus

    private val _deleteNoteStatus= MutableLiveData<Event<Resource<Int>>>()
    val deleteNoteStatus: LiveData<Event<Resource<Int>>> =_deleteNoteStatus

    private val _searchResults = MutableLiveData<Event<Resource<List<Note>>>>()
    val searchResults: LiveData<Event<Resource<List<Note>>>> = _searchResults

    fun searchUser(query: String) {
        if(query.isEmpty()) return

        _searchResults.postValue(Event(Resource.Loading()))
        viewModelScope.launch(dispatcher) {
            val result = repository.searchNote(query)

            _searchResults.postValue(Event(result))
        }
    }
     fun getAllNotes(){
        _listNoteStatus.postValue(Event(Resource.Loading()))

        viewModelScope.launch(dispatcher) {
            val result=repository.getAllNote()
            _listNoteStatus.postValue(Event(result))
        }
    }

    fun delete(note : Int) {
        _listNoteStatus.postValue(Event(Resource.Loading()))

        viewModelScope.launch(dispatcher) {
            val result=repository.delete(note)
            _deleteNoteStatus.postValue(Event(result))
        }

    }

    init {
        getAllNotes()
    }

}