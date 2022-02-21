package com.example.pictoura;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class sign_up extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        Objects.requireNonNull(getSupportActionBar()).hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_sign_up);


        FirebaseDatabase database = FirebaseDatabase.getInstance("https://pictoura-f4ae4-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference myRef = database.getReference("Users");


        Button b = findViewById(R.id.btn_signup);
        EditText email = findViewById(R.id.email);
        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        EditText repassword = findViewById(R.id.re_password);
        b.setOnClickListener(view -> {
            String saveemail = email.getText().toString().trim();
            String saveusername = username.getText().toString().trim();
            String savepassword = password.getText().toString().trim();
            String checkpassword = repassword.getText().toString().trim();
            if(TextUtils.isEmpty(saveemail)) {
                email.setError("required");
            }
            if(TextUtils.isEmpty(checkpassword)) {
                repassword.setError("required");
            }
            if(TextUtils.isEmpty(savepassword)) {
                password.setError("required");
            }
            if(TextUtils.isEmpty(saveusername)) {
                username.setError("required");
            }
            else {
                if(savepassword.equals(checkpassword)){
                    myRef.child(saveusername).child("username").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"User Already Exists",Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(sign_up.this, MainActivity.class);
                            startActivity(i);
                        }
                        else {
                            myRef.child(saveusername).child("username").setValue(saveusername);
                            myRef.child(saveusername).child("password").setValue(savepassword);
                            myRef.child(saveusername).child("email").setValue(saveemail);
                            myRef.child(saveusername).child("profilepath").setValue(" ");

                            myRef.child(saveusername).child("followers").setValue("[]");

                            Intent i = new Intent(sign_up.this, mainpage.class);
                            i.putExtra("username", saveusername);
                            startActivity(i);
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(), "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}