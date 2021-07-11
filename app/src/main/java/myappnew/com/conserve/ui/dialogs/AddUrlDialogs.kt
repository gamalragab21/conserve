package myappnew.com.conserve.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.RequestManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_dialog_add_url.*
import myappnew.com.conserve.R
import myappnew.com.conserve.helper.Resource
import myappnew.com.conserve.ui.viewmodels.CreateNoteViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AddUrlDialogs : DialogFragment() {


    val createNoteViewModel : CreateNoteViewModel by activityViewModels()
    private lateinit var dialogView : View

    private var addUrlListener: ((String,Dialog?) -> Unit)? = null

    fun setPositiveAddUrlListener(listener: (String,Dialog?) -> Unit) {
        addUrlListener = listener
    }
    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View {
        return dialogView
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog {
        dialogView = LayoutInflater.from(requireContext()).inflate(
            R.layout.layout_dialog_add_url ,
            null
        )
        return MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        btAdd.setOnClickListener {
            addUrlListener?.let { click ->
                click(input_url.text.toString(),dialog)
            }
        }

        btCancel.setOnClickListener {
            dismiss()
        }


    }

}