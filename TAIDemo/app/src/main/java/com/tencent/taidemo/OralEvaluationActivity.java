package com.tencent.taidemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tencent.taisdk.TAIErrCode;
import com.tencent.taisdk.TAIError;
import com.tencent.taisdk.TAIOralEvaluation;
import com.tencent.taisdk.TAIOralEvaluationCallback;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


public class OralEvaluationActivity extends AppCompatActivity {
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

        if(this.oral == null){
            this.oral = new TAIOralEvaluation();
        }
        if(oral.isRecording()){
            this.oral.stopRecordAndEvaluation(new TAIOralEvaluationCallback() {
                @Override
                public void onResult(final TAIError error) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            String string = gson.toJson(error);
                            OralEvaluationActivity.this.setResponse(String.format("stopRecordAndEvaluation:%s", string));
                            OralEvaluationActivity.this.recordBtn.setText(R.string.start_record);
                        }
                    });
                }
            });
        }
        else{
            this.oral.setListener(new TAIOralEvaluationListener() {
                @Override
                public void onEvaluationData(final TAIOralEvaluationData data, final TAIOralEvaluationRet result, final TAIError error) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(error.code != TAIErrCode.SUCC){
                                OralEvaluationActivity.this.recordBtn.setText(R.string.start_record);
                            }
                            Gson gson = new Gson();
                            String errString = gson.toJson(error);
                            String retString = gson.toJson(result);
                            OralEvaluationActivity.this.setResponse(String.format("oralEvaluation:seq:%d, end:%d, error:%s, ret:%s", data.seqId, data.bEnd ? 1 : 0, errString, retString));
                        }
                    });
                }

                @Override
                public void onEndOfSpeech() {
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

            if(this.scoreCoeff.getText().toString().equals("")){
                this.setResponse("startRecordAndEvaluation:scoreCoeff invalid");
                return;
            }
            if(this.fragSize.getText().toString().equals("")){
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
            if(this.evalWordBtn.isChecked()){
                evalMode = TAIOralEvaluationEvalMode.WORD;
            }
            else if(this.evalSentenceBtn.isChecked()){
                evalMode = TAIOralEvaluationEvalMode.SENTENCE;
            }
            else if(this.evalParagraphBtn.isChecked()){
                evalMode = TAIOralEvaluationEvalMode.PARAGRAPH;
            }
            else if(this.evalFreeBtn.isChecked()){
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
            if(param.workMode == TAIOralEvaluationWorkMode.STREAM){
                param.timeout = 5;
                param.retryTimes = 5;
            }
            else{
                param.timeout = 30;
                param.retryTimes = 0;
            }
            TAIRecorderParam recordParam = new TAIRecorderParam();
            recordParam.fragSize = (int)(Double.parseDouble(this.fragSize.getText().toString()) * 1024);
            recordParam.fragEnable = !this.workOnceBtn.isChecked();
            recordParam.vadEnable = true;
            recordParam.vadInterval = Integer.parseInt(this.vadInterval.getText().toString());
            this.oral.setRecorderParam(recordParam);
            this.oral.startRecordAndEvaluation(param, new TAIOralEvaluationCallback() {
                @Override
                public void onResult(final TAIError error) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(error.code == TAIErrCode.SUCC){
                                OralEvaluationActivity.this.recordBtn.setText(R.string.stop_record);
                            }
                            Gson gson = new Gson();
                            String string = gson.toJson(error);
                            OralEvaluationActivity.this.setResponse(String.format("startRecordAndEvaluation:%s", string));
                        }
                    });
                }
            });
        }
    }

    public void onLocalRecord(View view)
    {
        if(this.oral == null){
            this.oral = new TAIOralEvaluation();
            this.oral.setListener(new TAIOralEvaluationListener() {
                @Override
                public void onEvaluationData(final TAIOralEvaluationData data, final TAIOralEvaluationRet result, final TAIError error) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(error.code != TAIErrCode.SUCC){
                                OralEvaluationActivity.this.recordBtn.setText(R.string.start_record);
                            }
                            Gson gson = new Gson();
                            String errString = gson.toJson(error);
                            String retString = gson.toJson(result);
                            OralEvaluationActivity.this.setResponse(String.format("oralEvaluation:seq:%d, end:%d, error:%s, ret:%s", data.seqId, data.bEnd ? 1 : 0, errString, retString));
                        }
                    });
                }

                @Override
                public void onEndOfSpeech() {

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

        try{
            InputStream is = getAssets().open("hello_guagua.mp3");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            TAIOralEvaluationData data = new TAIOralEvaluationData();
            data.seqId = 1;
            data.bEnd = true;
            data.audio = buffer;
            this.oral.oralEvaluation(param, data, new TAIOralEvaluationCallback() {
                @Override
                public void onResult(final TAIError error) {
                    OralEvaluationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            String string = gson.toJson(error);
                            OralEvaluationActivity.this.setResponse(String.format("oralEvaluation:%s", string));
                        }
                    });
                }
            });
        }
        catch (Exception e){

        }


    }

    private void requestPermission()
    {
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
