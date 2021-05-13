package com.siddharthsinghbaghel.canvakids

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View


class DrawingView(context: Context, attrs: AttributeSet): View(context,attrs) {

    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPath = ArrayList<CustomPath>()
    internal val mRedoPath = ArrayList<CustomPath>()
    private var mRedoIndex: Int = -1

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
        draw(canvas)

    }

    fun onClickUndo(){

        if(mPaths.size > 0){
            mRedoPath.add(mPaths[mPaths.size -1])
            mUndoPath.add(mPaths.removeAt(mPaths.size-1))
            invalidate() // Invalidate will allow us to draw again and redirect us to onDraw() with mpaths.size =- 1
        }
    }

    fun onClickRedo(){

        mRedoIndex = mRedoPath.size - 1
        if(mRedoPath.size > 0){

            mPaths.add(mRedoPath[mRedoIndex])
            mRedoPath.remove(mRedoPath[mRedoIndex])
            invalidate() // Invalidate will allow us to draw again and redirect us to onDraw() with mpaths.size =- 1
        }
        else{
            println("No redo")
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f, mCanvasPaint)

        for(path in mPaths){
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path,mDrawPaint!!)
        }

        if(!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX,touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE ->{
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX,touchY)
                    }
                }
              }
            MotionEvent.ACTION_UP ->{
                mPaths.add(mDrawPath!!)
                mRedoPath.clear()
                mDrawPath = CustomPath(color,mBrushSize)
                    }
            else -> return false

        }
        invalidate()
        return true
    }

    fun setSizeForBrush(newSize: Float){
        //to maintain the thickness of brush in every device similar
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize,resources.displayMetrics)

        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color

    }

    internal class CustomPath(var color: Int, var brushThickness: Float) : Path()

}