package com.codeshot.carscustomerapp.starting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;


import com.codeshot.carscustomerapp.Common.Common;
import com.codeshot.carscustomerapp.Models.User;
import com.codeshot.carscustomerapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class SignUpActivity extends AppCompatActivity {

    //View
    private EditText edtUserName,edtEmail,edtPass,edtRePass,edtUserPhone;
    private SwitchMaterial switch_gender;
    private String gender;
    private Button btnRegister;
    private CoordinatorLayout coordinatorLayout;
    private ProgressBar progressBar;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference ridersRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/SFProText-Regular.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_signup);
        initializations();
        switch_gender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    gender="male";
                else gender="female";
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register(v);
            }
        });

    }

    private void Register(final View v) {

        final String userName,email,password,rePassword,phoneNumber;
        userName=edtUserName.getText().toString();
        email=edtEmail.getText().toString();
        password=edtPass.getText().toString();
        rePassword=edtRePass.getText().toString();
        phoneNumber=edtUserPhone.getText().toString();
        if (!switch_gender.isChecked()){
            Snackbar.make(v,"Select Your Gender please ...",Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (password.length()<8) {
            edtPass.setError("Password too short");
            return;
        }
        if (!password.equals(rePassword)){
            edtRePass.setError("passwords not sames");
            return;
        }
        //Register
        progressBar.setVisibility(View.VISIBLE);
        if (gender!=null){
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String userKey=mAuth.getCurrentUser().getUid();
                        User user=new User(userName,email,phoneNumber,gender);
                        ridersRef.child(userKey).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Snackbar.make(v,"Successful Register",Snackbar.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    sendToLoginActivity();
                                }else {
                                    progressBar.setVisibility(View.GONE);
                                    Snackbar.make(v,task.getException().getMessage(),Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(v,task.getException().getMessage(),Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }else Snackbar.make(v,"Gender Is null",Snackbar.LENGTH_SHORT).show();


    }

    private void sendToLoginActivity() {
        Intent intent =new Intent(SignUpActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializations() {

        //View
        edtUserName=(EditText)findViewById(R.id.edtUserNameReg);
        edtEmail=(EditText)findViewById(R.id.edtEmailReg);
        edtPass=(EditText)findViewById(R.id.edtPasswordReg);
        edtRePass=(EditText)findViewById(R.id.edtPasswordReg);
        edtUserPhone=(EditText)findViewById(R.id.edtUserPhoneReg);
        switch_gender=(SwitchMaterial)findViewById(R.id.switch_gender);
        btnRegister=(Button)findViewById(R.id.btnSignUp);
        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.signUpLayout);
        progressBar=(ProgressBar)findViewById(R.id.progress_circularReg);

        //FireBase
        mAuth=FirebaseAuth.getInstance();
        ridersRef = FirebaseDatabase.getInstance().getReference().child(Common.riders_tbl);

    }
}
