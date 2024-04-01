package com.example.diarybook.view.sheet

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diarybook.R
import com.example.diarybook.adapter.ColorAdapter
import com.example.diarybook.constant.Constant.BACKGROUND_DATA
import com.example.diarybook.constant.Constant.blackHexCode
import com.example.diarybook.constant.Constant.whiteHexCode
import com.example.diarybook.viewmodel.DiaryViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class ColorsSheet
    (private val context: Context, private val viewModel: DiaryViewModel) {



    private lateinit var colorsBottomSheet: BottomSheetDialog

    fun getColorsSheet(selectedTextColor : String,
                       selectedBackgroundColor : String,
                       colorClicked: (String) -> Unit) {

        colorsBottomSheet = BottomSheetDialog(
            context,
            R.style.BottomSheetDialogTheme
        )

        val bottomSheetView = LayoutInflater
            .from(context)
            .inflate(
                R.layout.bottom_sheet_color,
                null
            )


        var colors: Array<String> = arrayOf(
            "#FFFFFFFF", "#FF000000", "#757580",
            "#BF40BF", "#FFBB86FC", "#1DA1F2",
            "#F5820D", "#A90432",
            "#ffed00", "#00D683",
        )

        val fromBackground = viewModel.getBooleanData(BACKGROUND_DATA)

        if (fromBackground) {
            if (selectedTextColor != blackHexCode) {
                val updatedTextColors =
                    colors.filter { it != selectedTextColor }
                        .toTypedArray()
                colors = updatedTextColors
            } else {
                val updatedColors =
                    colors.filter { it != blackHexCode }
                        .toTypedArray()
                colors = updatedColors
            }
        } else {
            if (selectedBackgroundColor != whiteHexCode) {
                val updatedBackgroundColors =
                    colors.filter { it != selectedBackgroundColor }
                        .toTypedArray()
                colors = updatedBackgroundColors
            } else {
                val updatedColors =
                    colors.filter { it != whiteHexCode }
                        .toTypedArray()
                colors = updatedColors
            }
        }

        colorsBottomSheet?.setContentView(bottomSheetView)

        colorsBottomSheet?.show()

        val recyclerColorView = bottomSheetView.findViewById<RecyclerView>(R.id.recyclerColorView)

        recyclerColorView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )

        val colorAdapter = ColorAdapter(colors) { newColor ->
            colorClicked(newColor)
        }

        val backButton = bottomSheetView.findViewById<ImageView>(R.id.colorBackButton)


        backButton.setOnClickListener {
            colorsBottomSheet?.dismiss()
        }

        recyclerColorView.adapter = colorAdapter

    }

}