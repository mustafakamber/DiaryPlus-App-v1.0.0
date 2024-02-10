package com.example.diarybook.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.diarybook.databinding.RecyclerColorRowBinding
import kotlinx.android.synthetic.main.recycler_color_row.view.*


class ColorAdapter(
    val colors: Array<String>,
    val onColorSelected: (String) -> Unit
) :
    RecyclerView.Adapter<ColorAdapter.ColorHolder>() {


    class ColorHolder(val binding: RecyclerColorRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(colors: Array<String>, position: Int) {
            with(itemView) {
                color_image.setImageDrawable(
                    Color.parseColor(colors[position]).toDrawable()
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorHolder {

        val colorAdapterBinding = RecyclerColorRowBinding
            .inflate(
                LayoutInflater.from(parent.context), parent,
                false
            )

        return ColorHolder(colorAdapterBinding)
    }


    override fun getItemCount(): Int {

        return colors.size
    }

    override fun onBindViewHolder(holder: ColorHolder, position: Int) {

        holder.bind(colors, position)
        holder.itemView.setOnClickListener {
            val selectedColor = colors[position]
            onColorSelected(selectedColor)
        }

    }

}


