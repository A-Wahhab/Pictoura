package com.example.pictoura;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        Objects.requireNonNull(getSupportActionBar()).hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_main);

        Button btnmain = findViewById(R.id.btn_signin);
        Button btnsignup = findViewById(R.id.btn_signuppage);
        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        btnsignup.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, sign_up.class);
            startActivity(i);
        });
        btnmain.setOnClickListener(view -> {
            if (TextUtils.isEmpty(username.getText().toString())) {
                username.setError("required");
            }
            if (TextUtils.isEmpty(password.getText().toString())) {
                password.setError("required");
            }
            String user= username.getText().toString().trim();
            Query checker = FirebaseDatabase.getInstance("https://pictoura-f4ae4-default-rtdb.asia-southeast1.firebasedatabase.app/").
                    getReference().child("Users").orderByChild("username").equalTo(user);
            checker.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String dbpassword = Objects.requireNonNull(snapshot.child(user).child("password").getValue()).toString();
                        if (dbpassword.equals(password.getText().toString().trim())) {
                            Intent i = new Intent(MainActivity.this, mainpage.class);
                            i.putExtra("username",user);
                            startActivity(i);
                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "User Invalid", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), "database failed", Toast.LENGTH_LONG).show();
                }
            });

        });


    }
}