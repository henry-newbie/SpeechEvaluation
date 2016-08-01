package com.henry.speechevaluation;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by henry on 2016/7/30.
 */
public class TimerView extends TextView {

    public static final int SECOND = 1000;
    public static final int SCALE = 10;

    private int mSecond, mMillisecond, mScale;

    CountUpTimer countUpTimer;

    OnFinishListener onFinishListener;

    public TimerView(Context context) {
        this(context, null);
    }

    public TimerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置监听
     * @param onFinishListener
     */
    public void setListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    /**
     * 开始计时
     * @param millisecond   总时间
     * @param interval      时间间隔
     */
    public void start(int millisecond, int interval) {
        countUpTimer = new CountUpTimer(millisecond, interval) {
            @Override
            public void onTick(long past) {
                updateShow(past);
            }

            @Override
            public void onFinish() {
                if(onFinishListener != null) {
                    onFinishListener.onFinish();
                }
            }
        };
        countUpTimer.start();
    }

    /**
     * 停止
     */
    public void stop() {
        if(countUpTimer != null) {
            countUpTimer.stop();
        }
    }

    public long getPast() {
        return countUpTimer.getPast();
    }

    public void updateShow(long millisecond) {

        mSecond = (int) (millisecond / SECOND);
        mMillisecond = (int)(millisecond % SECOND);
        mScale = mMillisecond / SCALE;

        // 显示格式为00:00
        String secondStr, millisecondStr;
        if(mScale < 10) {
            millisecondStr = "0" + mScale;
        } else {
            millisecondStr = String.valueOf(mScale);
        }

        if(mSecond < 10) {
            secondStr = "0" + mSecond;
        } else {
            secondStr = String.valueOf(mSecond);
        }

        setText(secondStr + ":" + millisecondStr);
    }

    public interface OnFinishListener {
        void onFinish();
    }
}
