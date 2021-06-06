package com.example.tileteamproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class test extends AppCompatActivity {
    private DatabaseReference mDatabase;
    Button butt_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        butt_start = findViewById(R.id.butt_start);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        String googlename = signInAccount.getDisplayName();

        Timer timer = new Timer();
        butt_start.setOnClickListener(new View.OnClickListener() {//버튼 이벤트 처리

            @Override
            public void onClick(View view) {
                TimerTask TT = new TimerTask() {
                    @Override
                    public void run() {
                        long now = System.currentTimeMillis();
                        Date mDate = new Date(now);
                        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMdd");
                        String time = simpleDate.format(mDate);

                        mDatabase.child(googlename).child(time).child("위도"+time).setValue("위도");
                        mDatabase.child(googlename).child(time).child("경도"+time).setValue("경도");
                    }
                };

                timer.schedule(TT,0,3000);
            }
        });
    }
}
