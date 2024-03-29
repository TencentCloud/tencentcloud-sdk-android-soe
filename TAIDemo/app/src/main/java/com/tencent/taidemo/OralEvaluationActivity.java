package com.tencent.taidemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tencent.taisdk.TAIError;
import com.tencent.taisdk.TAIOralEvaluation;
import com.tencent.taisdk.TAIOralEvaluationData;
import com.tencent.taisdk.TAIOralEvaluationEvalMode;
import com.tencent.taisdk.TAIOralEvaluationFileType;
import com.tencent.taisdk.TAIOralEvaluationListener;
import com.tencent.taisdk.TAIOralEvaluationParam;
import com.tencent.taisdk.TAIOralEvaluationRet;
import com.tencent.taisdk.TAIOralEvaluationServerType;
import com.tencent.taisdk.TAIOralEvaluationStorageMode;
import com.tencent.taisdk.TAIOralEvaluationTextMode;
import com.tencent.taisdk.TAIOralEvaluationWorkMode;
import com.tencent.taisdk.TAIRecorderParam;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


public class OralEvaluationActivity extends AppCompatActivity {
    private static final String TAG = OralEvaluationActivity.class.getSimpleName();
    private TAIOralEvaluation oral;
    private EditText refText;
    private TextView logText;
    private Button recordBtn;
    private Button localRecordBtn;
    private RadioButton workOnceBtn;
    private RadioButton workStreamBtn;
    private RadioButton evalWordBtn;
    private RadioButton evalSentenceBtn;
    private RadioButton evalParagraphBtn;
    private RadioButton evalFreeBtn;
    private RadioButton storageDisableBtn;
    private RadioButton storageEnableBtn;
    private RadioButton typeEnglishBtn;
    private RadioButton typeChineseBtn;
    private RadioButton textModeNoramlBtn;
    private RadioButton textModePhonemeBtn;
    private EditText scoreCoeff;
    private EditText fragSize;
    private EditText vadInterval;
    private ProgressBar vadVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oralevaluation);
        this.refText = this.findViewById(R.id.refText);
        this.refText.setText("how are you");
        this.logText = this.findViewById(R.id.logText);
        logText.setMovementMethod(ScrollingMovementMethod.getInstance());
        this.recordBtn = this.findViewById(R.id.recordBtn);
        this.localRecordBtn = this.findViewById(R.id.localRecordBtn);
        this.workOnceBtn = this.findViewById(R.id.workOnceBtn);
        this.workStreamBtn = this.findViewById(R.id.workStreamBtn);
        this.workStreamBtn.setChecked(true);
        this.evalWordBtn = this.findViewById(R.id.evalWordBtn);
        this.evalSentenceBtn = this.findViewById(R.id.evalSentenceBtn);
        this.evalSentenceBtn.setChecked(true);
        this.evalParagraphBtn = this.findViewById(R.id.evalParagraphBtn);
        this.evalFreeBtn = this.findViewById(R.id.evalFreeBtn);
        this.storageDisableBtn = this.findViewById(R.id.storageDisable);
        this.storageEnableBtn = this.findViewById(R.id.storageEnable);
        this.storageDisableBtn.setChecked(true);
        this.typeEnglishBtn = this.findViewById(R.id.typeEnglish);
        this.typeChineseBtn = this.findViewById(R.id.typeChinese);
        this.typeEnglishBtn.setChecked(true);
        this.textModeNoramlBtn = this.findViewById(R.id.textModeNormal);
        this.textModeNoramlBtn.setChecked(true);
        this.textModePhonemeBtn = this.findViewById(R.id.textModePhoneme);

        this.scoreCoeff = this.findViewById(R.id.scoreCoeff);
        this.scoreCoeff.setText("1.0");

        this.fragSize = this.findViewById(R.id.fragSize);
        this.fragSize.setText("1.0");

        this.vadVolume = this.findViewById(R.id.vadVolume);

        this.vadInterval = this.findViewById(R.id.vadInterval);
        this.vadInterval.setText("5000");
        this.requestPermission();
    }


    public void onRecord(View view) {

        if (this.oral == null) {
            this.oral = new TAIOralEvaluation();
        }
        if (oral.isRecording()) {
            this.oral.stopRecordAndEvaluation();
            setResponse("stopRecordAndEvaluation");
            recordBtn.setText(R.string.start_record);
        } else {
            this.oral.setListener(new TAIOralEvaluationListener() {
                @Override
                public void onEvaluationData(final TAIOralEvaluationData data, final TAIOralEvaluationRet result) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            String retString = gson.toJson(result);
                            Log.w(TAG, "onEvaluationData isEnd: " + data.bEnd);
                            Log.d(TAG, "onEvaluationData onRecordretString: " + retString);
                            String tempRes = String.format("oralEvaluation:seq:%d, end:%d,  ret:%s", data.seqId, data.bEnd ? 1 : 0, retString);

                            setResponse(tempRes);
                            Log.w(TAG, "onEvaluationData tempRes:" + tempRes);
                        }
                    });
                }

                @Override
                public void onFinalEvaluationData(final TAIOralEvaluationData data, final TAIOralEvaluationRet result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            String retString = gson.toJson(result);
                            Log.d(TAG, "onFinalEvaluationData isEnd: " + data.bEnd);
                            Log.v(TAG, "onFinalEvaluationData onRecordretString: " + retString);
                            String tempRes = String.format("onFinalEvaluationData:seq:%d, end:%d,  ret:%s", data.seqId, data.bEnd ? 1 : 0, retString);

                            setResponse(tempRes);
                            Log.e(TAG, "onFinalEvaluationData tempRes:" + tempRes);
                        }
                    });
                }

                @Override
                public void onEvaluationError(TAIOralEvaluationData data, TAIError error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            OralEvaluationActivity.this.recordBtn.setText(R.string.start_record);
                            Gson gson = new Gson();
                            String retString = gson.toJson(error);
                            String errorStr = "onEvaluationError: " + retString + " seqId: " + data.seqId;
                            setResponse(errorStr);
                            Log.e(TAG, "onEvaluationError errorStr:" + errorStr);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    recordBtn.setText(R.string.start_record);
                                }
                            });
                        }
                    });

                }

                @Override
                public void onEndOfSpeech(boolean b) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            OralEvaluationActivity.this.setResponse("onEndOfSpeech");
                            OralEvaluationActivity.this.onRecord(null);
                        }
                    });
                }


                @Override
                public void onVolumeChanged(final int volume) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            OralEvaluationActivity.this.vadVolume.setProgress(volume);
                        }
                    });
                }
            });

            if (this.scoreCoeff.getText().toString().equals("")) {
                this.setResponse("startRecordAndEvaluation:scoreCoeff invalid");
                return;
            }
            if (this.fragSize.getText().toString().equals("")) {
                this.setResponse("startRecordAndEvaluation:fragSize invalid");
                return;
            }
            this.logText.setText("");
            TAIOralEvaluationParam param = new TAIOralEvaluationParam();
            param.context = this;
            param.sessionId = UUID.randomUUID().toString();
            param.appId = PrivateInfo.appId;
            param.soeAppId = PrivateInfo.soeAppId;
            param.secretId = PrivateInfo.secretId;
            param.secretKey = PrivateInfo.secretKey;
            param.token = PrivateInfo.token;
            int evalMode = TAIOralEvaluationEvalMode.SENTENCE;
            if (this.evalWordBtn.isChecked()) {
                evalMode = TAIOralEvaluationEvalMode.WORD;
            } else if (this.evalSentenceBtn.isChecked()) {
                evalMode = TAIOralEvaluationEvalMode.SENTENCE;
            } else if (this.evalParagraphBtn.isChecked()) {
                evalMode = TAIOralEvaluationEvalMode.PARAGRAPH;
            } else if (this.evalFreeBtn.isChecked()) {
                evalMode = TAIOralEvaluationEvalMode.FREE;
            }
            param.workMode = this.workOnceBtn.isChecked() ? TAIOralEvaluationWorkMode.ONCE : TAIOralEvaluationWorkMode.STREAM;
            param.evalMode = evalMode;
            param.storageMode = this.storageDisableBtn.isChecked() ? TAIOralEvaluationStorageMode.DISABLE : TAIOralEvaluationStorageMode.ENABLE;
            param.fileType = TAIOralEvaluationFileType.MP3;
            param.serverType = this.typeChineseBtn.isChecked() ? TAIOralEvaluationServerType.CHINESE : TAIOralEvaluationServerType.ENGLISH;
            param.textMode = this.textModeNoramlBtn.isChecked() ? TAIOralEvaluationTextMode.NORMAL : TAIOralEvaluationTextMode.PHONEME;
            param.scoreCoeff = Double.parseDouble(this.scoreCoeff.getText().toString());
            param.refText = this.refText.getText().toString();
            param.audioPath = this.getFilesDir() + "/" + param.sessionId + ".mp3";
            if (param.workMode == TAIOralEvaluationWorkMode.STREAM) {
                param.timeout = 5;
                param.retryTimes = 5;
            } else {
                param.timeout = 30;
                param.retryTimes = 0;
            }

//            param.evalMode = 2;
//            param.refText = "It was Mother’s Day. Tom stopped his car outside a flower shop. He wanted to order some beautiful flowers and ask " +
//                    "the shop to deliver them to his mother. When he was about to enter the shop, he saw a little girl crying on the roadside. " +
//                    "Tom walked to her and asked what happened. The girl told him she wanted to buy a rose for her mother, but she had no money." +
//                    " Hearing this, Tom took the girl’s hand and entered the flower shop. He ordered the flowers for his mother and bought a " +
//                    "beautiful rose for the girl. Then he offered to drive the girl home. But the girl said that she was not going home; " +
//                    "she was going to the bus station. Her mother worked in another town. She must go to her today with the rose. Tom was touched. " +
//                    "He changed his mind. After driving the girl to the bus station, he returned to the shop, canceled the delivery service and bought a " +
//                    "larger bunch of flowers and a box of chocolates. After Tom left the shop, he drove all the way to his mother’s place. " +
//                    "He would give the flowers to his mother in person.";
//            param.keyword = "It was Mother’s Day.| Tom went to a flower shop and wanted to ask the shop to send some flowers to his mother." +
//                    " | Tom saw a little girl crying on the roadside before he entered the shop. | The girl said she had no money to buy a rose for her mother. " +
//                    "| He bought a rose for the girl and offered to drive her home. | The girl was going to give the rose to her mother in another town. " +
//                    " | Tom was touched and changed his mind. | He returned to the flower shop after driving the girl to the bus station. " +
//                    "| He canceled the delivery service and bought more flowers and some chocolates. | He would give the flowers to his mother in person.";
//
//
//            param.evalMode = 5;
//            param.refText = "I need a hat. | I want to buy a hat. | Yes, please. I want a hat.";
//            param.keyword = "I need | hat # I want | buy | hat";

            TAIRecorderParam recordParam = new TAIRecorderParam();
            recordParam.fragSize = (int) (Double.parseDouble(this.fragSize.getText().toString()) * 1024);
            recordParam.fragEnable = !this.workOnceBtn.isChecked();
            recordParam.vadEnable = true;
            recordParam.vadInterval = Integer.parseInt(this.vadInterval.getText().toString());
            this.oral.setRecorderParam(recordParam);
            this.oral.startRecordAndEvaluation(param);
            OralEvaluationActivity.this.recordBtn.setText(R.string.stop_record);
        }
    }

    public void onLocalRecord(View view) {
        if (this.oral == null) {
            this.oral = new TAIOralEvaluation();
            this.oral.setListener(new TAIOralEvaluationListener() {
                @Override
                public void onEvaluationData(final TAIOralEvaluationData data, final TAIOralEvaluationRet result) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Gson gson = new Gson();
                            String retString = gson.toJson(result);
                            Log.d("onEvaluationData", "retString: " + retString);
                            String tempRes = String.format("oralEvaluation:seq:%d, end:%d, ret:%s", data.seqId, data.bEnd ? 1 : 0, retString);
                            OralEvaluationActivity.this.setResponse(tempRes);

                        }
                    });
                }

                @Override
                public void onFinalEvaluationData(TAIOralEvaluationData data, TAIOralEvaluationRet result) {

                }

                @Override
                public void onEvaluationError(TAIOralEvaluationData data, TAIError error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            OralEvaluationActivity.this.recordBtn.setText(R.string.start_record);
                            Gson gson = new Gson();
                            String retString = gson.toJson(error);
                            String errorStr = "onEvaluationError: " + retString + " seqId: " + data.seqId;
                            setResponse(errorStr);
                            Log.e(TAG, "onEvaluationError errorStr:" + errorStr);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    recordBtn.setText(R.string.start_record);
                                }
                            });
                        }
                    });
                }

                @Override
                public void onEndOfSpeech(boolean isSpeak) {

                }

                @Override
                public void onVolumeChanged(int volume) {

                }
            });
        }
        this.logText.setText("");
        TAIOralEvaluationParam param = new TAIOralEvaluationParam();
        param.context = this;
        param.sessionId = UUID.randomUUID().toString();
        param.appId = PrivateInfo.appId;
        param.soeAppId = PrivateInfo.soeAppId;
        param.secretId = PrivateInfo.secretId;
        param.secretKey = PrivateInfo.secretKey;
        param.workMode = TAIOralEvaluationWorkMode.ONCE;
        param.evalMode = TAIOralEvaluationEvalMode.SENTENCE;
        param.storageMode = TAIOralEvaluationStorageMode.DISABLE;
        param.fileType = TAIOralEvaluationFileType.MP3;
        param.serverType = TAIOralEvaluationServerType.ENGLISH;
        param.textMode = TAIOralEvaluationTextMode.NORMAL;
        param.scoreCoeff = Double.parseDouble(this.scoreCoeff.getText().toString());
        param.refText = "hello guagua";

        try {
            InputStream is = getAssets().open("hello_guagua.mp3");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            TAIOralEvaluationData data = new TAIOralEvaluationData();
            data.seqId = 1;
            data.bEnd = true;
            data.audio = buffer;
            this.oral.oralEvaluation(param, data);
        } catch (Exception e) {

        }


    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1234);
        }
    }

    private void setResponse(String rsp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
        String date = format.format(new Date(System.currentTimeMillis()));
        String newS = String.format("%s %s", date, rsp);
        String old = this.logText.getText().toString();
        this.logText.setText(String.format("%s\n%s", old, newS));
    }
}
