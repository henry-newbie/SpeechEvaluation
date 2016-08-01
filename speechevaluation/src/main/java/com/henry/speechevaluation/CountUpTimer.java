package com.henry.speechevaluation;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * Created by henry on 2016/7/30.
 */
public abstract class CountUpTimer {
    private static final int MSG = 1;

    private final long mMillisInFuture;     // 计时总时间
    private final long mInterval;           // 计时间隔时间

    private long mStartTime;                // 开始的时间点
    private long mStopTimeInFuture;         // 停止的时间点
    private boolean isStop = false;         // 是否停止

    /**
     * @param millisInFuture    总计时时间
     * @param interval 计时间隔时间
     */
    public CountUpTimer(long millisInFuture, long interval) {
        // 解决秒数有时会减去了2秒问题
        if (interval > 1000) millisInFuture += 15;
        mMillisInFuture = millisInFuture;
        mInterval = interval;
    }

    /**
     * 开始计时
     * @param millisInFuture 总时间
     * @return
     */
    private synchronized CountUpTimer start(long millisInFuture) {
        isStop = false;
        if (millisInFuture <= 0) {
            onFinish();
            return this;
        }
        mStartTime = SystemClock.elapsedRealtime();
        mStopTimeInFuture = SystemClock.elapsedRealtime() + millisInFuture;
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }



    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (CountUpTimer.this) {
                if (isStop) {
                    return;
                }

                final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();
                if (millisLeft <= 0) {
                    onFinish();
                } else {

                    long lastTickStart = SystemClock.elapsedRealtime();
                    long past = SystemClock.elapsedRealtime() - mStartTime; // 已持续的时间
                    onTick(past);

                    // 考虑onTick方法消耗的时间
                    long delay = lastTickStart + mInterval - SystemClock.elapsedRealtime();

                    // special case: user's onTick took more than interval to
                    // complete, skip to next interval
                    while (delay < 0) delay += mInterval;

                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    };

    /**
     * 开始倒计时
     */
    public synchronized final void start() {
        start(mMillisInFuture);
    }

    /**
     * 停止倒计时
     */
    public synchronized final void stop() {
        isStop = true;
        mHandler.removeMessages(MSG);
    }

    public synchronized final long getPast() {
        return SystemClock.elapsedRealtime() - mStartTime;
    }

    /**
     * 计时间隔回调
     * @param past 已过去的毫秒数
     */
    public abstract void onTick(long past);

    /**
     * 计时结束回调
     */
    public abstract void onFinish();
}
