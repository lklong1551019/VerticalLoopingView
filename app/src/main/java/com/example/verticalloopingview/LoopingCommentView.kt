package com.example.verticalloopingview

import android.content.Context
import android.util.AttributeSet
import android.view.View

class LoopingCommentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : VerticalLoopingView(context, attrs, defStyleAttr) {

    private lateinit var mainView: ItemCommentView
    private lateinit var nextView: ItemCommentView

    private var mData: List<String>? = null

    override fun getMainWidget(context: Context, attrs: AttributeSet?, defStyleAttr: Int): View {
        return ItemCommentView(context, attrs, defStyleAttr).also {
            mainView = it
        }
    }

    override fun getNextWidget(context: Context, attrs: AttributeSet?, defStyleAttr: Int): View {
        return ItemCommentView(context, attrs, defStyleAttr).also {
            nextView = it
        }
    }

    override fun setDataMainWidget(position: Int) {
        mData?.let {
            mainView.setData(it[position])
        }
    }

    override fun setDataNextWidget(position: Int) {
        mData?.let {
            nextView.setData(it[position])
        }
    }

    override fun lastStepOfLooping() {
        mainView.setData(nextView.getData())
    }

    fun setData(data: List<String>?, startPos: Int) {
        if (data == null) return

        onReceivedData(data.size)
        mData = data
        start(startPos)
    }
}