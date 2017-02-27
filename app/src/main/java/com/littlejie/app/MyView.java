package com.littlejie.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.littlejie.circleprogress.utils.MiscUtil;

/**
 * Created by littlejie on 2017/2/27.
 */

public class MyView extends View {

    Paint mPaint;
    Path mPath;

    float x, y;
    float radius;

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPath = new Path();
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(15);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MiscUtil.measure(widthMeasureSpec, 400),
                MiscUtil.measure(heightMeasureSpec, 400));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        x = w / 2;
        y = h / 2;
        radius = w / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float quarter = radius * 2 / 3;
        float half = radius;
        mPath.moveTo(x - radius, y);
        mPath.quadTo(x / 2, y - radius, x, y);
        mPath.quadTo(x * 3 / 2, y + radius, x + radius, y);
        canvas.drawPath(mPath, mPaint);
    }
}
