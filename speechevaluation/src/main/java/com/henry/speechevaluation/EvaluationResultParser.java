/**
 *
 */
package com.henry.speechevaluation;

import android.text.TextUtils;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 解析科大讯飞语音评测的结果，数据结构是xml形式的
 *
 */
public class EvaluationResultParser {

    public FinalResult parse(String xml) {
        if (TextUtils.isEmpty(xml)) {
            return null;
        }

        XmlPullParser pullParser = Xml.newPullParser();

        try {
            InputStream ins = new ByteArrayInputStream(xml.getBytes());
            pullParser.setInput(ins, "utf-8");
            FinalResult finalResult = null;

            int eventType = pullParser.getEventType();
            while (XmlPullParser.END_DOCUMENT != eventType) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("FinalResult".equals(pullParser.getName())) {
                            // 只有一个总分的结果
                            finalResult = new FinalResult();
                        } else if ("ret".equals(pullParser.getName())) {
                            finalResult.ret = getInt(pullParser, "value");
                        } else if ("total_score".equals(pullParser.getName())) {
                            finalResult.total_score = getFloat(pullParser, "value");
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        if ("FinalResult".equals(pullParser.getName())) {
                            return finalResult;
                        }
                        break;

                    default:
                        break;
                }
                eventType = pullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 根据属性名获取int值
     * @param pullParser
     * @param attrName
     * @return
     */
    private int getInt(XmlPullParser pullParser, String attrName) {
        String val = pullParser.getAttributeValue(null, attrName);
        if (null == val) {
            return 0;
        }
        return Integer.parseInt(val);
    }

    /**
     * 根据属性名获取float值
     * @param pullParser
     * @param attrName
     * @return
     */
    private float getFloat(XmlPullParser pullParser, String attrName) {
        String val = pullParser.getAttributeValue(null, attrName);
        if (null == val) {
            return 0f;
        }
        return Float.parseFloat(val);
    }

    public class FinalResult {

        public int ret;             // 0表示评测正常

        public float total_score;   // 总分，分值0-5，满分5分

        @Override
        public String toString() {
            return "返回值：" + ret + "，总分：" + total_score;
        }
    }
}
