package myappnew.com.conserve.data

import androidx.room.*
import io.reactivex.Single
import myappnew.com.conserve.entiteis.Note

@Dao
interface NoteDao {

    @Query("select * from notes order by id desc")
    suspend fun getAllNote(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note : Note):Long

    @Delete
    suspend fun delete(note : Note):Int

    @Update
    suspend fun update(obj: Note):Int

    @Query("SELECT * FROM NOTES WHERE title LIKE :keyword OR subtitle LIKE :keyword OR note_text LIKE :keyword")
   suspend fun searchNotes(keyword :String): List<Note>
}