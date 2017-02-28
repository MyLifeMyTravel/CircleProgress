package com.littlejie.app;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.littlejie.circleprogress.CircleProgress;
import com.littlejie.circleprogress.DialProgress;
import com.littlejie.circleprogress.WaveProgress;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int[] COLORS = new int[]{Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE};

    private Button mBtnResetAll;
    private CircleProgress mCircleProgress1, mCircleProgress2, mCircleProgress3;
    private DialProgress mDialProgress;
    private WaveProgress mWaveProgress;
    private Random mRandom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnResetAll = (Button) findViewById(R.id.btn_reset_all);
        mCircleProgress1 = (CircleProgress) findViewById(R.id.circle_progress_bar1);
        mCircleProgress2 = (CircleProgress) findViewById(R.id.circle_progress_bar2);
        mCircleProgress3 = (CircleProgress) findViewById(R.id.circle_progress_bar3);
        mDialProgress = (DialProgress) findViewById(R.id.dial_progress_bar);
        mWaveProgress = (WaveProgress) findViewById(R.id.wave_progress_bar);

        mBtnResetAll.setOnClickListener(this);
        mCircleProgress1.setOnClickListener(this);
        mCircleProgress2.setOnClickListener(this);
        mCircleProgress3.setOnClickListener(this);
        mDialProgress.setOnClickListener(this);
        mWaveProgress.setOnClickListener(this);

        mRandom = new Random();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset_all:
                mCircleProgress1.reset();
                mCircleProgress2.reset();
                mCircleProgress3.reset();
                mDialProgress.reset();
                break;
            case R.id.circle_progress_bar1:
                mCircleProgress1.setValue(mRandom.nextInt((int) mCircleProgress1.getMaxValue()));
                break;
            case R.id.circle_progress_bar2:
                mCircleProgress2.setValue(mRandom.nextFloat() * mCircleProgress2.getMaxValue());
                break;
            case R.id.circle_progress_bar3:
                //在代码中动态改变渐变色，可能会导致颜色跳跃
                mCircleProgress3.setGradientColors(COLORS);
                mCircleProgress3.setValue(mRandom.nextFloat() * mCircleProgress3.getMaxValue());
                break;
            case R.id.dial_progress_bar:
                mDialProgress.setValue(mRandom.nextFloat() * mDialProgress.getMaxValue());
                break;
            case R.id.wave_progress_bar:
                mWaveProgress.setValue(mRandom.nextFloat() * mWaveProgress.getMaxValue());
                break;
        }
    }
}
