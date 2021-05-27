package com.example.tileteamproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.SignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyPage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<My> arrayList;
    private ArrayList<String> mkeys = new ArrayList<>();
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    ImageView iv_google;
    TextView tv_name, tv_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypage_item);

        iv_google = findViewById(R.id.myprofile);
        tv_email = findViewById(R.id.profile_email);
        tv_name = findViewById(R.id.profile_text);

        //recyclerView = findViewById(R.id.alarmlist); // 아디 연결 마이페이지 오류나서 주석
        //recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화 마이페이지 오류나서 주석
        layoutManager = new LinearLayoutManager(this);
        //recyclerView.setLayoutManager(layoutManager);마이페이지 오류나서 주석
        arrayList = new ArrayList<>();

        database = FirebaseDatabase.getInstance();


        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        String googlename = signInAccount.getDisplayName();
        databaseReference = database.getReference("userfreeboard").child(googlename);
        if(signInAccount != null){
            tv_name.setText(signInAccount.getDisplayName());
            tv_email.setText(signInAccount.getEmail());
            String imageUrl = signInAccount.getPhotoUrl().toString();
            Glide.with(this).load(imageUrl).into(iv_google);
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String mkey = dataSnapshot.getKey();
                    My my = dataSnapshot.getValue(My.class);
                    arrayList.add(my);
                    mkeys.add(mkey);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("loginAfter", String.valueOf(error.toException()));
            }
        });
        adapter = new Adapter(arrayList,mkeys, this); {
        };
        //recyclerView.setAdapter(adapter); 마이페이지 오류나서 주석
    }
}