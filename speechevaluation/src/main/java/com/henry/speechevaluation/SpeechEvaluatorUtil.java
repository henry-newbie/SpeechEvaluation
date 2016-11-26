package com.henry.speechevaluation;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.EvaluatorListener;
import com.iflytek.cloud.EvaluatorResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvaluator;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by henry on 2016/7/27.
 */
public class SpeechEvaluatorUtil {

    public static final String TYPE_LANGUAGE_CN = "zh_cn";                  // 中文

    public static final String TYPE_LANGUAGE_EN = "en_us";                  // 英文

    public static final String TYPE_CATEGORY_SENTENCE = "read_sentence";    // 句子

    public static final String TYPE_CATEGORY_WORD = "read_word";            // 词语

    private SpeechEvaluator mIse;

    EvaluatorCallback evaluatorCallback;

//    private String recordPath;

    private Context context;

    public static void createUtility(Context context, String appId) {
        SpeechUtility.createUtility(context, "appid=" + appId);
    }

    public void init(Context context) {
        this.context = context;
        mIse = SpeechEvaluator.createEvaluator(context, null);
    }

    public void setParams(String language, String category, String timeout, String recordPath) {
        mIse.setParameter(SpeechConstant.LANGUAGE, language);
        mIse.setParameter(SpeechConstant.ISE_CATEGORY, category);
        mIse.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIse.setParameter(SpeechConstant.VAD_BOS, "5000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIse.setParameter(SpeechConstant.VAD_EOS, "1800");
//        // 语音输入超时时间，即用户最多可以连续说多长时间
        mIse.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, timeout);
        // 设置结果等级（中文仅支持complete）
        mIse.setParameter(SpeechConstant.RESULT_LEVEL, "plain");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIse.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        if(TextUtils.isEmpty(recordPath)) {
            recordPath = context.getExternalFilesDir("record/record.wav").getAbsolutePath();
//            recordPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/ise.wav";
        }
        mIse.setParameter(SpeechConstant.ISE_AUDIO_PATH, recordPath);
    }

    /**
     * 回调监听
     *
     * @param evaluatorCallback
     */
    public void setmEvaluatorCallback(EvaluatorCallback evaluatorCallback) {
        this.evaluatorCallback = evaluatorCallback;
    }

//    /**
//     * 设置录音保存路径
//     * @param path
//     */
//    public void setRecordPath(String path) {
//        recordPath = path;
//    }
//
//    /**
//     * 获取录音文件路径
//     * @return
//     */
//    public String getRecordPath() {
//        return recordPath;
//    }

    /**
     * 设置评测语言（TYPE_LANGUAGE_CN，TYPE_LANGUAGE_EN）
     * @param language
     */
/*    public void setLanguage(String language) {
        // 设置评测语言
        mIse.setParameter(SpeechConstant.LANGUAGE, language);
    }*/

    /**
     * 设置评测类型（TYPE_CATEGORY_SENTENCE, TYPE_CATEGORY_WORD）
     * @param category
     */
/*    public void setCategory(String category) {
        // 设置需要评测的类型
        mIse.setParameter(SpeechConstant.ISE_CATEGORY, category);
    }*/

    /**
     * 设置超时
     * @param timeout   毫秒
     */
/*    public void setTimeOut(String timeout) {
        // 语音输入超时时间，即用户最多可以连续说多长时间
        mIse.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, timeout);
    }*/

    /**
     * 开始
     *
     * @param content
     * @param evaluatorCallback 结果回调
     */
    public void start(String content, EvaluatorCallback evaluatorCallback) {
        this.evaluatorCallback = evaluatorCallback;
        mIse.startEvaluating(content, null, mEvaluatorListener);
    }

    /**
     * 取消
     */
    public void cancel() {
        mIse.cancel();
    }

    /**
     * 停止
     */
    public void stop() {
        if (mIse.isEvaluating()) {
            Log.e("isstop", "stop");
            mIse.stopEvaluating();
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        mIse.destroy();
    }

    /**
     * 评测监听接口
     */
    private EvaluatorListener mEvaluatorListener = new EvaluatorListener() {

        @Override
        public void onResult(EvaluatorResult result, boolean isLast) {
            Log.e("onResult", "evaluator result :" + isLast);
            Log.e("onResult", "evaluator result :" + result.getResultString());
            // isLast为true为最后结果
            if (isLast) {
                String resultXml = result.getResultString();
                if (!TextUtils.isEmpty(resultXml)) {
                    // 有评测结果
                    EvaluationResultParser parser = new EvaluationResultParser();
                    EvaluationResultParser.FinalResult finalResult = parser.parse(resultXml);
                    if (finalResult != null) {
                        if (evaluatorCallback != null) {
                            // 分值0-5换算成0-100
                            evaluatorCallback.onResult((int)(finalResult.total_score * 20));
                        }
                    } else {
                        if (evaluatorCallback != null) {
                            evaluatorCallback.onError("无评测结果，请重新评测！");
                        }
                    }

                } else {
                    // 无评测结果
                    if (evaluatorCallback != null) {
                        evaluatorCallback.onError("无评测结果，请重新评测！");
                    }
                }

            }
        }

        @Override
        public void onError(SpeechError error) {
            if (error != null) {
                Log.e("SpeechError", error.getErrorDescription());
                if (evaluatorCallback != null) {
                    evaluatorCallback.onError(error.getErrorDescription());
                }
            } else {
                if (evaluatorCallback != null) {
                    evaluatorCallback.onError("评测出错了！");
                }
            }
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
//            Log.e("onBeginOfSpeech", "evaluator begin");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
//            Log.e("onEndOfSpeech", "evaluator stoped");
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            // 音量值0-30，data音频数据
            Log.e("onVolumeChanged", "当前音量：" + volume);
            if (evaluatorCallback != null) {
                evaluatorCallback.onVolumeChanged(volume, data);
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
//            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
//                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
//                Log.e("onEvent", "session id =" + sid);
//            }
        }

    };

    public interface EvaluatorCallback {
        /**
         * 音量变化
         *
         * @param volume volume音量值，范围0-30
         * @param data   音频数据
         */
        void onVolumeChanged(int volume, byte[] data);

        /**
         * 得分
         *
         * @param score 分值，范围0-100
         */
        void onResult(int score);

        void onError(String error);
    }
}
