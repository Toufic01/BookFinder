package com.example.bookshow;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import de.hdodenhof.circleimageview.CircleImageView;

public class Forntview extends AppCompatActivity {

    CircleImageView logo;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forntview);

        logo = findViewById(R.id.logo);

        YoYo.with(Techniques.FadeIn)
                .duration(9000)
                .playOn(logo);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        },7000);

    }
}
