package com.example.renya.myapplication_ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton button =(ImageButton)findViewById(R.id.imageButton);
        Intent intent = new Intent(this,StartActivity.class);
        button.setOnClickListener();
    }
}
