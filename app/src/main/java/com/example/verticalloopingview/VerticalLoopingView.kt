package com.example.verticalloopingview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

private const val ANIMATION_DURATION: Long = 500
private const val DELAY_DURATION: Long = 1000

abstract class VerticalLoopingView : ViewGroup {

    private var totalHeight = 0
    private var offset = 0
    private var position = 0
    private var dataSize = 0

    private var valueAnimator: ValueAnimator? = null
    private val runnable : Runnable = run {
        Runnable {
                valueAnimator = ValueAnimator.ofInt(0, totalHeight).apply {
                    duration = ANIMATION_DURATION
                    addUpdateListener { animation: ValueAnimator ->
                        offset = animation.animatedValue as Int
                        requestLayout()
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            //  At this moment, the "next" is the visible one, and "main" is at top
                            //      (invisible). We set "main" to the "next" and reset offset, so the "main" will be at the visible position
                            // This is feasible due to the LAST layout-pass triggered by the last call to onAnimationUpdate()

                            // (1) : widget 1
                            // (2) : widget 2

                            // Current anim starts:
                            // |       |
                            // |   a   | (visible) (1)
                            // |   b   | (invisible) (2)

                            // Current anim nearly ends:
                            // |   a   | (invisible) (1)
                            // |   b   | (visible) (2)
                            // |       |

                            // Last step of looping called:
                            // |   b   | (invisible) (1)
                            // |   b   | (visible) (2)
                            // |       | (invisible)

                            // Current anim done (Last layout-pass triggered):
                            // |       |
                            // |   b   | (visible) (1)
                            // |   c   | (invisible) (2)
                            lastStepOfLooping()
                            setDataNextWidget(getNextPosition())
                            offset = 0
                            updateHandler.postDelayed(runnable, DELAY_DURATION)
                        }
                    })
                    start()
                }
        }
    }
    private var updateHandler: Handler
    private var mainView: View
    private var nextView: View

    private var spacingSmall = 0

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    @Suppress("LeakingThis")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        spacingSmall = resources.getDimensionPixelSize(R.dimen.spacing_small)

        updateHandler = Handler()

        mainView = getMainWidget(context, attrs, defStyleAttr)
        addView(mainView)
        nextView = getNextWidget(context, attrs, defStyleAttr)
        addView(nextView)

        isVerticalFadingEdgeEnabled = true
        setFadingEdgeLength(20)
        setWillNotDraw(false)
    }

    protected abstract fun getMainWidget(context: Context, attrs: AttributeSet?, defStyleAttr: Int): View

    protected abstract fun getNextWidget(context: Context, attrs: AttributeSet?, defStyleAttr: Int): View

    protected abstract fun setDataMainWidget(position: Int)

    protected abstract fun setDataNextWidget(position: Int)

    /**
     * Make the [.mMainView] has EXACTLY the same content with [.mNextView]
     *
     *
     *
     * **Note** Careful with cases like ellipsized text (depends on the implementation of how to ellipsize text).
     *
     *
     * For example, if we only ellipsize text in onDraw(), then we have to use the already ellipsized
     * text of [.mNextView] to set to the [.mMainView], or else we will see a not-ellipsize text
     * of [.mMainView] in a blink of an eye
     */
    protected abstract fun lastStepOfLooping()

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val availableWidth = width - getSpacingLeftOrRight() * 2
        mainView.measure(
            MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        nextView.measure(
            MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(mainView.measuredHeight, MeasureSpec.EXACTLY)
        )
        totalHeight = mainView.measuredHeight + getSpacingTopOrBottom()
        setMeasuredDimension(width, mainView.measuredHeight + getSpacingTopOrBottom() * 2)
    }

    protected open fun getSpacingLeftOrRight(): Int {
        return 0
    }

    protected open fun getSpacingTopOrBottom(): Int {
        return spacingSmall
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val left = getSpacingLeftOrRight()
        var top = (measuredHeight - mainView.measuredHeight) / 2 - offset
        mainView.layout(left, top, left + mainView.measuredWidth, top + mainView.measuredHeight)
        top = measuredHeight - offset
        nextView.layout(
            left, top, left + nextView.measuredWidth,
            top + nextView.measuredHeight
        )
    }

    override fun getTopFadingEdgeStrength(): Float {
        // Read the source code, return 1f to always have fading edge
        return 1f
    }

    override fun getBottomFadingEdgeStrength(): Float {
        // Read the source code, return 1f to always have fading edge
        return 1f
    }

    protected open fun onReceivedData(size: Int) {
        stop()
        position = 0
        dataSize = size
    }

    /**
     * **Note: ** Must be call after [.onReceivedData] and has set data
     */
    protected fun start(startPos: Int) {
        position = if (startPos > dataSize || startPos < 0) 0 else startPos
        setDataMainWidget(position)
        if (dataSize > 1) {
            setDataNextWidget(getNextPosition())
            updateHandler.postDelayed(runnable, DELAY_DURATION)
        }
    }

    protected open fun stop() {
        valueAnimator?.end()
        updateHandler.removeCallbacksAndMessages(null)
    }

    open fun getCurrentPosition(): Int {
        return position
    }

    protected open fun getNextPosition(): Int {
        ++position
        if (position >= dataSize) {
            // Loop again
            position = 0
        }
        return position
    }
}