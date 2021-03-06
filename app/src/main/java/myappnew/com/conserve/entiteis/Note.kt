package myappnew.com.conserve.entiteis

import android.os.Parcel
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "notes")
class Note(
    @ColumnInfo(name = "title")
    val title:String,
    @ColumnInfo(name = "date_time")
    val dateTime:String,
    @ColumnInfo(name = "subtitle")
    val subTitle:String?,
    @ColumnInfo(name = "image_path")
    val imagePath:String?,
    @ColumnInfo(name = "note_text")
    var note_text: String,
    @ColumnInfo(name = "color")
    val color:String?,
    @ColumnInfo(name = "weblink")
    val webLink:String?
):Parcelable{
    @PrimaryKey(autoGenerate = true )
    var  id:Int?=null
    override fun toString() : String {
        return "Note(id=$id, title='$title', dateTime='$dateTime', subTitle='$subTitle', imagePath=$imagePath, color=$color, webLink=$webLink)"
    }

}