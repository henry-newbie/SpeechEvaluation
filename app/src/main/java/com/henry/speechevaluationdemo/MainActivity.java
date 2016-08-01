package com.henry.speechevaluationdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.henry.speechevaluation.SpeechEvaluationView;
import com.henry.speechevaluation.SpeechEvaluatorUtil;

public class MainActivity extends AppCompatActivity {

    SpeechEvaluationView speechEvaluationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speechEvaluationView = (SpeechEvaluationView) findViewById(R.id.sev_speech);
        speechEvaluationView.setContent(SpeechEvaluatorUtil.TYPE_LANGUAGE_EN, SpeechEvaluatorUtil.TYPE_CATEGORY_WORD, 5000, "hello");
    }
}
