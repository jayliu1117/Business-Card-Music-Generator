package com.example.christopher.Business_Card_Music_Generator;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import android.graphics.Canvas;
import android.hardware.Camera.PreviewCallback;
import android.graphics.Rect;
import android.graphics.Bitmap;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.view.View;
import android.widget.Button;
import android.graphics.Matrix;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
//import android.util.Log;



public class TakeThePicture extends AppCompatActivity implements SurfaceHolder.Callback{

    private static final String TAG = "OCVSample::Activity";

    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView2;
    private SurfaceHolder surfaceHolder2;
    boolean previewing = false;
    public static int w = 1280;
    public static int h = 720;
    public static int picw = 1920;
    public static int pich = 1080;
    private Camera.PictureCallback mPicture;
    private int left;
    private int right;
    private int top;
    private int bottom;
    public static int step = 20;
    private boolean rightDetected = false;
    private boolean leftDetected = false;
    private boolean topDetected = false;
    private boolean bottomDetected = false;
    private boolean detected = false;
    private boolean picTaken = false;
    private int detectCount = 0;

    public static Bitmap accBmp;

    //Load OpenCv
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
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

    public TakeThePicture() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_the_picture);
        // Lock down the app orientation
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        surfaceView = (SurfaceView)findViewById(R.id.ViewOrigin);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView2 = (SurfaceView)findViewById(R.id.ViewHisteq);
        surfaceHolder2 = surfaceView2.getHolder();
        pich = MainActivity.hh;
        picw = MainActivity.ww;
        w = MainActivity.wwP;
        h = MainActivity.hhP;

        //Determine the step size based on the resolution of the picture
        if(MainActivity.appFlag == 2){
            step = 40;
        }else{
            step =  20;
        }

        //Used for determining the location of the sides of the frame
        left = step;
        right = h - step;
        top = (int)((double)w/2-(double)(right-left)*4.0/14.0);
        bottom = (int)((double)w/2+(double)(right-left)*4.0/14.0);
        detected = false;

        //Flags for when the business card is within the frame
        rightDetected = false;
        leftDetected = false;
        topDetected = false;
        bottomDetected = false;

        //Flag for when the picture is taken
        picTaken = false;

        Button click = (Button)findViewById(R.id.Bcapture);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(null, null, mPicture);
            }
        });

        mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                detected = false;
                picTaken = true;
                Matrix matrix = new Matrix();
                matrix.postRotate(90);

                //Store data from picture in a bitmap and rotate the iage
                accBmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                accBmp = Bitmap.createBitmap(accBmp, 0, 0, picw, pich, matrix, true);

                //Launch new activity
                Intent intent = new Intent(TakeThePicture.this, display.class);
                startActivity(intent);
            }
        };

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        Context context = getApplicationContext();
//        SurfaceView dummy = new SurfaceView(context);
        if(!previewing) {
            camera = Camera.open();
            if (camera != null) {
                try {
                    Camera.Parameters parameters = camera.getParameters();
                    if (parameters.getSupportedFocusModes().contains(
                            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    }
                    parameters.setPreviewSize(w,h);
//                    Camera.Size s = parameters.getPictureSize();
                    parameters.setPictureSize(picw,pich);
//                    Log.d(TAG, "Height: " + Integer.toString(pich) + "; Width: " + Integer.toString(picw));
//                    List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
//                    for(int i=0;i<sizes.size();i++) {
//                        int height = sizes.get(i).height;
//                        int width = sizes.get(i).width;
//                        Log.d("size: ", Integer.toString(width)+";"+Integer.toString(height));
//                        //2592x1944;1920x1080;1440x1080;1280x720;640x480;352x288;320x240;176x144;
//                    }
                    camera.setParameters(parameters);
                    camera.setDisplayOrientation(90);
                    //camera.setPreviewDisplay(dummy.getHolder());
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.setPreviewCallback(new PreviewCallback() {
                        public void onPreviewFrame(byte[] data, Camera camera)
                        {

                            Canvas canvas = surfaceHolder2.lockCanvas(null);
                            // Manipulate preview image in this function
                            drawSomething(canvas,data);
                            surfaceHolder2.unlockCanvasAndPost(canvas);
                            //Only take a picture if the business card is detected and a picture has not been taken
                            if(detected && !picTaken){
                                camera.takePicture(null, null, mPicture);
                            }
                        }
                    });
                    camera.startPreview();
                    previewing = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null && previewing) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            previewing = false;
        }
    }

    // Callback will be directed to this function
    protected void drawSomething(Canvas canvas, byte[] data) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        // Convert YUV to RGB
        int[] rgbdata = yuv2rgb(data);
        Bitmap bmp = Bitmap.createBitmap(rgbdata,w,h,Bitmap.Config.ARGB_8888);
        //The business card detection happens here
        bmp = processFrame(bmp);
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        // Draw the bitmap
        canvas.drawBitmap(bmp,new Rect(0,0,h,w),new Rect(0,0,canvas.getWidth(),canvas.getHeight()),null);
    }

    // Convert YUV to RGB
    public int[] yuv2rgb(byte[] data){
        final int frameSize = w * h;
        int[] rgb = new int[frameSize];

        for (int j = 0, yp = 0; j < h; j++) {
            int uvp = frameSize + (j >> 1) * w, u = 0, v = 0;
            for (int i = 0; i < w; i++, yp++) {
                int y = (0xff & ((int) data[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & data[uvp++]) - 128;
                    u = (0xff & data[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)                  r = 0;
                else if (r > 262143)       r = 262143;
                if (g < 0)                  g = 0;
                else if (g > 262143)       g = 262143;
                if (b < 0)                  b = 0;
                else if (b > 262143)        b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }

    private Bitmap processFrame(Bitmap bmp){

        //Convert bitmap to
        //Only perform on a cropped version
        Bitmap cropped = Bitmap.createBitmap(bmp, (int)((double)bmp.getWidth()/2) - (bottom - top)/2- step, 0, (bottom-top)+2*step, bmp.getHeight());
        Mat img_cropped = new Mat(cropped.getWidth(), cropped.getHeight(), CvType.CV_8UC1);


        Mat image_big = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
        Mat img_p = new Mat();
        Utils.bitmapToMat(bmp,image_big);
        Utils.bitmapToMat(cropped,img_cropped);

        Mat img_gray = new Mat();


        Imgproc.cvtColor(img_cropped,img_gray,Imgproc.COLOR_RGB2GRAY);

        //apply Gaussian blur
        Imgproc.GaussianBlur(img_gray, img_p, new Size(5, 5) ,1 ,1);

        //apply the Canny edge detector
        Imgproc.Canny( img_p, img_p, 5, 40 );

        //Apply the Hough transform
        Mat lines = new Mat();
        Imgproc.HoughLines(img_p, lines, 1, Math.PI/1800, 80);

        //Filter the results

        //Store rho and theta returned from hough transform
        double data[] ;
        double rho1[] = new double[lines.rows()];
        double theta1[] = new double[lines.rows()];
        for (int i = 0; i < lines.rows(); i++) {
            data = lines.get(i,0);
            rho1[i] = data[0];
            theta1[i] = data[1];

        }
        Vector<Double> rho_final = new Vector<>();
        Vector<Double> theta_final = new  Vector<>();

        for (int i=0; i < rho1.length ; i++) {

            double min = 1000 ;
            double min2 = 1000 ;

            //Find the closest distance in array of final rho and theta values
            for(int k=0; k < rho_final.size(); k++) {
                double cost = Math.pow(rho1[i] - rho_final.get(k) , 2)/255 + Math.pow(theta1[i] - theta_final.get(k) , 2)/0.5 ;
                double cost2 = Math.pow(Math.abs(rho1[i]) - Math.abs(rho_final.get(k)) , 2)/400 + Math.pow(Math.abs(theta1[i] - Math.PI) - Math.abs(theta_final.get(k)) , 2)/0.5 ;
                if( cost < min){
                    min = cost;
                }
                if( cost2 < min2){
                    min2 = cost2;
                }
            }

            //If the final closest distance is close enough, discard the found line
            if(min <1) {
                continue;
            }
            else if(Math.abs(theta1[i] - Math.PI) <= Math.PI*10/180 || Math.abs(theta1[i]) <= Math.PI*10/180 ){
                if(min2 < 1){
                    continue;
                }
            }
            //Otherwise keep it
            rho_final.add(rho1[i]);
            theta_final.add(theta1[i]);

        }

        //Normalization factors
        double normRho = 49;
        double normTheta = 0.3;

        //If close enough, say the left vertical edge is detected
        double rhoLeft = step;
        List<Double> min3pi = closestDistance(rho_final, -rhoLeft, normRho, theta_final, Math.PI, normTheta);
        List<Double> min3zero = closestDistance(rho_final, rhoLeft, normRho, theta_final, 0, normTheta);
        if(min3zero.get(1) < min3pi.get(1) && min3zero.get(1) < 2.0){
            leftDetected = true;
        }
        else if (min3pi.get(1) < 2.0){
            leftDetected = true;
        }

        //If close enough, say the right vertical edge is detected
        double rhoRight = img_cropped.height()-step;
        List<Double> min4pi = closestDistance(rho_final, -rhoRight, normRho, theta_final, Math.PI, normTheta);
        List<Double> min4zero = closestDistance(rho_final, rhoRight, normRho, theta_final, 0, normTheta);
        if(min4zero.get(1) < min4pi.get(1) && min4zero.get(1) < 2.0){
            rightDetected = true;
        }
        else if(min4pi.get(1) < 2.0){
            rightDetected = true;
        }

        //If close enough, say the top vertical edge is detected
        double rhoTop = step;
        List<Double> min1 = closestDistance(rho_final, rhoTop,normRho,theta_final, Math.PI/2,normTheta);
        if(min1.get(1) < 2.0){
            topDetected = true;
        }

        //If close enough, say the bottom vertical edge is detected
        double rhoBottom = img_cropped.width()-step;
        List<Double> min2 = closestDistance(rho_final, rhoBottom,normRho,theta_final, Math.PI/2,normTheta);
        if(min2.get(1) < 2.0){
            bottomDetected = true;
        }

        //Coordinates for frame
        double tP = step - 1;
        double bP = img_cropped.width() - step -1;
        double rP = img_cropped.height() - step - 1;
        double lP = step - 1;


        //Draw top line of the frame
        if(topDetected){
            Imgproc.line(img_cropped, new Point(tP, lP),new Point(tP, rP), new Scalar(0, 255, 0), 2);
        }else{
            Imgproc.line(img_cropped, new Point(tP, lP),new Point(tP, rP)  , new Scalar(255, 0, 0), 2);
        }

        //Draw bottom line of the frame
        if(bottomDetected){
            Imgproc.line(img_cropped, new Point(bP, lP),new Point(bP, rP), new Scalar(0, 255, 0), 2);
        }else{
            Imgproc.line(img_cropped, new Point(bP, lP),new Point(bP, rP), new Scalar(255, 0, 0), 2);
        }

        //Draw the left line of the frame
        if(leftDetected){
            Imgproc.line(img_cropped, new Point(tP, lP),new Point(bP, lP), new Scalar(0, 255, 0), 2);
        }else{
            Imgproc.line(img_cropped, new Point(tP, lP),new Point(bP, lP), new Scalar(255, 0, 0), 2);
        }

        //Draw the right line of the frame
        if(rightDetected){
            Imgproc.line(img_cropped, new Point(tP, rP),new Point(bP, rP), new Scalar(0, 255, 0), 2);
        }else{
            Imgproc.line(img_cropped, new Point(tP, rP),new Point(bP, rP), new Scalar(255, 0, 0), 2);
        }

        //If all the sides are detected then the business card is detected and we can take the picture
        if(rightDetected && leftDetected && topDetected && bottomDetected){
            Imgproc.circle(img_cropped, new Point(5.0,5.0), 5 , new Scalar(0), 2 ,8 , 0);
            detected = true;
        }else{
            Imgproc.circle(img_cropped, new Point(5.0,5.0), 5 , new Scalar(0), 2 ,8 , 0);
            Imgproc.circle(img_cropped, new Point(10.0,5.0), 5 , new Scalar(0), 2 ,8 , 0);
        }

        //Make sure the card is detected 3 times in row
        if(detected == true){
            detectCount++;
        } else{
            detectCount = 0;
        }

        if(detectCount <3){
            detected = false;
        }


        rightDetected = false;
        leftDetected = false;
        topDetected = false;
        bottomDetected = false;

        //draw Lines on the Image
        for (int i = 0; i < rho_final.size(); i++) {
            double rho = rho_final.get(i);
            double theta = theta_final.get(i);
            double cosTheta = Math.cos(theta);
            double sinTheta = Math.sin(theta);
            double x0 = cosTheta * rho;
            double y0 = sinTheta * rho;
            Point pt1 = new Point(x0 + 10000 * (-sinTheta), y0 + 10000 * cosTheta);
            Point pt2 = new Point(x0 - 10000 * (-sinTheta), y0 - 10000 * cosTheta);
            Imgproc.line(img_cropped, pt1, pt2, new Scalar(0, 0, 255), 2);
        }


       Bitmap retbmp = Bitmap.createBitmap(img_cropped.cols(), img_cropped.rows(), Bitmap.Config.ARGB_8888);

       Utils.matToBitmap(img_cropped, retbmp);

        return retbmp;
    }

    //Finds the closest value in an array
    public static List<Double> closestDistance(Vector<Double> a1, double a0,double norm1, Vector<Double> b1, double b0, double norm2) {
        List<Double> closest = new ArrayList<Double>();
        double min = Double.MAX_VALUE;
        int argmin = 0;
        for (int i = 0; i < a1.size(); ++i) {
            double temp = Math.sqrt(Math.pow(a1.get(i)-a0,2)/Math.pow(norm1,2) + Math.pow(b1.get(i)-b0,2)/Math.pow(norm2,2));
            if (temp < min) {
                min = temp;
                argmin = i;
            }
        }
        closest.add((double)argmin);
        closest.add(min);
        return closest;
    }





}
