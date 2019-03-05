package com.tencent.taidemo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.taisdk.TAIError;
import com.tencent.taisdk.TAIMathCorrection;
import com.tencent.taisdk.TAIMathCorrectionCallback;
import com.tencent.taisdk.TAIManager;
import com.tencent.taisdk.TAIMathCorrectionItem;
import com.tencent.taisdk.TAIMathCorrectionParam;
import com.tencent.taisdk.TAIMathCorrectionRet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;

public class MathCorrectionActivity extends AppCompatActivity {
    private static final int FROM_CAMERA = 1;
    private static final int FROM_LIBRARY = 2;
    private ImageView imageView;
    private Bitmap bitmap;
    private TextView textView;
    private RelativeLayout relativeLayout;
    private Uri imageUri;
    private String imageAbsolutePath;
    private ProgressBar progressBar;
    private TAIMathCorrection correction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mathcorrection);
        this.imageView = findViewById(R.id.imageView);
        this.textView = findViewById(R.id.formula);
        this.relativeLayout = findViewById(R.id.relativeLayout);
        this.progressBar = findViewById(R.id.progressBar);
        this.correction = new TAIMathCorrection();
        String version = TAIManager.getVersion();
    }

    public void onPick(View view)
    {
        this.requestPermission();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择图片");
        builder.setItems(new String[]{"图库", "相机"}, new android.content.DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        pickFromLibrary();
                        break;
                    case 1:
                        pickFromCamera();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.create().show();
    }

    public void onRecognize(View view)
    {
        TAIMathCorrectionParam param = new TAIMathCorrectionParam();
        param.context = this;
        param.sessionId = UUID.randomUUID().toString();
        param.appId = PrivateInfo.appId;
        param.secretId = PrivateInfo.secretId;
        param.secretKey = PrivateInfo.secretKey;
        if(this.bitmap == null){
            return;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(this.bitmap.getByteCount());
        this.bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        param.imageData = outputStream.toByteArray();

        this.progressBar.setVisibility(View.VISIBLE);
        this.correction.mathCorrection(param, new TAIMathCorrectionCallback() {
            @Override
            public void onError(TAIError error) {
                MathCorrectionActivity.this.progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MathCorrectionActivity.this, error.desc, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(final TAIMathCorrectionRet result) {

                    MathCorrectionActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            MathCorrectionActivity.this.progressBar.setVisibility(View.INVISIBLE);
                            for (final TAIMathCorrectionItem item : result.items){
                                Rect rect = MathCorrectionActivity.this.converRect(item.rect);
                                View view = new View(MathCorrectionActivity.this);
                                view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        MathCorrectionActivity.this.textView.setText(item.formula);
                                    }
                                });
                                MathCorrectionActivity.this.relativeLayout.addView(view);
                                ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(view.getLayoutParams());
                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
                                layoutParams.width = rect.right - rect.left;
                                layoutParams.height = rect.bottom - rect.top;
                                layoutParams.leftMargin = rect.left;
                                layoutParams.topMargin = rect.top;
                                if(item.result){
                                    view.setBackgroundResource(R.drawable.shape_green_border);
                                }
                                else{
                                    view.setBackgroundResource(R.drawable.shape_red_border);
                                }
                                view.setLayoutParams(layoutParams);
                            }
                        }
                    });
            }
        });
    }

    private void pickFromCamera() {
        File outputImage = new File(getExternalCacheDir(), "image.jpg");
        this.imageAbsolutePath = outputImage.getAbsolutePath();
        if (outputImage.exists()) {
            outputImage.delete();
        }
        try {
            outputImage.createNewFile();
        } catch (Exception e) {
        }
        if (Build.VERSION.SDK_INT >= 24) {
            this.imageUri = FileProvider.getUriForFile(getApplicationContext(), "com.tencent.taidemo.provider", outputImage);
        } else {
            this.imageUri = Uri.fromFile(outputImage);
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, FROM_CAMERA);
    }

    private void pickFromLibrary()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, FROM_LIBRARY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == FROM_CAMERA && resultCode == RESULT_OK){
            try{
                InputStream is = getContentResolver().openInputStream(imageUri);
                this.bitmap = BitmapFactory.decodeStream(is);
                ExifInterface exif = new ExifInterface(this.imageAbsolutePath);
                int angle = 0;
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation){
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        angle = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        angle = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        angle = 270;
                        break;
                }
                Matrix m = new Matrix();
                m.setRotate(angle);
                this.bitmap = Bitmap.createBitmap(this.bitmap, 0, 0, this.bitmap.getWidth(), this.bitmap.getHeight(), m, true);
                imageView.setImageBitmap(bitmap);
            }
            catch(Exception e){
                Log.i("", "onActivityResult: ");
            }
        }
        else if(requestCode == FROM_LIBRARY && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if(cursor != null){
                cursor.moveToFirst();
                String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                String orientation = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
                cursor.close();
                if(imagePath != null){
                    this.bitmap = BitmapFactory.decodeFile(imagePath);
                    int angle = 0;
                    if(orientation != null && !"".equals(orientation)){
                        angle = Integer.parseInt(orientation);
                    }
                    if(angle != 0){
                        Matrix m = new Matrix();
                        m.setRotate(angle);
                        this.bitmap = Bitmap.createBitmap(this.bitmap, 0, 0, this.bitmap.getWidth(), this.bitmap.getHeight(), m, true);
                    }
                    imageView.setImageBitmap(this.bitmap);
                }
            }
        }

        this.textView.setText(R.string.formula_placeholder);
        this.relativeLayout.removeAllViews();
    }

    private void requestPermission()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){

            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private Rect converRect(Rect rect)
    {
        Rect drawRect = new Rect();
        this.imageView.getHitRect(drawRect);
        float scale = 0;
        float xoffset = drawRect.left;
        float yoffset = drawRect.top;
        float width = this.bitmap.getWidth();
        float height = this.bitmap.getHeight();
        if(height / width > drawRect.height() / drawRect.width()){
            scale = height / drawRect.height();
            xoffset += (int)((drawRect.width() - width / scale) * 0.5);
        }
        else{
            scale = width / drawRect.width();
            yoffset += (int)((drawRect.height() - height / scale) * 0.5);
        }
        int l = (int)(xoffset + rect.left / scale);
        int t = (int)(yoffset + rect.top / scale);
        int r = l + (int)(rect.width() / scale);
        int b = t + (int)(rect.height() / scale);

        return new Rect(l, t, r, b);
    }


}



