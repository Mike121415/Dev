package com.android.myweibo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

/**
 * Created by mikez on 2016/9/29.
 */
public class StardardActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Activity Id","Task id is"+getTaskId());
        Log.d("BaseActivity",getClass().getSimpleName());
      //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.standard_activity_layout);


    }
}
