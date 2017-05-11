package com.example.christopher.Business_Card_Music_Generator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;

import java.util.List;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
//import java.util.List;
//import android.util.Log;



public class MainActivity extends AppCompatActivity{

    private static final String TAG = "OCVSample::Activity";

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView2;
    private SurfaceHolder surfaceHolder2;
    boolean previewing = false;
    public static int ww = 1280;
    public static int hh = 720;
    public static int wwP = 1920;
    public static int hhP = 1080;
    private TextView textRes;
    public static Bitmap dataHold;
    public static boolean requestCapture = false;
    private Camera.PictureCallback mPicture;

    public static Bitmap accBmp;
    public static int appFlag = 0;

    // For permission request
    private static final int CAMERA_PERMISSIONS_REQUEST = 1;
    private static final int WRITE_Storage_PERMISSIONS_REQUEST = 1;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //mOpenCvCameraView.enableView();
//                    image = new Mat();
//                    img_p = new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Lock dthe app orientation
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        textRes = (TextView) findViewById(R.id.Res);


        ///// Request for permission
        int permissionCheck_camera = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA);
        int permissionCheck_write_external = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);


        if (permissionCheck_write_external != PackageManager.PERMISSION_GRANTED) {

            // request the permission.
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_Storage_PERMISSIONS_REQUEST);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }

        if (permissionCheck_camera != PackageManager.PERMISSION_GRANTED) {

            // request the permission.

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSIONS_REQUEST);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }



        ///////////// Request for permission done

        Camera fCamera = Camera.open();
        Camera.Parameters parameters = fCamera.getParameters();

        Button click = (Button)findViewById(R.id.Bbegin);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TakeThePicture.class);
                startActivity(intent);
            }
        });

        //Get the possible preview resolutions
        List<Camera.Size> sizesP = parameters.getSupportedPreviewSizes();

        //Get the possible picture resolutions
        List<Camera.Size> sizesI = parameters.getSupportedPictureSizes();

        //Picture and preview sizes we want
        Camera.Size s1 = fCamera.new Size(960,720);
        Camera.Size s1p = fCamera.new Size(320,240);
        Camera.Size s2 = fCamera.new Size(1440, 1080);
        Camera.Size s2p = fCamera.new Size(320, 240);

        if(sizesI.contains(s1) && sizesP.contains(s1p)) {
            //Set the picture resolution
            ww = sizesI.get(sizesI.indexOf(s1)).width;
            hh = sizesI.get(sizesI.indexOf(s1)).height;
            //Set the Preview Resolution
            wwP = sizesP.get(sizesP.indexOf(s1p)).width;
            hhP = sizesP.get(sizesP.indexOf(s1p)).height;
            textRes.setText("Picture Resolution Set to: " + Integer.toString(ww) + "x"+ Integer.toString(hh)+
                    "\nPreview Resolution Set to: " + Integer.toString(wwP) + "x"+ Integer.toString(hhP));
            appFlag = 1;
        }else if(sizesI.contains(s2) && sizesP.contains(s2p)) {
            int ww = sizesI.get(sizesI.indexOf(s2)).width;
            int hh = sizesI.get(sizesI.indexOf(s2)).height;
            wwP = sizesP.get(sizesP.indexOf(s2p)).width;
            hhP = sizesP.get(sizesP.indexOf(s2p)).height;
            textRes.setText("Picture Resolution Set to: " + Integer.toString(ww) + "x"+ Integer.toString(hh)+
                    "\nPreview Resolution Set to: " + Integer.toString(wwP) + "x"+ Integer.toString(hhP));
            appFlag = 2;
        }else{
            //We're not gonna deal with any other resolutions at the moment so disable the button
            textRes.setText("Camera not supported");
            click.setClickable(false);
        }

        for(int i=0;i<sizesI.size();i++) {
            int height = sizesI.get(i).height;
            int width = sizesI.get(i).width;
            Log.d("size: ", Integer.toString(width)+";"+Integer.toString(height));
        }

        for(int i=0;i<sizesP.size();i++) {
            int height = sizesP.get(i).height;
            int width = sizesP.get(i).width;
            Log.d("size: ", Integer.toString(width)+";"+Integer.toString(height));
        }



    }


    //// The callback method for Permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
    }

    /////// The callback method for Permission (end)


}
