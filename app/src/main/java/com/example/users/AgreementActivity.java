package com.example.users;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.soniceyes.R;

public class AgreementActivity extends Activity {

    AutoCompleteTextView agreements;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agreement);
        agreements=findViewById(R.id.autoCompleteTextView2);
        agreements.setFocusable(false);
    }

    @Override
    public void onBackPressed() {
       finish();
    }


}
