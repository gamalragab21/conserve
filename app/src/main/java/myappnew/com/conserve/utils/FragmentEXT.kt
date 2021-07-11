package myappnew.com.conserve.utils

import android.app.Activity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.snackbar(message:String){
Snackbar.make(
    requireView(),
    message,
    Snackbar.LENGTH_LONG
).show()

}

