package myappnew.com.conserve.utils

import com.google.android.material.snackbar.Snackbar
import myappnew.com.conserve.helper.Resource


inline fun <T> safeCall(action: () -> Resource<T>): Resource<T> {
    return try {
        action()
    } catch(e: Exception) {
        Resource.Error(e.message ?: "An unknown error occurred")
    }
}


