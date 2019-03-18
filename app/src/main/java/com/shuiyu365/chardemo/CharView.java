package com.shuiyu365.chardemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharView extends View {

    private int width;
    private int heigh;
    //网格的宽度与高度
    private float gridspace_width;
    private float gridspace_heigh;
    //底部空白的高度
    private float brokenline_bottom;
    private float brokenline_top;

    private float initialX;


    //开始的x宽度
    private float startWidthX;

    //尾部预留位置
    private float endWidthX;

    //横线画笔
    private Paint mPaint_xian;

    //Y轴文本数据的画笔
    private Paint yText_mPaint;
    //路径
    private Path mpath;
    //折线画笔
    private Paint linePaint;

    //数值画笔
    private Paint textPaint;


    private List<Float> verticalList = new ArrayList<>();
    /**
     * X轴
     */
    private Rect mBound;
    private List<String> horizontalList = new ArrayList<>();

    /**
     * 折线区域渐变
     */
    private int[] GRADIENT_COLORS = {Color.parseColor("#66FFFFFF"),Color.parseColor("#66A989F9")};

    /**
     * 柱状渐变
     */
    private int[] GRADIENT_COLORS1 = {Color.parseColor("#AC85FA"),Color.parseColor("#8BABED")};


    public CharView(Context context) {
        super(context);
        init(context);
    }

    public CharView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CharView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        /**
         * 初始化控件
         */
        init(context);
    }

    private void init(Context context) {
        mBound = new Rect();


        //线画笔
        mPaint_xian = new Paint();
        mPaint_xian.setStyle(Paint.Style.STROKE);
        mPaint_xian.setAntiAlias(true);
        mPaint_xian.setColor(0xffe0e0e0);
        mPaint_xian.setPathEffect(new DashPathEffect(new float[] {5, 5}, 0));
        setLayerType(LAYER_TYPE_SOFTWARE, null);



        textPaint = new TextPaint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(20f);
        textPaint.setColor(0xffffffff);

        //Y轴画笔
        yText_mPaint  = new TextPaint();
        yText_mPaint.setTextAlign(Paint.Align.CENTER);
        yText_mPaint.setAntiAlias(true);
        yText_mPaint.setTextSize(28f);
        yText_mPaint.setColor(0xff999999);

        //线画笔
        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setAntiAlias(true);



        mpath = new Path();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //宽度的模式
        int mWidthModle = MeasureSpec.getMode(widthMeasureSpec);
        //宽度大小
        int mWidthSize = MeasureSpec.getSize(widthMeasureSpec);

        int mHeightModle = MeasureSpec.getMode(heightMeasureSpec);
        int mHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        //如果明确大小,直接设置大小
        if (mWidthModle == MeasureSpec.EXACTLY) {
            width = mWidthSize;
        } else {
            //计算宽度,可以根据实际情况进行计算
            width = (getPaddingLeft() + getPaddingRight());
            //如果为AT_MOST, 不允许超过默认宽度的大小
            if (mWidthModle == MeasureSpec.AT_MOST) {
                width = Math.min(width, mWidthSize);
            }
        }
        if (mHeightModle == MeasureSpec.EXACTLY) {
            heigh = mHeightSize;
        } else {
            heigh = (getPaddingTop() + getPaddingBottom());
            if (mHeightModle == MeasureSpec.AT_MOST) {
                heigh = Math.min(heigh, mHeightSize);
            }
        }
        //设置测量完成的宽高
        setMeasuredDimension(width, heigh);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        heigh = getHeight();

        brokenline_top = 30;
        brokenline_bottom = 100f;

        /**
         * 横线之间的间隔
         */
        gridspace_heigh = (heigh - brokenline_top - brokenline_bottom) / 6f;

        initialX = 100f;

        startWidthX = initialX;
        endWidthX = initialX;
        /**
         * Y轴数据之间的间隔
         */
        gridspace_width = (width - 2 * startWidthX)/6f;


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 划横线
         */
        canvasLine(canvas);

        //Y轴数据
        canvasLineY(canvas);
        //画横坐标
        float textTempStart = startWidthX;
        //X轴数据
        canvasLineX(canvas, textTempStart);

        float size = (heigh - brokenline_bottom - brokenline_top) / 120f; //比例

        //画折线和柱子
        drawBar(canvas, textTempStart, size);

    }

    /**
     *         画折线和柱子
     * @param canvas
     * @param textTempStart 起始位置
     * @param size          每1份的高度
     */
    private void drawBar(Canvas canvas, float textTempStart, float size) {
        //canvas.clipRect(textTempStart, brokenline_top, width, heigh - brokenline_bottom, Region.Op.REPLACE);
        List<Integer> numList = new ArrayList<>();
        List<Float> effectiveNumList = new ArrayList<>();
        float maxNum = 0;
        if(verticalList.size() != 0){
            maxNum = verticalList.get(0);
        }
        for(int i = 0; i < verticalList.size();i++){
            if(maxNum < verticalList.get(i)){
                maxNum = verticalList.get(i);
            }
            if(verticalList.get(i) != 0){
                numList.add(i);
                effectiveNumList.add(verticalList.get(i));
            }
        }


        if(numList.size() >= 1){
            int num = 0;
            mpath.reset();
            //第一个数在X轴上
            mpath.moveTo(numList.get(num) * gridspace_width + textTempStart,heigh - brokenline_bottom);
            for(int j = 0;j < numList.size();j++){


                mpath.lineTo(numList.get(j) * gridspace_width + textTempStart,heigh - effectiveNumList.get(j)*size- brokenline_bottom);
            }
            mpath.lineTo(numList.get(numList.size() - 1) * gridspace_width + textTempStart,heigh - brokenline_bottom);
            mpath.close();//封闭形状
                    //创建线性颜色渐变器
        LinearGradient shader = new LinearGradient(numList.get(num) * gridspace_width + textTempStart,heigh - brokenline_bottom
                ,numList.get(num) * gridspace_width + textTempStart,heigh - maxNum * size- brokenline_bottom,GRADIENT_COLORS, null, Shader.TileMode.MIRROR);
        linePaint.setShader(shader);//第三层矩形颜色(进度渐变色)
            canvas.drawPath(mpath, linePaint);

            Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.report_icon);
            Bitmap mBitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.recordthereport_icon);
            //画柱子
            for(int j = 0;j < numList.size();j++){
                RectF rectF = new RectF();
                rectF.left = numList.get(j) * gridspace_width + textTempStart - 2f;
                rectF.top = heigh - effectiveNumList.get(j) * size- brokenline_bottom;
                rectF.right = numList.get(j) * gridspace_width + textTempStart + 2f;
                rectF.bottom = heigh - brokenline_bottom;
                LinearGradient shader1 = new LinearGradient(numList.get(j) * gridspace_width + textTempStart,heigh - brokenline_bottom
                        ,numList.get(j) * gridspace_width + textTempStart,heigh - effectiveNumList.get(j) * size- brokenline_bottom,GRADIENT_COLORS1, null, Shader.TileMode.MIRROR);
                linePaint.setShader(shader1);//第三层矩形颜色(进度渐变色)
                canvas.drawRoundRect(rectF,4,4, linePaint);

                //原点
                canvas.drawBitmap(mBitmap, numList.get(j) * gridspace_width + textTempStart- mBitmap.getWidth()/2, heigh - effectiveNumList.get(j)*size- brokenline_bottom - mBitmap.getHeight()/2, linePaint);


                //文字
                canvas.drawBitmap(mBitmap1, numList.get(j) * gridspace_width + textTempStart - mBitmap1.getWidth()/2, heigh - effectiveNumList.get(j)*size- brokenline_bottom - mBitmap1.getHeight() - mBitmap.getHeight()/2, linePaint);



                canvas.drawText(effectiveNumList.get(j)+"kg",numList.get(j) * gridspace_width + textTempStart,heigh - effectiveNumList.get(j)*size- brokenline_bottom - mBitmap1.getHeight()/2 -  mBitmap.getHeight()/2,textPaint);
            }


        }
    }

    /**
     *  //X轴数据
     * @param canvas
     * @param textTempStart
     */
    private void canvasLineX(Canvas canvas, float textTempStart) {

        for (int i = 0; i < horizontalList.size(); i++) {
            //yText_mPaint.getTextBounds(horizontalList.get(i), 0, horizontalList.get(i).length(), mBound);
            canvas.drawText( horizontalList.get(i), textTempStart, heigh -50f, yText_mPaint);

            textTempStart += gridspace_width;
        }
    }

    /**
     * 画Y轴
     * @param canvas
     */
    private void canvasLineY(Canvas canvas) {
        for(int i = 6; i >= 0; i--){
            canvas.drawText(20*i+"",initialX/2 - 10f,(6-i)*gridspace_heigh + brokenline_top + 12f,yText_mPaint);

        }
    }

    /**
     * 画横线
     * @param canvas
     */
    private void canvasLine(Canvas canvas) {
        for(int i = 0; i <= 6; i++){
            canvas.drawLine(initialX,gridspace_heigh*i + brokenline_top,width,gridspace_heigh*i + brokenline_top,mPaint_xian);
        }
    }



    /**
     * 重新指定起始位置
     *
     * @param verticalList
     */
//    private void measureWidthShort(List<Float> verticalList) {
//        startChart = outSpace;
//        textStart = startChart + barWidth / 2f;
//    }


    /**
     * 设置纵轴数据
     *
     * @param verticalList
     */
    public void setVerticalList(List<Float> verticalList) {

        if (verticalList != null) {
            this.verticalList = verticalList;

        } else {

            invalidate();
            return;
        }


        //measureWidthShort(verticalList);



        //mAnimator.animateY(mDuriation);
    }


    /**
     * 设置横轴数据
     *
     * @param horizontalList
     */
    public void setHorizontalList(List<String> horizontalList) {
        if (horizontalList != null)
            this.horizontalList = horizontalList;
            invalidate();
    }


    /**
     * 简单做滑动处理
     */
    int lastX;
    int offsetX;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取当前输入点的X、Y坐标（视图坐标）
        int x = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //处理按下事件
                // 记录触摸点坐标
                lastX = x;
                Log.e("aaaaaaa","MotionEvent.ACTION_DOWN" + lastX);
                break;
            case MotionEvent.ACTION_MOVE:
                //处理移动事件
                offsetX = x - lastX;
                Log.e("aaaaaaa","MotionEvent.ACTION_MOVE" + offsetX);


                if(verticalList.size() > 7) {
                    float moveMax =  (verticalList.size() - 1) * gridspace_width;

                    if (offsetX < 0) {
                        //startWidthX = (startWidthX + offsetX);
                        startWidthX = (startWidthX + offsetX) > gridspace_width * 7 - moveMax  ? (startWidthX + offsetX) : gridspace_width * 7 - moveMax;
                    } else {
                        //startWidthX = (startWidthX + offsetX);
                        startWidthX = (startWidthX + offsetX) < initialX ? (startWidthX + offsetX) : initialX;
                    }

                    invalidate();
                }
                //重新设置初始化坐标
                lastX = x;
                break;
            case MotionEvent.ACTION_UP:
                //处理离开事件
                Log.e("aaaaaaa","MotionEvent.ACTION_UP" + offsetX);


                break;
        }
        return true;
    }
}
