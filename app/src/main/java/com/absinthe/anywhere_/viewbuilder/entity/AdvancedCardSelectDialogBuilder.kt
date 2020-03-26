package com.absinthe.anywhere_.viewbuilder.entity

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.absinthe.anywhere_.R
import com.absinthe.anywhere_.viewbuilder.ViewBuilder

class AdvancedCardSelectDialogBuilder(context: Context) : ViewBuilder(context) {

    var tvAddImage: TextView
    var tvAddShell: TextView

    init {
        root = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
        }
        tvAddImage = addItem(R.string.btn_add_image, R.drawable.ic_photo)
        tvAddShell = addItem(R.string.btn_add_shell, R.drawable.ic_code)
    }

    private fun addItem(titleRes: Int, iconRes: Int): TextView {
        val itemView = TextView(mContext).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f
            }
            textSize = 15f
            gravity = Gravity.CENTER
            compoundDrawablePadding = 5.dp
            isClickable = true

            setPadding(15.dp, 15.dp, 15.dp, 15.dp)
            setText(titleRes)
            setTextColor(ContextCompat.getColor(mContext, R.color.textColorNormal))
            setCompoundDrawablesRelativeWithIntrinsicBounds(0, iconRes, 0, 0)
            setTypeface(null, Typeface.BOLD)

            val outValue = TypedValue()
            mContext.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }

        addView(itemView)
        return itemView
    }
}