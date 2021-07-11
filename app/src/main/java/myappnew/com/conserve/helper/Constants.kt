package myappnew.com.conserve.helper

import android.content.Context
import java.util.*

object Constants {
    const val  DEFAULT_PROFILE_PICTURE_URL:String="https://firebasestorage.googleapis.com/v0/b/snplc-1d8bb.appspot.com/o/img_avatar.png?alt=media&token=fd385dce-df17-4b0d-936e-2b8fd356e215"


    const val MIN_TITLENOTE_LENGTH=3
    const val MAX_TITLENOTE_LENGTH=50

    const val MIN_SUBTITLE_LENGTH=3
    const val MAX_SUBTITLE_LENGTH=40
    const val SEARCH_TIME_DELAY= 500L



    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS


    fun getTimeAgo(time : Long , ctx : Context?) : String? {
        var time = time
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }
        val now = Date().time
        if (time > now || time <= 0) {
            return null
        }
        val diff = now - time
        return if (diff < MINUTE_MILLIS) {
            "just now"
        } else if (diff < 2 * MINUTE_MILLIS) {
            "a minute ago"
        } else if (diff < 50 * MINUTE_MILLIS) {
            "${diff / MINUTE_MILLIS } minutes ago"
        } else if (diff < 90 * MINUTE_MILLIS) {
            "an hour ago"
        } else if (diff < 24 * HOUR_MILLIS) {
           " ${diff / HOUR_MILLIS}  hours ago"
        } else if (diff < 48 * HOUR_MILLIS) {
            "yesterday"
        } else {
           "  ${diff / DAY_MILLIS} days ago"
        }
    }

}