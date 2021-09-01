package com.codeshot.carscustomerapp.starting;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.codeshot.carscustomerapp.Common.Common;
import com.codeshot.carscustomerapp.HomeActivity;
import com.codeshot.carscustomerapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class LoginActivity extends AppCompatActivity {
    //View

    private EditText edtEmail,edtPass;
    private TextView tvForgotPassword,tvSignUp;
    private Button btnLogin;
    private ProgressBar progressBar;
    private AlertDialog waitingDialog;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference ridersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Before setContentView
//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("fonts/SFProText-Regular.otf")
//                .setFontAttrId(R.attr.fontPath)
//                .build());
        setContentView(R.layout.activity_login);
        initializations();
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {sendToForGotPasswordActivity();
            }
        });
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {sendToRegisterActivity();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }
    private void login() {
        final String email,password;
        email=edtEmail.getText().toString();
        password=edtPass.getText().toString();
        if (password.length()<8) {
            edtPass.setError("Password too short");
            return;
        }
        waitingDialog.show();
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String id=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    ridersRef.child(id)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        sendToHomeActivity();
                                        waitingDialog.dismiss();
                                    }else {
                                        Toast.makeText(LoginActivity.this,"You are Driver please login in Driver App",Toast.LENGTH_LONG).show();
                                        waitingDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                }else {
                    Toast.makeText(LoginActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    waitingDialog.dismiss();
                }
            }
        });
    }
    private void initializations() {
        //View
        edtEmail=(EditText)findViewById(R.id.edtEmailLog);
        edtPass=(EditText)findViewById(R.id.edtPassword);
        tvForgotPassword=(TextView)findViewById(R.id.tvForgotPasswordLog);
        tvSignUp=(TextView)findViewById(R.id.tvSignUpLog);
        edtEmail=(EditText)findViewById(R.id.edtEmailLog);
        
        btnLogin=(Button)findViewById(R.id.btnLogin);
        progressBar=(ProgressBar)findViewById(R.id.progress_circularLog);
        waitingDialog= new SpotsDialog.Builder().setContext(this).build();
        waitingDialog.setTitle("Waiting");
        waitingDialog.setMessage("waiting to log in Cars App");
        //FireBase
        mAuth=FirebaseAuth.getInstance();
        ridersRef = FirebaseDatabase.getInstance().getReference().child(Common.riders_tbl);
    }
    private void sendToRegisterActivity(){
        Intent intent=new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
    private void sendToHomeActivity(){
        Intent intent=new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
    private void sendToForGotPasswordActivity(){
        Intent intent=new Intent(this,ForgotPasswordActivity.class);
        startActivity(intent);
    }
}
