package com.gusya.mv.simpleadidapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.gusya.mv.simpleadid.R;
import com.gusya.mv.simpleadid.SimpleAdId;

/**
 * Created by Gusya on 29/04/2017.
 */

public class MainActivity extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        final TextView adIdTv = (TextView)findViewById(R.id.main_activity_tv_adid);
        final TextView flagTv = (TextView)findViewById(R.id.main_activity_tv_limitedflag);

        SimpleAdId.getAdInfo(getApplicationContext(), new SimpleAdId.SimpleAdListener() {

            @Override
            public void onSuccess(SimpleAdId.AdIdInfo info) {
                adIdTv.setText("Ad ID: "+info.getAdId());
                flagTv.setText("Limited tracking: "+info.isAdTrackingEnabled());
            }

            @Override
            public void onException(Exception exception) {
                adIdTv.setText("Exception: "+exception.getMessage());
            }
        });
    }
}
