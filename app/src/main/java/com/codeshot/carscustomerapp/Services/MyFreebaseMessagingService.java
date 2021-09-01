package com.codeshot.carscustomerapp.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.codeshot.carscustomerapp.Common.Common;
import com.codeshot.carscustomerapp.Helpers.NotificationsHelper;
import com.codeshot.carscustomerapp.HomeActivity;
import com.codeshot.carscustomerapp.Models.Token;
import com.codeshot.carscustomerapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


public class MyFreebaseMessagingService extends FirebaseMessagingService {
    NotificationManager notificationManager;
    NotificationsHelper notificationsHelper;
    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        remoteMessage.getData();
        notificationManager=(NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationsHelper=new NotificationsHelper(getBaseContext());
        Map<String,String> data=remoteMessage.getData();
        String title=data.get("title");
        final String message=data.get("message");

        if (title.equals("Cancel")){
            Handler handler=new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFreebaseMessagingService.this,message,Toast.LENGTH_SHORT).show();
                }
            });
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                showCancelNotificationAPI26(message);
            else
                showCancelNotification(message);

        }else if (title.equals("Arrived")){
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                showArrivedNotificationAPI26(message);
            else
                showArrivedNotification(message);

        }else if (title.equals("start trip")){
                notificationsHelper.getManager().cancel(1);
        }else if (title.equals("drop here")){
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                showRatingNotificationAPI26(message);
            else
                showRatingNotification(message);

        }

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationAPI26(String body) {
        PendingIntent contentIntent=PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        Notification.Builder builder=notificationsHelper.getCarsNotification("Arrived",
                body,
                contentIntent,
                defaultSound);
        notificationsHelper.getManager().notify(1,builder.build());
    }
    private void showArrivedNotification(String body) {

        //This code only work for android api 25 and below
        //from android api 26 of higher i need create notification channel
        PendingIntent contentIntent=PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(getBaseContext());
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(contentIntent);
        notificationManager.notify(1,builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showCancelNotificationAPI26(String body) {
        PendingIntent contentIntent=PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder builder=notificationsHelper.getCarsNotification("Canceled",
                body,
                contentIntent,
                defaultSound);
        notificationsHelper.getManager().notify(3,builder.build());
    }
    private void showCancelNotification(String body) {

        //This code only work for android api 25 and below
        //from android api 26 of higher i need create notification channel
        PendingIntent contentIntent=PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(getBaseContext());
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Canceled")
                .setContentText(body)
                .setContentIntent(contentIntent);
        notificationManager.notify(3,builder.build());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showRatingNotificationAPI26(String body) {

        Intent homeIntent=new Intent(getBaseContext(), HomeActivity.class);
        homeIntent.putExtra("type","rating");
        homeIntent.putExtra("message",body);
        PendingIntent contentIntent=PendingIntent.getActivity(getBaseContext(),
                0,homeIntent,PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder builder=notificationsHelper.getCarsNotification("Drop Here",
                body,
                contentIntent,
                defaultSound);
        notificationsHelper.getManager().notify(2,builder.build());
    }
    private void showRatingNotification(String body) {
        Intent homeIntent=new Intent(getBaseContext(), HomeActivity.class);
        homeIntent.putExtra("type","rating");
        homeIntent.putExtra("message",body);
        PendingIntent contentIntent=PendingIntent.getActivity(getBaseContext(),
                0,homeIntent,PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(getBaseContext());
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Canceled")
                .setContentText(body)
                .setContentIntent(contentIntent);
        notificationManager.notify(2,builder.build());
    }




    @Override
    public void onNewToken(final String s) {
        super.onNewToken(s);
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        String newToken=s;
                        updateTokenToServer(newToken);//When have new token, i need update to our realtime db
                        SharedPreferences sharedPreferences =getSharedPreferences("com.codeshot.carscustomerapp", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString("token", s).apply();
                    }
                });
        Log.d("NEW_TOKEN",s);
    }
    private void updateTokenToServer(String newToken){
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference().child(Common.token_tbl);

        Token token=new Token(newToken);
        if (FirebaseAuth.getInstance().getCurrentUser()!=null)//if already login, must update Token
        {
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("Saved Token","Yesssssssssssssssssssssss");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("ERROR TOKEN",e.getMessage());
                }
            });
        }

    }
}
