package com.example.orientationtester

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope


class CalibrateDialog (val viewModel: OrientationViewModel) : DialogFragment()  {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner);
        return inflater.inflate(R.layout.calibrate_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.4).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        //this.viewModel.pitch.observe(this, Observer { angle ->
        //    view?.findViewById<TextView>(R.id.tv_login)?.text = angle.toString()
        //})

        val accuracyType = {
                accuracy: Int ->
            when (accuracy) {
                0 -> "unreliable"
                1 -> "low"
                2 -> "medium"
                3 -> "high"
                else -> "unreliable"
            }
        }

        this.viewModel.geomagneticAccuracy.observe(this, Observer { accuracy ->
            view?.findViewById<TextView>(R.id.geomagneticAcc)?.text = accuracyType(accuracy)
        })

        this.viewModel.gravityAccuracy.observe(this, Observer { accuracy ->
            view?.findViewById<TextView>(R.id.gravityAcc)?.text = accuracyType(accuracy)
        })

    }
}