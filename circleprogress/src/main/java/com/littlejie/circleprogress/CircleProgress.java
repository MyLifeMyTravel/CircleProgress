package com.littlejie.circleprogress;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 类似 QQ 中计步器 UI
 * Created by littlejie on 2017/2/21.
 */

public class CircleProgress extends View {

    private static final String TAG = CircleProgress.class.getSimpleName();
    private Context mContext;

    //默认大小
    private int mDefaultSize;
    //是否开启抗锯齿
    private boolean antiAlias;
    //绘制标题
    private TextPaint mHintPaint;
    private CharSequence mHint;
    private int mHintColor;
    private float mHintSize;

    //绘制单位
    private TextPaint mUnitPaint;
    private CharSequence mUnit;
    private int mUnitColor;
    private float mUnitSize;

    //绘制数值
    private TextPaint mValuePaint;
    private float mValue;
    private float mMaxValue;
    private int mPrecision;
    private String mPrecisionFormat;
    private int mValueColor;
    private float mValueSize;

    //绘制圆弧
    private Paint mArcPaint;
    private int mArcColor1, mArcColor2, mArcColor3;
    private float mArcWidth;
    private float mStartAngle, mSweepAngle;
    private RectF mRectF;
    //渐变
    private Matrix mRotateMatrix;
    //渐变的颜色是360度，如果只显示270，那么则会缺失部分颜色
    private SweepGradient mSweepGradient;
    private int[] mGradientColors = {Color.GREEN, Color.YELLOW, Color.RED};
    //当前进度，[0.0f,1.0f]
    private float mPercent;
    //动画时间
    private long mAnimTime;
    //属性动画
    private ValueAnimator mAnimator;

    //绘制背景圆弧
    private Paint mBgArcPaint;
    private int mBgArcColor;
    private float mBgArcWidth;

    //圆心坐标，半径
    private float mFloatX, mFloatY, mRadius;
    //在屏幕上的坐标
    private int[] mLocationOnScreen = new int[2];

    public CircleProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mDefaultSize = MiscUtil.dipToPx(mContext, 150);
        mAnimator = new ValueAnimator();
        getLocationOnScreen(mLocationOnScreen);
        mRectF = new RectF();
        initAttrs(attrs);
        initPaint();
        setValue(mValue);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);

        antiAlias = typedArray.getBoolean(R.styleable.CircleProgressBar_antiAlias, false);

        mHint = typedArray.getString(R.styleable.CircleProgressBar_hint);
        mHintColor = typedArray.getColor(R.styleable.CircleProgressBar_hintColor, Color.BLACK);
        mHintSize = typedArray.getDimension(R.styleable.CircleProgressBar_hintSize, 15);

        mValue = typedArray.getFloat(R.styleable.CircleProgressBar_value, 0);
        mMaxValue = typedArray.getFloat(R.styleable.CircleProgressBar_maxValue, 0);
        //内容数值精度格式
        mPrecision = typedArray.getInt(R.styleable.CircleProgressBar_precision, 0);
        mPrecisionFormat = getPrecisionFormat(mPrecision);
        mValueColor = typedArray.getColor(R.styleable.CircleProgressBar_valueColor, Color.BLACK);
        mValueSize = typedArray.getDimension(R.styleable.CircleProgressBar_valueSize, 60);

        mUnit = typedArray.getString(R.styleable.CircleProgressBar_unit);
        mUnitColor = typedArray.getColor(R.styleable.CircleProgressBar_unitColor, Color.BLACK);
        mUnitSize = typedArray.getDimension(R.styleable.CircleProgressBar_unitSize, 15);

        // 设置渐变色
        mArcColor1 = typedArray.getColor(R.styleable.CircleProgressBar_arcColor1, Color.GREEN);
        mArcColor2 = typedArray.getColor(R.styleable.CircleProgressBar_arcColor2, Color.YELLOW);
        mArcColor3 = typedArray.getColor(R.styleable.CircleProgressBar_arcColor3, Color.RED);
        mGradientColors = new int[]{mArcColor1, mArcColor2, mArcColor3};

        mArcWidth = typedArray.getDimension(R.styleable.CircleProgressBar_arcWidth, 15);
        mStartAngle = typedArray.getFloat(R.styleable.CircleProgressBar_startAngle, 270);
        mSweepAngle = typedArray.getFloat(R.styleable.CircleProgressBar_sweepAngle, 360);

        mBgArcColor = typedArray.getColor(R.styleable.CircleProgressBar_bgArcColor, Color.WHITE);
        mBgArcWidth = typedArray.getDimension(R.styleable.CircleProgressBar_bgArcWidth, 15);

        //mPercent = typedArray.getFloat(R.styleable.CircleProgressBar_percent, 0);
        mAnimTime = typedArray.getInt(R.styleable.CircleProgressBar_animTime, 1000);

        typedArray.recycle();
    }

    private String getPrecisionFormat(int precision) {
        return "%." + precision + "f";
    }

    private void initPaint() {
        mHintPaint = new TextPaint();
        // 设置抗锯齿,会消耗较大资源，绘制图形速度会变慢。
        mHintPaint.setAntiAlias(antiAlias);
        // 设置绘制文字大小
        mHintPaint.setTextSize(mHintSize);
        // 设置画笔颜色
        mHintPaint.setColor(mHintColor);
        // 从中间向两边绘制，不需要再次计算文字
        mHintPaint.setTextAlign(Paint.Align.CENTER);

        mValuePaint = new TextPaint();
        mValuePaint.setAntiAlias(antiAlias);
        mValuePaint.setTextSize(mValueSize);
        mValuePaint.setColor(mValueColor);
        // 设置Typeface对象，即字体风格，包括粗体，斜体以及衬线体，非衬线体等
        mValuePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mValuePaint.setTextAlign(Paint.Align.CENTER);

        mUnitPaint = new TextPaint();
        mUnitPaint.setAntiAlias(antiAlias);
        mUnitPaint.setTextSize(mUnitSize);
        mUnitPaint.setColor(mUnitColor);
        mUnitPaint.setTextAlign(Paint.Align.CENTER);

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(antiAlias);
        mArcPaint.setColor(mArcColor1);
        // 设置画笔的样式，为FILL，FILL_OR_STROKE，或STROKE
        mArcPaint.setStyle(Paint.Style.STROKE);
        // 设置画笔粗细
        mArcPaint.setStrokeWidth(mArcWidth);
        // 当画笔样式为STROKE或FILL_OR_STROKE时，设置笔刷的图形样式，如圆形样式
        // Cap.ROUND,或方形样式 Cap.SQUARE
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);
        mRotateMatrix = new Matrix();

        mBgArcPaint = new Paint();
        mBgArcPaint.setAntiAlias(antiAlias);
        mBgArcPaint.setColor(mBgArcColor);
        mBgArcPaint.setStyle(Paint.Style.STROKE);
        mBgArcPaint.setStrokeWidth(mBgArcWidth);
        mBgArcPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //设置默认内边距，防止圆弧与边界重叠
        int padding = MiscUtil.dipToPx(mContext, 5);
        setPadding(padding, padding, padding, padding);
        //因为是画圆，所以宽高相等
        int measuredWidth = MiscUtil.measure(widthMeasureSpec, mDefaultSize);
        int measuredHeight = MiscUtil.measure(heightMeasureSpec, mDefaultSize);
        //求最小值作为实际值
        int size = Math.min(measuredWidth, measuredHeight);
        setMeasuredDimension(measuredWidth + getPaddingLeft() + getPaddingRight(),
                measuredHeight + getPaddingTop() + getPaddingBottom());
        //获取圆的相关参数
        mFloatX = mLocationOnScreen[0] + size / 2 + getPaddingLeft();
        mFloatY = mLocationOnScreen[1] + size / 2 + getPaddingTop();
        //求圆弧和背景圆弧的最大宽度
        float maxArcWidth = Math.max(mArcWidth, mBgArcWidth);
        //减去圆弧的宽度，否则会造成部分圆弧绘制在外围，通过clipPadding属性可以解决
        mRadius = size / 2 - maxArcWidth;
        //绘制圆弧的边界
        mRectF.left = mLocationOnScreen[0] + getPaddingLeft();
        mRectF.top = mLocationOnScreen[1] + getPaddingTop();
        mRectF.right = mRectF.left + size;
        mRectF.bottom = mRectF.top + size;
        updateArcPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawText(canvas);
        drawArc(canvas);
    }

    /**
     * 绘制内容文字
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        // 计算文字宽度，由于Paint已设置为居中绘制，故此处不需要重新计算
        // float textWidth = mValuePaint.measureText(mValue.toString());
        // float x = mFloatX - textWidth / 2;
        float y = mFloatY - (mValuePaint.descent() + mValuePaint.ascent()) / 2;
        canvas.drawText(String.format(mPrecisionFormat, mValue), mFloatX, y, mValuePaint);

        if (mHint != null) {
            float hy = mFloatY * 2 / 3 - (mHintPaint.descent() + mHintPaint.ascent()) / 2;
            canvas.drawText(mHint.toString(), mFloatX, hy, mHintPaint);
        }

        if (mUnit != null) {
            float uy = mFloatY * 4 / 3 - (mUnitPaint.descent() + mUnitPaint.ascent()) / 2;
            canvas.drawText(mUnit.toString(), mFloatX, uy, mUnitPaint);
        }
    }

    private void drawArc(Canvas canvas) {
        // 绘制背景圆弧
        // 从进度圆弧结束的地方开始重新绘制，优化性能
        float currentAngle = mSweepAngle * mPercent;
        canvas.drawArc(mRectF, mStartAngle, mSweepAngle, false, mBgArcPaint);
        // 第一个参数 oval 为 RectF 类型，即圆弧显示区域
        // startAngle 和 sweepAngle  均为 float 类型，分别表示圆弧起始角度和圆弧度数
        // 3点钟方向为0度，顺时针递增
        // 如果 startAngle < 0 或者 > 360,则相当于 startAngle % 360
        // useCenter:如果为True时，在绘制圆弧时将圆心包括在内，通常用来绘制扇形
        canvas.drawArc(mRectF, mStartAngle, currentAngle, false, mArcPaint);
    }

    /**
     * 更新圆弧画笔
     */
    private void updateArcPaint() {
        // 设置渐变
        mSweepGradient = new SweepGradient(mFloatX, mFloatY, mGradientColors, null);
        // 矩阵变化，-5是因为开始颜色可能会与结束颜色重叠
        mRotateMatrix.setRotate(mStartAngle - 5, mFloatX, mFloatY);
        mSweepGradient.setLocalMatrix(mRotateMatrix);
        mArcPaint.setShader(mSweepGradient);
    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    public CharSequence getHint() {
        return mHint;
    }

    public void setHint(CharSequence hint) {
        mHint = hint;
    }

    public CharSequence getUnit() {
        return mUnit;
    }

    public void setUnit(CharSequence unit) {
        mUnit = unit;
    }

    public float getValue() {
        return mValue;
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
        startAnimator(start, end, mAnimTime);
    }

    private void startAnimator(float start, float end, long animTime) {
        mAnimator.setDuration(animTime);
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPercent = (float) animation.getAnimatedValue();
                mValue = mPercent * mMaxValue;
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onAnimationUpdate: percent = " + mPercent
                            + ";currentAngle = " + (mSweepAngle * mPercent)
                            + ";value = " + mValue);
                }
                invalidate();
            }
        });
        mAnimator.start();
    }

    /**
     * 获取最大值
     *
     * @return
     */
    public float getMaxValue() {
        return mMaxValue;
    }

    /**
     * 设置最大值
     *
     * @param maxValue
     */
    public void setMaxValue(float maxValue) {
        mMaxValue = maxValue;
    }

    /**
     * 获取精度
     *
     * @return
     */
    public int getPrecision() {
        return mPrecision;
    }

    public void setPrecision(int precision) {
        mPrecision = precision;
        mPrecisionFormat = getPrecisionFormat(precision);
    }

    public int[] getGradientColors() {
        return mGradientColors;
    }

    /**
     * 设置渐变
     *
     * @param gradientColors
     */
    public void setGradientColors(int[] gradientColors) {
        mGradientColors = gradientColors;
        updateArcPaint();
    }

    public long getAnimTime() {
        return mAnimTime;
    }

    public void setAnimTime(long animTime) {
        mAnimTime = animTime;
    }

    /**
     * 重置
     */
    public void reset() {
        mValue = 0.0f;
        mPercent = 0.0f;
        startAnimator(0.0f, 0.0f, 0L);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //释放资源
    }
}
