package myappnew.com.conserve.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myappnew.com.conserve.data.NoteDao
import myappnew.com.conserve.entiteis.Note
import myappnew.com.conserve.helper.Resource
import myappnew.com.conserve.utils.safeCall
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class DefaultHomeRepository @Inject constructor(
    private val noteDao : NoteDao,
   private var simpleDateFormat : SimpleDateFormat,
   private var date: Date
) {


    suspend fun getAllNote():Resource<List<Note>> = withContext(Dispatchers.IO) {
        safeCall {
            val allNotes = noteDao.getAllNote()
            Resource.Success(allNotes)
        }
    }

    suspend fun insert(note : Note):Resource<Long> = withContext(Dispatchers.IO){
        safeCall {
            val noteInserted:Long=noteDao.insertNote(note)
          Resource.Success(noteInserted)
        }
    }

    suspend fun delete(id: Int):Resource<Int> = withContext(Dispatchers.IO) {
        safeCall {
            val noteDeleted:Int=noteDao.delete(id)
          Resource.Success(noteDeleted)
        }
    }
    suspend fun getTimeAndData():Resource<String> = withContext(Dispatchers.IO) {
        safeCall {
            Resource.Success(simpleDateFormat.format(date))
        }
    }
    suspend fun setColorIndicator(color:String):Resource<String> = withContext(Dispatchers.IO) {
        safeCall {
            Resource.Success(color)
        }
    }

    suspend fun update(note : Note) : Resource<Int> = withContext(Dispatchers.IO) {
        safeCall {
            val noteUpdate:Int=noteDao.update(note)
            Resource.Success(noteUpdate)
        }
    }

    suspend fun searchNote(query : String) : Resource<List<Note>> = withContext(Dispatchers.IO){
        safeCall {
            val result=noteDao.searchNotes(query)
            Resource.Success(result)
        }
    }
}