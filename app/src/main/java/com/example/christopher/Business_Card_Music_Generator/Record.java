package com.example.christopher.Business_Card_Music_Generator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by jayli on 5/2/2017.
 */

public class Record extends AppCompatActivity {

    final static int F_S = 48000 ;
    public static int N = 2*F_S ; // Recording 2 secs of samples
    public static short[] sig = new short[N];

    final public int MIC_PERMISSIONS_REQUEST = 1 ;
    AudioRecord recorder ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record);


        int permissionCheck_mic = ContextCompat.checkSelfPermission(Record.this,
                Manifest.permission.RECORD_AUDIO);

        if (permissionCheck_mic != PackageManager.PERMISSION_GRANTED) {

            // request the permission.

            ActivityCompat.requestPermissions(Record.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSIONS_REQUEST);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }


    }


    public void record(View v){

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, F_S, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N*10);
        recorder.startRecording();

        recorder.read(sig, 0, N);

        recorder.stop();
        recorder.release();

        Context context = getBaseContext() ;
        int duration = Toast.LENGTH_SHORT;
        CharSequence text_before = "Done Recording! ";

        Toast toast1 = Toast.makeText(context, text_before, duration);
        toast1.show();

        new Handler().postDelayed(new Runnable() {
//            private static final String TAG = "Display signal short: " ;

            @Override
            public void run() {
                Intent intent = new Intent(Record.this, Human_display.class);
                startActivity(intent);

            }
        },2000) ;
    }


    //// The callback method for Permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
//        if (requestCode != CAMERA_PERMISSIONS_REQUEST) {
//
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
    }

    /////// The callback method for Permission (end)



}
