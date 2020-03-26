package com.absinthe.anywhere_.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.LinearLayout
import com.absinthe.anywhere_.services.OverlayService
import com.absinthe.anywhere_.utils.CommandUtils
import com.absinthe.anywhere_.utils.UiUtils
import com.absinthe.anywhere_.viewbuilder.entity.OverlayBuilder
import timber.log.Timber

class OverlayView(private val mContext: Context) : LinearLayout(mContext) {

    private lateinit var mBuilder: OverlayBuilder

    var command: String? = null
    private val mWindowManager: WindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mTouchSlop: Int = ViewConfiguration.get(mContext).scaledTouchSlop

    private var mPkgName: String? = null
    private var isClick = false
    private var mStartTime: Long = 0
    private var mEndTime: Long = 0

    private val removeWindowTask = Runnable {
        mBuilder.ivIcon.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        mContext.startService(
                Intent(mContext, OverlayService::class.java)
                        .putExtra(OverlayService.COMMAND, OverlayService.COMMAND_CLOSE)
        )
    }

    init {
        initView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        mBuilder = OverlayBuilder(mContext, this)

        mBuilder.ivIcon.setOnClickListener {
            Timber.d("Overlay window clicked!")
            CommandUtils.execCmd(command)
        }
        mBuilder.ivIcon.setOnTouchListener(object : OnTouchListener {
            private var lastX = 0f//上一次位置的X.Y坐标 = 0f
            private var lastY = 0f
            private var nowX = 0f //当前移动位置的X.Y坐标 = 0f
            private var nowY = 0f
            private var tranX = 0f //悬浮窗移动位置的相对值 = 0f
            private var tranY = 0f

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                val mLayoutParams = layoutParams as WindowManager.LayoutParams

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 获取按下时的X，Y坐标
                        lastX = motionEvent.rawX
                        lastY = motionEvent.rawY
                        Timber.d("MotionEvent.ACTION_DOWN last: %d %d", lastX, lastY)
                        isClick = false
                        mStartTime = System.currentTimeMillis()
                        postDelayed(removeWindowTask, 1000)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        isClick = true

                        // 获取移动时的X，Y坐标
                        nowX = motionEvent.rawX
                        nowY = motionEvent.rawY
                        Timber.d("MotionEvent.ACTION_MOVE now: %d %d", nowX, nowY)

                        // 计算XY坐标偏移量
                        tranX = nowX - lastX
                        tranY = nowY - lastY
                        Timber.d("MotionEvent.ACTION_MOVE tran: %d %d", tranX, tranY)
                        if (tranX * tranX + tranY * tranY > mTouchSlop * mTouchSlop) {
                            removeCallbacks(removeWindowTask)
                        }

                        // 移动悬浮窗
                        mLayoutParams.x.minus(tranX.toInt())
                        mLayoutParams.y.plus(tranY.toInt())
                        //更新悬浮窗位置
                        mWindowManager.updateViewLayout(this@OverlayView, mLayoutParams)
                        //记录当前坐标作为下一次计算的上一次移动的位置坐标
                        lastX = nowX
                        lastY = nowY
                    }
                    MotionEvent.ACTION_UP -> {
                        mEndTime = System.currentTimeMillis()
                        Timber.d("Touch period = %d", mEndTime - mStartTime)
                        isClick = mEndTime - mStartTime > 0.2 * 1000L
                        removeCallbacks(removeWindowTask)
                    }
                }
                return isClick
            }
        })
    }

    var pkgName: String?
        get() = mPkgName
        set(mPkgName) {
            this.mPkgName = mPkgName
            mBuilder.ivIcon.setImageDrawable(UiUtils.getAppIconByPackageName(mContext, mPkgName))
        }
}