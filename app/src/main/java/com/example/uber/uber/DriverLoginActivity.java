package com.example.uber.uber;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginActivity extends AppCompatActivity {
    private EditText mEmail, mPassword;
    private Button  btn_Login, btn_Registration;

    private FirebaseAuth mAut;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        mAut= FirebaseAuth.getInstance();

        firebaseAuthListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(DriverLoginActivity.this, DriverMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };
        
        mEmail= (EditText) findViewById(R.id.email);
        mPassword= (EditText) findViewById(R.id.password);

        btn_Login= (Button) findViewById(R.id.login);
        btn_Registration= (Button) findViewById(R.id.registration);

        // when click on register button

        btn_Registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                mAut.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(DriverLoginActivity.this, "Sign up Error", Toast.LENGTH_SHORT).show();
                        } else {
                            String user_id = mAut.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
                            current_user_db.setValue(true);


                        }
                    }
                });
            }
        });

        // when click on login button

        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                mAut.signInWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(DriverLoginActivity.this, "Sign in Error", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAut.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAut.removeAuthStateListener(firebaseAuthListener);
    }
}