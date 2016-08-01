package com.henry.speechevaluationdemo;

import android.app.Application;

import com.henry.speechevaluation.SpeechEvaluatorUtil;

/**
 * Created by henry on 2016/8/1.
 */
public class SpeechApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SpeechEvaluatorUtil.createUtility(this, "5647101b");
    }
}
