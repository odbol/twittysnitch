package com.odbol.twittysnitch

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.*

import androidx.core.content.ContextCompat.getSystemService

import android.view.WindowManager

import android.view.Gravity

import android.os.Build
import android.view.Display
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
import android.view.WindowManager.LayoutParams.MATCH_PARENT
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

import android.view.Display.DEFAULT_DISPLAY

import android.hardware.display.DisplayManager





class Highlighter(private val context: Context) {

    private val highlights: MutableList<Rect> = mutableListOf()
    private var view: View? = null

    fun onConnected() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.RIGHT or Gravity.TOP
        params.width = MATCH_PARENT
        params.height = MATCH_PARENT

        val dm: DisplayManager = context.getSystemService(DisplayManager::class.java)
        val primaryDisplay = dm.getDisplay(DEFAULT_DISPLAY)
        val windowContext = if (Build.VERSION.SDK_INT >= 30) {
            context.createDisplayContext(primaryDisplay)
                .createWindowContext(TYPE_APPLICATION_OVERLAY, null)
        } else {
            context.createDisplayContext(primaryDisplay)
        }

        view = HighlighterView(windowContext)

        val wm = windowContext.getSystemService(WINDOW_SERVICE) as WindowManager?
        wm!!.addView(view, params)
    }

    fun addHighlight(highlightRect: Rect) {
        highlights.add(Rect(highlightRect))
        view?.postInvalidate()
    }

    fun clearHighlights() {
        highlights.clear()
        view?.postInvalidate()
    }

    inner class HighlighterView(context: Context) : View(context) {

        private val paint: Paint = Paint().apply {
            color = Color.GREEN
            alpha = 100
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            highlights.forEach { canvas.drawRect(it, paint) }
        }
    }
}