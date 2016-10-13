package com.android.myweibo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.android.myweibo.openapi.WBOpenAPIActivity;
/**
 * Created by mikez on 2016/9/11.
 */
public class HomeActivity extends StardardActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button AuthBtn=(Button)findViewById(R.id.authorizaton_button);
        AuthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,WBAuthActivity.class));
            }
        });
        Button open_API_btn=(Button)findViewById(R.id.feature_open_api);
        open_API_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,WBOpenAPIActivity.class));
            }
        });
    }
}
