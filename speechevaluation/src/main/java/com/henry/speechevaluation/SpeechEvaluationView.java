package com.henry.speechevaluation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
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

    FrameLayout rlScore;

    TextView tvScore, tvLabel;

    SpeechEvaluatorUtil speechEvaluatorUtil;

    String language, category, content;

    SpeechEvaluatorCallback speechEvaluatorCallback;

    Drawable tapeBackground, volumeBackground;

    int textSize, scoreTextSize, scoreBackgroundSize;

    String recordPath;

    public SpeechEvaluationView(Context context) {
        this(context, null);
    }

    public SpeechEvaluationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeechEvaluationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SpeechEvaluationView);
        tapeBackground = typedArray.getDrawable(R.styleable.SpeechEvaluationView_tapeBackground);
        volumeBackground = typedArray.getDrawable(R.styleable.SpeechEvaluationView_volumeBackground);
        textSize = typedArray.getDimensionPixelSize(R.styleable.SpeechEvaluationView_labelTextSize, getResources().getDimensionPixelSize(R.dimen.text_size));

        scoreTextSize = typedArray.getDimensionPixelSize(R.styleable.SpeechEvaluationView_scoreTextSize, getResources().getDimensionPixelSize(R.dimen.text_size));
        scoreBackgroundSize = typedArray.getDimensionPixelSize(R.styleable.SpeechEvaluationView_scoreBackgroundSize, getResources().getDimensionPixelSize(R.dimen.scoreBackgroundSize));
//        scoreBackground = typedArray.getDrawable(R.styleable.SpeechEvaluationView_scoreBackground);

        typedArray.recycle();

        if(tapeBackground == null) {
            tapeBackground = getResources().getDrawable(R.drawable.selector_tape);
        }
        if(volumeBackground == null) {
            volumeBackground = getResources().getDrawable(R.drawable.level_volume);
        }
//        if(scoreBackground == null) {
//            scoreBackground = getResources().getDrawable(R.drawable.level_score);
//        }
        this.context = context;
        init();
    }

    private void init() {
        inflate(context, R.layout.view_speech_evaluation, this);
        tvTape = (TapeView) findViewById(R.id.tv_tape);
        rlScore = (FrameLayout) findViewById(R.id.rl_score);
        tvScore = (TextView) findViewById(R.id.tv_score);
        tvLabel = (TextView) findViewById(R.id.tv_label);

        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        tvScore.setTextSize(TypedValue.COMPLEX_UNIT_PX, scoreTextSize);

        LayoutParams params = (LayoutParams) tvScore.getLayoutParams();
        params.width = scoreBackgroundSize;
        params.height = scoreBackgroundSize;
        tvScore.setLayoutParams(params);
        tvScore.setBackgroundDrawable(setScoreBackgroundSize());
//        tvScore.setBackgroundDrawable(scoreBackground);

        tvTape.setResource(tapeBackground, volumeBackground, textSize);
//        tvTape.setTapeBackground(tapeBackground);
//        tvTape.setVolumeBackground(volumeBackground);
        tvTape.setOnTapeCallback(new TapeView.OnTapeCallback() {
            @Override
            public void start() {
                Log.e("start", "start");
                if(speechEvaluatorUtil != null) {
                    speechEvaluatorUtil.setParams(language, category, "-1", recordPath);
                    speechEvaluatorUtil.start(content, getEvaluatorCallback());
                }
                if(speechEvaluatorCallback != null) {
                    speechEvaluatorCallback.onStart();
                }
            }

            @Override
            public void cancel() {
                Log.e("cancel", "cancel");
                if(speechEvaluatorUtil != null) {
                    speechEvaluatorUtil.cancel();
                }
            }

            @Override
            public void stop() {
                Log.e("stop", "stop");
                if(speechEvaluatorUtil != null) {
                    speechEvaluatorUtil.stop();
                }

                setLoading();
                rlScore.setVisibility(VISIBLE);
                tvTape.setVisibility(INVISIBLE);
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
                if(speechEvaluatorCallback != null) {
                    speechEvaluatorCallback.onRetry();
                }
            }
        });
    }

    /**
     * 设置最终得分的背景
     * @return
     */
    private LevelListDrawable setScoreBackgroundSize() {
        LevelListDrawable levelListDrawable = new LevelListDrawable();
//        GradientDrawable gradientDrawableLow = new GradientDrawable();
//        gradientDrawableLow.setShape(GradientDrawable.OVAL);
//        gradientDrawableLow.setColor(Color.parseColor("#ff344c"));
//        gradientDrawableLow.setSize(size, size);
//
//        GradientDrawable gradientDrawableMiddle = new GradientDrawable();
//        gradientDrawableMiddle.setShape(GradientDrawable.OVAL);
//        gradientDrawableMiddle.setColor(Color.parseColor("#ffa025"));
//        gradientDrawableMiddle.setSize(size, size);
//
//        GradientDrawable gradientDrawableHigh = new GradientDrawable();
//        gradientDrawableHigh.setShape(GradientDrawable.OVAL);
//        gradientDrawableHigh.setColor(Color.parseColor("#0dc67a"));
//        gradientDrawableHigh.setSize(size, size);

        levelListDrawable.addLevel(0, 59, getResources().getDrawable(R.drawable.bg_score_level1));
        levelListDrawable.addLevel(60, 79, getResources().getDrawable(R.drawable.bg_score_level2));
        levelListDrawable.addLevel(80, 100, getResources().getDrawable(R.drawable.bg_score_level3));
        return levelListDrawable;
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

                if(speechEvaluatorCallback != null) {
                    speechEvaluatorCallback.onResult(score);
                }
            }

            @Override
            public void onError(String error) {
                Log.e("onError", error);
                setScore(0);
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        speechEvaluatorUtil = new SpeechEvaluatorUtil();
        speechEvaluatorUtil.init(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        speechEvaluatorUtil.destroy();
        speechEvaluatorUtil = null;
    }

    /**
     * 设置分数
     *
     * @param score
     */
    private void setScore(int score) {
        tvScore.setBackgroundDrawable(setScoreBackgroundSize());
        Drawable drawable = tvScore.getBackground();
        drawable.setLevel(score);

        tvScore.setTextSize(TypedValue.COMPLEX_UNIT_PX, scoreTextSize);
        tvScore.setText(String.valueOf(score));
    }

    /**
     * 正在打分
     */
    private void setLoading() {
//        Drawable drawable = tvScore.getBackground();
//        drawable.setLevel(100);
        tvScore.setTextSize(TypedValue.COMPLEX_UNIT_PX, scoreTextSize / 2);
        tvScore.setText("正在打分");
        tvScore.setBackgroundResource(R.drawable.bg_score);
    }

    /**
     * 设置参数
     *
     * @param language 语言
     * @param category 词语还是句子
     * @param timeout  超时
     * @param content  内容
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

    /**
     * 只展示结果页
     *
     * @param score
     */
    public void displayResult(int score) {
        tvTape.setVisibility(INVISIBLE);
        rlScore.setVisibility(VISIBLE);
        tvLabel.setVisibility(INVISIBLE);
        rlScore.setEnabled(false);
        setScore(score);
    }

    /**
     * 显示分数
     * @param score
     */
    public void showScore(int score) {
        tvTape.setVisibility(INVISIBLE);
        rlScore.setVisibility(VISIBLE);
        setScore(score);
    }

    /**
     * 重置
     */
    public void reset() {
        tvTape.setVisibility(VISIBLE);
        tvLabel.setVisibility(VISIBLE);
        rlScore.setVisibility(GONE);
        rlScore.setEnabled(true);
        tvTape.setVolume(0);
    }

    /**
     * 设置录音文件路径
     * @param path
     */
    public void setRecordPath(String path) {
        this.recordPath = path;
    }

    /**
     * 获取录音文件路径
     * @return
     */
    public String getRecordPath() {
        return recordPath;
    }


    public void setListener(SpeechEvaluatorCallback speechEvaluatorCallback) {
        this.speechEvaluatorCallback = speechEvaluatorCallback;
    }

    public interface SpeechEvaluatorCallback {
        void onResult(int score);
        void onRetry();
        void onStart();
    }
}
