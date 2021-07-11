package myappnew.com.conserve.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped
import dagger.hilt.android.scopes.ViewModelScoped
import myappnew.com.conserve.data.NoteDao
import myappnew.com.conserve.data.NoteDataBase
import myappnew.com.conserve.repositories.DefaultHomeRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton


@Module
@InstallIn(ViewModelComponent::class)
object RoomModel {

    @Provides
    @ViewModelScoped
    fun provideNoteDao(appDatabase: NoteDataBase): NoteDao {
        return appDatabase.noteDao()
    }

//    @Provides
//    @ViewModelScoped
//    fun provideAppDatabase(@ApplicationContext appContext: Context): NoteDataBase {
//        return Room.databaseBuilder(
//            appContext,
//            NoteDataBase::class.java,
//            "note_DB"
//        ).build()
//    }

//    @Provides
//    @ViewModelScoped
//    fun provideDefaultHomeRepository(dao:NoteDao): DefaultHomeRepository {
//        return DefaultHomeRepository(dao)
//    }


}