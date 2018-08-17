package com.example.csh.mykeyboard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NumKeyboardView numKeyboardView= (NumKeyboardView)findViewById(R.id.num_keyboard);
        NumKeyDisplayView numkeyDisplay= (NumKeyDisplayView)findViewById(R.id.numkey_display);
        numKeyboardView.setFocusDisplayView(numkeyDisplay);
        numKeyboardView.setDisplayViews(new NumKeyDisplayView[]{numkeyDisplay});
        numKeyboardView.setOnActionDoneListener(new NumKeyboardView.OnActionDoneListener() {
            @Override
            public void onDone() {
                Log.i("csh", "onDone: ");
            }
        });
    }
}
