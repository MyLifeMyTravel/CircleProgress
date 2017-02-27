package com.littlejie.circleprogress;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.littlejie.circleprogress.utils.Constant;
import com.littlejie.circleprogress.utils.MiscUtil;

/**
 * 水波进度条
 * Created by littlejie on 2017/2/26.
 */

public class WaveProgress extends View {

    private static final String TAG = WaveProgress.class.getSimpleName();

    private int mDefaultSize;
    //圆心
    private Point mCenterPoint;
    //半径
    private float mRadius;
    //圆的外接矩形
    private RectF mRectF;
    //深色波浪移动距离
    private float mDarkWaveMoving;
    //浅色波浪移动距离
    private float mLightWaveMoving;

    //是否开启抗锯齿
    private boolean antiAlias;
    //最大值
    private float mMaxValue;
    //当前值
    private float mValue;
    //当前进度
    private float mPercent;

    //圆环宽度
    private float mCircleWidth;
    //圆环
    private Paint mCirclePaint;
    //圆环颜色
    private int mCircleColor;
    //背景圆环颜色
    private int mBgCircleColor;

    //水波路径
    private Path mWavePath;
    private Path mDarkWavePath;
    private Path mLightWavePath;
    //水波高度
    private float mWaveHeight;
    //水波数量
    private int mWaveNum;
    //深色水波
    private Paint mDarkWavePaint;
    //深色水波颜色
    private int mDarkWaveColor;

    //浅色水波
    private Paint mLightWavePaint;
    //浅色水波颜色
    private int mLightWaveColor;

    //深色水波贝塞尔曲线上的起始点、控制点
    private Point[] mDarkPoints;
    //浅色水波贝塞尔曲线上的起始点、控制点
    private Point[] mLightPoints;

    //贝塞尔曲线点的总个数
    private int mAllPointCount;
    private int mHalfPointCount;

    private ValueAnimator mProgressAnimator;
    private long mDarkWaveAnimTime;
    private ValueAnimator mDarkWaveAnimator;
    private long mLightWaveAnimTime;
    private ValueAnimator mLightWaveAnimator;

    public WaveProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mDefaultSize = MiscUtil.dipToPx(context, Constant.DEFAULT_SIZE);
        mRectF = new RectF();
        mCenterPoint = new Point();

        initAttrs(context, attrs);
        initPaint();
        initPath();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveProgress);

        antiAlias = typedArray.getBoolean(R.styleable.WaveProgress_antiAlias, true);
        mDarkWaveAnimTime = typedArray.getInt(R.styleable.WaveProgress_darkWaveAnimTime, Constant.DEFAULT_ANIM_TIME);
        mLightWaveAnimTime = typedArray.getInt(R.styleable.WaveProgress_lightWaveAnimTime, Constant.DEFAULT_ANIM_TIME);
        mMaxValue = typedArray.getFloat(R.styleable.WaveProgress_maxValue, Constant.DEFAULT_MAX_VALUE);
        mValue = typedArray.getFloat(R.styleable.WaveProgress_value, Constant.DEFAULT_VALUE);

        mCircleWidth = typedArray.getDimension(R.styleable.WaveProgress_circleWidth, Constant.DEFAULT_ARC_WIDTH);
        mCircleColor = typedArray.getColor(R.styleable.WaveProgress_circleColor, Color.GREEN);
        mBgCircleColor = typedArray.getColor(R.styleable.WaveProgress_bgCircleColor, Color.WHITE);

        mWaveHeight = typedArray.getDimension(R.styleable.WaveProgress_waveHeight, Constant.DEFAULT_WAVE_HEIGHT);
        mWaveNum = typedArray.getInt(R.styleable.WaveProgress_waveNum, 1);
        mDarkWaveColor = typedArray.getColor(R.styleable.WaveProgress_darkWaveColor,
                getResources().getColor(android.R.color.holo_blue_dark));
        mLightWaveColor = typedArray.getColor(R.styleable.WaveProgress_lightWaveColor,
                getResources().getColor(android.R.color.holo_green_light));

        typedArray.recycle();
    }

    private void initPaint() {
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(antiAlias);
        mCirclePaint.setStrokeWidth(mCircleWidth);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeCap(Paint.Cap.ROUND);

        mDarkWavePaint = new Paint();
        mDarkWavePaint.setAntiAlias(antiAlias);
        mDarkWavePaint.setStyle(Paint.Style.FILL);
        mDarkWavePaint.setColor(mDarkWaveColor);

        mLightWavePaint = new Paint();
        mLightWavePaint.setAntiAlias(antiAlias);
        mLightWavePaint.setStyle(Paint.Style.FILL);
        mLightWavePaint.setColor(mLightWaveColor);
    }

    private void initPath() {
        mWavePath = new Path();
        mDarkWavePath = new Path();
        mLightWavePath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MiscUtil.measure(widthMeasureSpec, mDefaultSize),
                MiscUtil.measure(heightMeasureSpec, mDefaultSize));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged: w = " + w + "; h = " + h + "; oldw = " + oldw + "; oldh = " + oldh);
        int minSize = Math.min(getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - 2 * (int) mCircleWidth,
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - 2 * (int) mCircleWidth);
        mRadius = minSize / 2;
        mCenterPoint.x = getMeasuredWidth() / 2;
        mCenterPoint.y = getMeasuredHeight() / 2;
        //绘制圆弧的边界
        mRectF.left = mCenterPoint.x - mRadius - mCircleWidth / 2;
        mRectF.top = mCenterPoint.y - mRadius - mCircleWidth / 2;
        mRectF.right = mCenterPoint.x + mRadius + mCircleWidth / 2;
        mRectF.bottom = mCenterPoint.y + mRadius + mCircleWidth / 2;
        Log.d(TAG, "onSizeChanged: 控件大小 = " + "(" + getMeasuredWidth() + ", " + getMeasuredHeight() + ")"
                + ";圆心坐标 = " + mCenterPoint.toString()
                + ";圆半径 = " + mRadius
                + ";圆的外接矩形 = " + mRectF.toString());
        initWavePoints();
        startWaveAnimator();
    }

    private void initWavePoints() {
        //当前波浪宽度
        float waveWidth = (mRadius * 2) / mWaveNum;
        mAllPointCount = 8 * mWaveNum + 1;
        mHalfPointCount = mAllPointCount / 2;
        mDarkPoints = getPointFromLeft2Right(waveWidth);
        mLightPoints = getPointFromRight2Left(waveWidth);
    }

    /**
     * 从左往右获取贝塞尔点
     *
     * @return
     */
    private Point[] getPointFromLeft2Right(float waveWidth) {
        Point[] points = new Point[mAllPointCount];
        //第1个点特殊处理
        points[mHalfPointCount] = new Point((int) (mCenterPoint.x - mRadius), mCenterPoint.y);
        //屏幕内的贝塞尔曲线点
        for (int i = mHalfPointCount + 1; i < mAllPointCount; i += 4) {
            float width = waveWidth * (i / 4 - 1);
            points[i] = new Point((int) (waveWidth / 4 + width), (int) (mCenterPoint.y - mWaveHeight));
            points[i + 1] = new Point((int) (waveWidth / 2 + width), mCenterPoint.y);
            points[i + 2] = new Point((int) (waveWidth * 3 / 4 + width), (int) (mCenterPoint.y + mWaveHeight));
            points[i + 3] = new Point((int) (waveWidth + width), mCenterPoint.y);
        }
        //屏幕外的贝塞尔曲线点
        for (int i = 0; i < mHalfPointCount; i++) {
            int reverse = mAllPointCount - i - 1;
            points[i] = new Point(points[mHalfPointCount].x - points[reverse].x,
                    points[mHalfPointCount].y * 2 - points[reverse].y);
        }
        return points;
    }

    /**
     * 从右往左获取贝塞尔点
     *
     * @return
     */
    private Point[] getPointFromRight2Left(float waveWidth) {
        Point[] points = new Point[mAllPointCount];
        //第1个点特殊处理
        points[mHalfPointCount] = new Point((int) (mCenterPoint.x + mRadius), mCenterPoint.y);
        //屏幕内的贝塞尔曲线点
        for (int i = mHalfPointCount + 1; i < mAllPointCount; i += 4) {
            float width = mCenterPoint.x + mRadius + waveWidth * (i / 4 - 1);
            points[i] = new Point((int) (waveWidth / 4 + width), (int) (mCenterPoint.y - mWaveHeight));
            points[i + 1] = new Point((int) (waveWidth / 2 + width), mCenterPoint.y);
            points[i + 2] = new Point((int) (waveWidth * 3 / 4 + width), (int) (mCenterPoint.y + mWaveHeight));
            points[i + 3] = new Point((int) (waveWidth + width), mCenterPoint.y);
        }
        //屏幕外的贝塞尔曲线点
        for (int i = 0; i < mHalfPointCount; i++) {
            int reverse = mAllPointCount - i - 1;
            points[i] = new Point(2 * points[mHalfPointCount].x - points[reverse].x,
                    points[mHalfPointCount].y * 2 - points[reverse].y);
        }
        return points;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(canvas);
        drawLightWave(canvas);
        drawDarkWave(canvas);
    }

    /**
     * 绘制圆环
     *
     * @param canvas
     */
    private void drawCircle(Canvas canvas) {
        canvas.save();
        canvas.rotate(270, mCenterPoint.x, mCenterPoint.y);
        float currentAngle = 360 * mPercent;
        //画圆环
        mCirclePaint.setColor(mCircleColor);
        canvas.drawArc(mRectF, 0, currentAngle, false, mCirclePaint);
        //画背景圆环
        mCirclePaint.setColor(mBgCircleColor);
        canvas.drawArc(mRectF, currentAngle, 360, false, mCirclePaint);
        canvas.restore();
    }

    /**
     * 绘制深色波浪(贝塞尔曲线)
     *
     * @param canvas
     */
    private void drawDarkWave(Canvas canvas) {
        mDarkWavePath.reset();
        //当前波浪位移
        float waveMoving = mDarkWaveMoving;
        mDarkWavePath.moveTo(mDarkPoints[0].x + waveMoving, mDarkPoints[0].y);

        for (int i = 1; i < mAllPointCount; i += 2) {
            mDarkWavePath.quadTo(mDarkPoints[i].x + waveMoving, mDarkPoints[i].y,
                    mDarkPoints[i + 1].x + waveMoving, mDarkPoints[i + 1].y);
        }
        mDarkWavePath.lineTo(mDarkPoints[mAllPointCount - 1].x, mDarkPoints[mAllPointCount - 1].y);
        mDarkWavePath.lineTo(mDarkPoints[mAllPointCount - 1].x, mDarkPoints[mAllPointCount - 1].y + mRadius);
        mDarkWavePath.lineTo(mDarkPoints[0].x, mDarkPoints[0].y + mRadius);
        mDarkWavePath.close();
        drawWave(canvas, mDarkWavePaint, mDarkWavePath);
    }

    /**
     * 绘制浅色波浪(贝塞尔曲线)
     *
     * @param canvas
     */
    private void drawLightWave(Canvas canvas) {
        mLightWavePath.reset();
        //当前波浪位移
        float waveMoving = mLightWaveMoving;
        mLightWavePath.moveTo(mLightPoints[mAllPointCount - 1].x - waveMoving, mLightPoints[mAllPointCount - 1].y);

        for (int i = mAllPointCount - 2; i > 0; i -= 2) {
            mLightWavePath.quadTo(mLightPoints[i].x - waveMoving, mLightPoints[i].y,
                    mLightPoints[i - 1].x - waveMoving, mLightPoints[i - 1].y);
        }
        mLightWavePath.lineTo(mLightPoints[0].x, mLightPoints[0].y);
        mLightWavePath.lineTo(mLightPoints[0].x, mLightPoints[0].y + mRadius);
        mLightWavePath.lineTo(mLightPoints[mAllPointCount - 1].x, mLightPoints[mAllPointCount - 1].y + mRadius);
        mLightWavePath.close();
        drawWave(canvas, mLightWavePaint, mLightWavePath);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void drawWave(Canvas canvas, Paint paint, Path path) {
        mWavePath.reset();
        mWavePath.addCircle(mCenterPoint.x, mCenterPoint.y, mRadius, Path.Direction.CW);
        //取该圆与波浪路径的交集，形成波浪在圆内的效果
        mWavePath.op(path, Path.Op.INTERSECT);
        canvas.drawPath(mWavePath, paint);
    }

    public float getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(float maxValue) {
        mMaxValue = maxValue;
    }

    /**
     * 设置当前值
     *
     * @param value
     */
    public void setValue(float value) {
        if (value > mMaxValue) {
            value = mMaxValue;
        }
        float start = mPercent;
        float end = value / mMaxValue;
        startAnimator(start, end, mDarkWaveAnimTime);
    }

    private void startAnimator(float start, float end, long animTime) {
        mProgressAnimator = ValueAnimator.ofFloat(start, end);
        mProgressAnimator.setDuration(animTime);
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPercent = (float) animation.getAnimatedValue();
                mValue = mPercent * mMaxValue;
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onAnimationUpdate: percent = " + mPercent
                            + ";value = " + mValue);
                }
                invalidate();
            }
        });
        mProgressAnimator.start();
    }

    public void startWaveAnimator() {
        startLightWaveAnimator();
        startDarkWaveAnimator();
    }

    public void stopWaveAnimator() {
        if (mDarkWaveAnimator != null && mDarkWaveAnimator.isRunning()) {
            mDarkWaveAnimator.cancel();
            mDarkWaveAnimator = null;
        }
        if (mLightWaveAnimator != null && mLightWaveAnimator.isRunning()) {
            mLightWaveAnimator.cancel();
            mLightWaveAnimator = null;
        }
    }

    private void startLightWaveAnimator() {
        mLightWaveAnimator = ValueAnimator.ofFloat(0, 2 * mRadius);
        mLightWaveAnimator.setDuration(mLightWaveAnimTime);
        mLightWaveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mLightWaveAnimator.setInterpolator(new LinearInterpolator());
        mLightWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLightWaveMoving = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        mLightWaveAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLightWaveMoving = 0;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mLightWaveAnimator.start();
    }

    private void startDarkWaveAnimator() {
        mDarkWaveAnimator = ValueAnimator.ofFloat(0, 2 * mRadius);
        mDarkWaveAnimator.setDuration(mDarkWaveAnimTime);
        mDarkWaveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mDarkWaveAnimator.setInterpolator(new LinearInterpolator());
        mDarkWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDarkWaveMoving = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        mDarkWaveAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mDarkWaveMoving = 0;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mDarkWaveAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopWaveAnimator();
        if (mProgressAnimator != null && mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }
    }
}
