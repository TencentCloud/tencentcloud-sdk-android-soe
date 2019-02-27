package com.tencent.taidemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private String[] activityTitles = {"数学批改", "口语评测"};
    private String[] activityName = {"com.tencent.taidemo.MathCorrectionActivity",
            "com.tencent.taidemo.OralEvaluationActivity"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.listView = this.findViewById(R.id.listView);
        this.listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, activityTitles));
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Class clz = Class.forName(MainActivity.this.activityName[position]);
                    MainActivity.this.startActivity(new Intent(MainActivity.this, clz));
                } catch (ClassNotFoundException e) {

                }
            }
        });
    }

}
