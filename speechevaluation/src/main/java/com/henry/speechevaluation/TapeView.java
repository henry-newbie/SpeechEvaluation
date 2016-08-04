package com.henry.speechevaluation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by henry on 2016/7/26.
 */
public class TapeView extends FrameLayout {

    Context context;

    ImageView ivVolume;

    CheckBox cbTape;

    TextView tvTip;

    TimerView tvTime;

    OnTapeCallback onTapeCallback;

    int millisecond;

    boolean isTimeout;

    public TapeView(Context context) {
        super(context);
        init(context);
    }

    public TapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TapeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        inflate(context, R.layout.view_tape, this);
        ivVolume = (ImageView) findViewById(R.id.iv_volume);
        cbTape = (CheckBox) findViewById(R.id.cb_tape);
        tvTip = (TextView) findViewById(R.id.tv_tip);
        tvTime = (TimerView) findViewById(R.id.tv_time);
        cbTape.setOnTouchListener(new TapeOnTouchListener());
    }

//    protected void setTapeBackground(Drawable drawable) {
//        cbTape.setBackgroundDrawable(drawable);
//    }
//
//    protected void setVolumeBackground(Drawable drawable) {
//        ivVolume.setBackgroundDrawable(drawable);
//    }

    protected void setResourse(Drawable tapeBackground, Drawable volumeBackground, int textSize) {
        cbTape.setBackgroundDrawable(tapeBackground);
        ivVolume.setImageDrawable(volumeBackground);
        tvTip.setTextSize(textSize);
    }


    /**
     * 设置录音最长时长，超时就自动取消
     * @param millisecond
     */
    public void setDuration(int millisecond) {
        this.millisecond = millisecond;
        tvTime.setListener(new TimerView.OnFinishListener() {
            @Override
            public void onFinish() {
                isTimeout = true;
                onTapeCallback.timeout();
                tvTime.setVisibility(GONE);
            }
        });
    }

    /**
     * 触摸事件监听
     * @param onTapeCallback
     */
    public void setOnTapeCallback(OnTapeCallback onTapeCallback) {
        this.onTapeCallback = onTapeCallback;
    }

    /**
     * 设置音量
     * @param volume 音量值，范围0-30
     */
    public void setVolume(int volume) {
        ivVolume.setImageLevel(volume);
    }

    class TapeOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
//            if(isTimeout) {
//                cbTape.setChecked(false);
//                tvTime.setVisibility(GONE);
//                return true;
//            }

            int radius = cbTape.getWidth() / 2; // 半径
            int distance = hypot(radius - event.getX(), radius - event.getY()); // 触摸点到圆心距离

            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isTimeout = false;
                    cbTape.setChecked(true);
                    tvTip.setText("松开完成，向上取消");

                    // 计时
                    if(millisecond <= 0) {
                        millisecond = 10000;    // 默认十秒
                    }
//                    isTimeout = false;
                    tvTime.setVisibility(VISIBLE);
                    tvTime.start(millisecond, 10);

                    if(onTapeCallback != null) {
                        onTapeCallback.start();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(isTimeout) {
                        break;
                    }
                    cbTape.setChecked(true);

                    if (slideUpRule(radius, event.getX(), event.getY(), distance)) {
                        tvTip.setText("松开后取消");
                    } else {
                        tvTip.setText("向上滑动取消");
                    }
                    break;
                case MotionEvent.ACTION_UP:

                    cbTape.setChecked(false);
                    tvTip.setText("按住录音");

                    if(isTimeout) {
                        break;
                    }

                    tvTime.setVisibility(GONE);
                    tvTime.stop();

                    // 如果录音时间小于0.3秒则无效
                    if(tvTime.getPast() <= 300) {
                        onTapeCallback.cancel();
                        break;
                    }

                    if (slideUpRule(radius, event.getX(), event.getY(), distance)) {
                        // 取消
                        if(onTapeCallback != null) {
                            onTapeCallback.cancel();
                        }
                    } else {
                        // 完成
                        if(onTapeCallback != null) {
                            onTapeCallback.stop();
                        }
                    }
                    break;
            }
            return true;
        }
    }

    /**
     * 向上滑动取消的规则（左上和右上45度方向夹角内）
     *
     * @param radius
     * @param x
     * @param y
     * @param distance
     * @return
     */
    private boolean slideUpRule(int radius, float x, float y, int distance) {
        if (distance > radius && y < radius) {   // 圆外部，圆心上半部分的范围
            if (x == radius) {
                return true;
            }
            double angle = calculateAngle(radius - x, distance);
            if (angle > 45) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 勾股定理，计算斜边长度
     *
     * @param x
     * @param y
     * @return
     */
    private int hypot(float x, float y) {
        return (int) Math.sqrt(x * x + y * y);
    }

    /**
     * 反余弦函数，求出夹角
     *
     * @param x
     * @param z
     * @return
     */
    private double calculateAngle(float x, float z) {
        double radian = Math.acos(Math.abs(x) / z);   // 弧度
        return Math.toDegrees(radian);
    }

    public interface OnTapeCallback {
        void start();   // 开始
        void cancel();  // 取消
        void stop();    // 完成
        void timeout(); // 超时
    }
}
