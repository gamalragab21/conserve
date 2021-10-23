package myappnew.com.conserve.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import myappnew.com.conserve.entiteis.Note

@Database(entities = [Note::class] , version = 2, exportSchema = false)
public  abstract class NoteDataBase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: NoteDataBase? = null

        fun getDatabase(context: Context): NoteDataBase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDataBase::class.java,
                    "Note_Database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

}