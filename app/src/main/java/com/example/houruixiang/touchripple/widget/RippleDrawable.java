package com.example.houruixiang.touchripple.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Interpolator;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.security.PrivilegedExceptionAction;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by houruixiang on 2017/7/18.
 */

public class RippleDrawable extends Drawable {

    private Paint mPaint;
    private Bitmap bitmap;
    private int rippleColor;
    private float mRipplePointX = 0;
    private float mRipplePointY = 0;
    private float mRippleRadius = 0;
    private int mAlpha = 200;

    private float mCenterPointX,mCenterPointY;
    private float mClickPointX;
    private float mClickPointY;
    //最大半径
    private float MaxRadius;
    //开始半径
    private float startRadius;
    //结束半径
    private float endRadius;

    //记录是否抬起手--->boolean
    private boolean mUpDone;
    //记录进入动画是否完毕
    private boolean mEnterDone;


    /**进入动画*/
    //进入动画的进度值
    private float mProgress;
    //每次递增的时间
    private float mEnterIncrement = 16f/360;
    //进入动画添加插值器
    private DecelerateInterpolator mEnterInterpolator = new DecelerateInterpolator(2);
    private Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mEnterDone = false;
                mCircleAlpha = 255;
                mProgress = mProgress + mEnterIncrement;
                if (mProgress > 1){
                    onEnterPrograss(1);
                    enterDone();
                    return;
                }

                float interpolation = mEnterInterpolator.getInterpolation(mProgress);
                onEnterPrograss(interpolation);
                scheduleSelf(this, SystemClock.uptimeMillis() + 16);
            }


    };

    /**进入动画刷新的方法
     * @parms realProgress */
    public void onEnterPrograss(float realPrograss){
        mRippleRadius = getCenter(startRadius,endRadius,realPrograss);
        mRipplePointX = getCenter(mClickPointX,mCenterPointX,realPrograss);
        mRipplePointY = getCenter(mClickPointY,mCenterPointY,realPrograss);
        mBgAlpha = (int) getCenter(0,182,realPrograss);


        invalidateSelf();
    }

    private void enterDone() {
        mEnterDone = true;
        if(mUpDone)
            startExitRunnable();
    }


    /**退出动画*/
    //退出动画的进度值
    private float mExitProgress;
    //每次递增的时间
    private float mExitIncrement = 16f/280;
    //退出动画添加插值器
    private AccelerateInterpolator mExitInterpolator = new AccelerateInterpolator(2);
    private Runnable exitRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mEnterDone){
                return;
            }

            mExitProgress = mExitProgress + mExitIncrement;
            if (mExitProgress > 1){
                onExitPrograss(1);
                exitDone();
                return;
            }

            float interpolation = mExitInterpolator.getInterpolation(mExitProgress);
            onExitPrograss(interpolation);
            scheduleSelf(this, SystemClock.uptimeMillis() + 16);
        }
    };


    /**退出动画刷新的方法
     * @parms realProgress */
    public void onExitPrograss(float realPrograss){
        //设置背景
        mBgAlpha = (int) getCenter(182,0,realPrograss);
        //设置圆形区域
        mCircleAlpha = (int) getCenter(255,0,realPrograss);

        invalidateSelf();
    }

    private void exitDone() {
        mEnterDone = false;
    }

    //设置渐变效果 包括半径/bg color/圆心位置等
    public float getCenter(float start,float end,float prograss){
        return start + (end - start)*prograss;
    }





    public RippleDrawable() {

        //抗锯齿的画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //设置抗锯齿
        mPaint.setAntiAlias(true);
        //设置放防抖动
        mPaint.setDither(true);

        setRippleColor(0x60000000);
    }

    //private int mPaintAlpha  = 255;
    //背景的透明度
    private int mBgAlpha;
    //圆形区域的透明度
    private int mCircleAlpha;
    @Override
    public void draw(Canvas canvas) {
        //获取用户设置的透明度 就是Z
        int preAlpha = mPaint.getAlpha();
        //当前背景
        int bgAlpha = (int) (preAlpha * (mBgAlpha/255f));
        //bg + prebg运算得到得背景
        int maxCircleAlpha = getCircleAlpha(preAlpha,bgAlpha);
        int circleAlpha = (int) (maxCircleAlpha * (mCircleAlpha/255f));

        mPaint.setAlpha(bgAlpha);
        canvas.drawColor(mPaint.getColor());

        mPaint.setAlpha(circleAlpha);
        canvas.drawCircle(mRipplePointX,mRipplePointY,mRippleRadius,mPaint);

        //设置最初的透明度 保证下次进入运算不会出错
        mPaint.setAlpha(preAlpha);

    }

    public int getCircleAlpha(int preAlpha,int bgAlpha){
        int dAlpha = preAlpha - bgAlpha;
        return (int) ((dAlpha*255f)/(255f - bgAlpha));
    }



    public void onTouch(MotionEvent event){
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                //按下
                mClickPointX = event.getX();
                mClickPointY = event.getY();
                onTouchDown(mClickPointX, mClickPointY);


                break;
            case MotionEvent.ACTION_MOVE:
                //移动

                //onTouchMove(moveX,moveY);


                break;
            case MotionEvent.ACTION_UP:
                //抬起

                onTouchUp();


                break;
            case MotionEvent.ACTION_CANCEL:
                //退出
                //onTouchCancel();

                break;

        }

    }

    public void onTouchDown(float x,float y){
        //Log.i("onTouchDown====",x + "" + y );
        //unscheduleSelf(runnable);
        mUpDone = false;
        mRipplePointX = x;
        mRipplePointY = y;
        mRippleRadius = 0;
        startEnterRunnable();

    }

    public void onTouchMove(float x,float y){

    }

    public void onTouchUp(){
        mUpDone = true;
        if (mEnterDone){
            startExitRunnable();
        }

    }

    public void onTouchCancel(){
        mUpDone = true;
        if (mEnterDone){
            startExitRunnable();
        }

    }

    /**
     * 开启进入动画
     * */
    public void startEnterRunnable(){


        mProgress = 0;
        //mEnterDone = false;
        unscheduleSelf(exitRunnable);
        unscheduleSelf(runnable);
        scheduleSelf(runnable,SystemClock.uptimeMillis());

    }

    /**
     * 开启退出动画
     * */
    public void startExitRunnable(){
        mExitProgress = 0;
        unscheduleSelf(runnable);
        unscheduleSelf(exitRunnable);
        scheduleSelf(exitRunnable,SystemClock.uptimeMillis());

    }

    public int changeColorAlpha(int color,int alpha){
        //设置透明度
        int a = (color >> 24) & 0xFF;
        a = a * alpha/255;

        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int argb = Color.argb(a, red, green, blue);
        return argb;
    }



    //取所绘制区域的中心点
    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mCenterPointX  = bounds.centerX();
        mCenterPointY = bounds.centerY();

        MaxRadius = Math.max(mCenterPointX,mCenterPointY);
        startRadius = MaxRadius * 0.1f;
        endRadius = MaxRadius * 0.8f;

    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
        onColorOrAlphaChange();

    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        //滤镜效果
        if (mPaint.getColorFilter() != colorFilter){
            mPaint.setColorFilter(colorFilter);
            invalidateSelf();
        }
    }

    @Override
    public int getOpacity() {
        //返回透明度
        if (mAlpha == 255){
            return PixelFormat.OPAQUE;
        }else if (mAlpha == 0){
            return PixelFormat.TRANSPARENT;
        }else{
            return PixelFormat.TRANSLUCENT;
        }

    }

    public void setRippleColor(int rippleColor) {
        this.rippleColor = rippleColor;
        onColorOrAlphaChange();
    }

    private void onColorOrAlphaChange() {
        //设置画笔颜色
        mPaint.setColor(rippleColor);
        if (mAlpha != 255){
            int pAlpha = mPaint.getAlpha();
            int realAlpha = (int) (pAlpha * (mAlpha/255f));
            //设置透明度
            mPaint.setAlpha(realAlpha);
        }
        invalidateSelf();

    }




}
