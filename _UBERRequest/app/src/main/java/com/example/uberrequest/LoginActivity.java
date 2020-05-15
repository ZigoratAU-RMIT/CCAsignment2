package com.example.uberrequest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText emailId,password;
    Button btnSignIn;
    TextView tvSignIn;
    FirebaseAuth mFirebaseAuth;
    private  FirebaseAuth.AuthStateListener maAuthStateListener;

    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);


        mFirebaseAuth = FirebaseAuth.getInstance();
        emailId = findViewById(R.id.txtEditEmail);
        password = findViewById(R.id.txtEditPassword);
        btnSignIn = findViewById(R.id.btnLogin);
        tvSignIn = findViewById(R.id.txtViewSignUp);

        maAuthStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if(mFirebaseUser != null){
                    Toast.makeText(LoginActivity.this,"You are logged in",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(LoginActivity.this,MapsActivity.class));
                }
                else{
                    Toast.makeText(LoginActivity.this,"Please log in",Toast.LENGTH_LONG).show();
                }
            }
        };

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailId.getText().toString();
                String pwd  = password.getText().toString();
                if(email.isEmpty()){
                    emailId.setError("Please enter the email");
                    emailId.requestFocus();
                }
                else if(pwd.isEmpty()){
                    password.setError("Please set your password");
                    password.requestFocus();
                }
                else if(email.isEmpty() && pwd.isEmpty()){
                    Toast.makeText(LoginActivity.this,"Email and password are empty!",Toast.LENGTH_LONG).show();
                }
                else if(!(email.isEmpty() && pwd.isEmpty())){
                    mFirebaseAuth.createUserWithEmailAndPassword(email,pwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(LoginActivity.this,"Signed up Unsuccessfully. Please try again",Toast.LENGTH_LONG).show();
                            }
                            else{
                                startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(LoginActivity.this,"Error . Pleae try agina",Toast.LENGTH_LONG).show();
                }
            }
        });

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        });

//        button = (Button) findViewById(R.id.btnLogin);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openMapActivity();
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(maAuthStateListener);
    }

    //@Override
    //protected void onStop() {
        //super.onStop();
        //mFirebaseAuth.getInstance().signOut();
    //}

    //    public void openMapActivity(){
//        Intent intent = new Intent(this, MapsActivity.class);
//        startActivity(intent);
//    }
}