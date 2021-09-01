package com.codeshot.carscustomerapp.starting;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.codeshot.carscustomerapp.HomeActivity;
import com.codeshot.carscustomerapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;


public class SplashActivity extends AppCompatActivity {
    private Animation animSplash;
    private ImageView logoSplash;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        initializations();
        animSplash.setDuration(3000);
        logoSplash.setAnimation(animSplash);
        Thread thread=new Thread(){
            @Override
            public void run() {
                try {
                    sleep(3000);
                    isLoginedIn();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

//        isLoginedIn();
    }

    private void initializations() {
        //Init View
        logoSplash = (ImageView) findViewById(R.id.imgLogoSplash);
        animSplash = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.anim_img_splash);
        //Init FireBase
        mAuth=FirebaseAuth.getInstance();

    }
    void isLoginedIn(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            FirebaseMessaging.getInstance().getToken()
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            String token = s;
//                Toast.makeText(MainActivity.this, "" + token, Toast.LENGTH_SHORT).show();
                            Toast.makeText(SplashActivity.this, "" + token, Toast.LENGTH_SHORT).show();
                        }
                    });
            sendToHomeActivity();

        }else                     sendLoginActivity();

    }

    private void sendLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(intent);
        fileList();
        finish();

    }
    private void sendToHomeActivity(){
        Intent intent=new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


//    private void sendCustomerHomeActivity() {
//        Intent intent = new Intent(this, CustomerHomeActivity.class);
//
//        startActivity(intent);
//        finish();
//    }

//    private void sendProviderActivity() {
//        Intent intent = new Intent(this, ProviderHomeActivity.class);
//        startActivity(intent);
//        finish();
//    }

//    private void checkUserValidation() {
//        final SharedPreferences sharedPreferences = getSharedPreferences("com.codeshot.homeperfect", Context.MODE_PRIVATE);
//        String valid = sharedPreferences.getString("valid", "");
//        assert valid != null;
//        if (valid.equals("1p")) {
//            sendProviderActivity();
//        } else if (valid.equals("1c")) {
//            sendCustomerHomeActivity();
//        } else sendLoginActivity();
//    }
}

