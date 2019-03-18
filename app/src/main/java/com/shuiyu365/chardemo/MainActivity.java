package com.shuiyu365.chardemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    CharView charview;
    List<String> horizontalList;
    List<Float> verticalList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        charview = findViewById(R.id.charview);
        horizontalList = new ArrayList<>();
        verticalList = new ArrayList<>();
        for(int i = 0; i< 20;i++){
            horizontalList.add( ( i + 1 ) + "周");

            switch (i%4){
                case 0:
                    verticalList.add(40f);
                    break;
                case 1:
                    verticalList.add(0f);
                    break;

                case 2:
                    verticalList.add(10f);
                    break;

                case 3:
                    verticalList.add(0f);
                    break;
            }
        }
        charview.setHorizontalList(horizontalList);
        charview.setVerticalList(verticalList);
    }
}
