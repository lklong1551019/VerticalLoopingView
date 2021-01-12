package com.example.verticalloopingview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.verticalloopingview.databinding.ItemCommentBinding

class ItemCommentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: ItemCommentBinding =
        ItemCommentBinding.inflate(LayoutInflater.from(context), this, true)

    private var comment: String? = null

    fun setData(s: String?) {
        comment = s
        binding.tvComment.text = s
    }

    fun getData(): String? {
        return comment
    }
}