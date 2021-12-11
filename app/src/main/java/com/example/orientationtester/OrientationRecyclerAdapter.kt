package com.example.orientationtester

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class OrientationRecyclerAdapter(private val viewModel: OrientationViewModel): RecyclerView.Adapter<OrientationRecyclerAdapter.OrientationHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrientationRecyclerAdapter.OrientationHolder {
        val inflatedView = parent.inflate(R.layout.orientation_item_row, false)
        return OrientationHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: OrientationHolder, position: Int) {
        val item = viewModel.orientationList.value!![position]
        holder.bind(position, item)
    }

    override fun getItemCount() = viewModel.orientationList.value?.size ?: 0

    class OrientationHolder(v: View): RecyclerView.ViewHolder(v),
            View.OnClickListener
    {
        private var view: View = v
        private var position: Int? = null
        private var orientation: ArrayList<Double>? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            // TODO
        }

        fun bind(position: Int, orientation: ArrayList<Double>) {
            // var txt = "$position : (${orientation[0]}, ${orientation[1]}, ${orientation[2]})"
            var txt = String.format("%d : %+.4f, %+.4f, %+.4f", position, orientation[1], orientation[2], orientation[3])

            view.findViewById<TextView>(R.id.positionTextView).text = position.toString()
            view.findViewById<TextView>(R.id.pitchTextView).text    = String.format("%+.4f", orientation[1])
            view.findViewById<TextView>(R.id.rollTextView).text     = String.format("%+.4f", orientation[2])
            view.findViewById<TextView>(R.id.yawTextView).text      = String.format("%+.4f", orientation[3])

            this.position = position
            this.orientation = orientation
        }


    }

}