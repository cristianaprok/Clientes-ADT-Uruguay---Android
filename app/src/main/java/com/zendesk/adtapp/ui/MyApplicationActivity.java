package com.zendesk.adtapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.zendesk.adtapp.R;

/**
 * Created by rahulramachandra on 09/10/17.
 */

public class MyApplicationActivity extends AppCompatActivity {
    Button sucursalBtn,adtfinduBtn,adtsmartBtn,adtGoBtn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list_activity);
        sucursalBtn = (Button) findViewById(R.id.sucursal_btn);
        sucursalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ForgotPasswordActivity.class);
                intent.putExtra("IS_PASSWORD", false);
                startActivity(intent);
            }
        });
        adtfinduBtn = (Button) findViewById(R.id.adt_findu_btn);
        adtfinduBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppLink("ar.com.localizart.android.report");
            }
        });
        adtsmartBtn = (Button) findViewById(R.id.adt_ss_btn);
        adtsmartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppLink("com.adtar.ch.fd.mobile.android.adt_ar_prod");
            }
        });
        adtGoBtn = (Button) findViewById(R.id.adt_go_btn);
        adtGoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppLink("com.visonic.ADTArGo");
            }
        });

    }
    public void openAppLink(String link){
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + link));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (android.content.ActivityNotFoundException anfe) {

        }
    }
}
