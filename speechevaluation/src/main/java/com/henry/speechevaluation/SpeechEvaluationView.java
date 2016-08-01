package com.henry.speechevaluation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by henry on 2016/7/30.
 */
public class SpeechEvaluationView extends FrameLayout {

    Context context;

    TapeView tvTape;

    RelativeLayout rlScore;

    TextView tvScore;

    SpeechEvaluatorUtil speechEvaluatorUtil;

    String language, category, content;

    public SpeechEvaluationView(Context context) {
        this(context, null);
    }

    public SpeechEvaluationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeechEvaluationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        inflate(context, R.layout.view_speech_evaluation, this);
        tvTape = (TapeView) findViewById(R.id.tv_tape);
        rlScore = (RelativeLayout) findViewById(R.id.rl_score);
        tvScore = (TextView) findViewById(R.id.tv_score);

        speechEvaluatorUtil = new SpeechEvaluatorUtil();
        speechEvaluatorUtil.init(context);

        tvTape.setOnTapeCallback(new TapeView.OnTapeCallback() {
            @Override
            public void start() {
                Log.e("start", "start");
                speechEvaluatorUtil.setParams(language, category, "-1");
                speechEvaluatorUtil.start(content, getEvaluatorCallback());
            }

            @Override
            public void cancel() {
                Log.e("cancel", "cancel");
                speechEvaluatorUtil.cancel();
            }

            @Override
            public void stop() {
                Log.e("stop", "stop");
                speechEvaluatorUtil.stop();

                setLoading();
                rlScore.setVisibility(VISIBLE);
                tvTape.setVisibility(GONE);
            }

            @Override
            public void timeout() {
                stop();
            }
        });

        // 点击分数重新测试
        rlScore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                tvTape.setVolume(0);
                rlScore.setVisibility(GONE);
                tvTape.setVisibility(VISIBLE);
            }
        });
    }

    private SpeechEvaluatorUtil.EvaluatorCallback getEvaluatorCallback() {
        return new SpeechEvaluatorUtil.EvaluatorCallback() {
            @Override
            public void onVolumeChanged(int volume, byte[] data) {
                // 更新音量
                tvTape.setVolume(volume);
            }

            @Override
            public void onResult(int score) {
                Log.e("onResult", score + "");

                // 显示得分
                setScore(score);
            }

            @Override
            public void onError(String error) {
                Log.e("onError", error);
                setScore(0);
            }
        };
    }

    /**
     * 设置分数
     * @param score
     */
    private void setScore(int score) {
        Drawable drawable = tvScore.getBackground();
        drawable.setLevel(score);

        tvScore.setTextSize(72);
        tvScore.setText(String.valueOf(score));
    }

    /**
     * 正在打分
     */
    private void setLoading() {
        Drawable drawable = tvScore.getBackground();
        drawable.setLevel(100);
        tvScore.setTextSize(24);
        tvScore.setText("正在打分");
    }

    /**
     * 设置参数
     * @param language  语言
     * @param category  词语还是句子
     * @param timeout   超时
     * @param content   内容
     */
    public void setContent(String language, String category, int timeout, String content) {
        this.language = language;
        this.category = category;
        tvTape.setDuration(timeout);
        if (TextUtils.equals(category, SpeechEvaluatorUtil.TYPE_CATEGORY_WORD)) {
            this.content = "[word]\n" + content;
        } else {
            this.content = content;
        }
    }
}
