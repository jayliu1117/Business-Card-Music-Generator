package com.example.christopher.Business_Card_Music_Generator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class display extends Activity {

    public static Mat img_label ;
    public static Mat final_img ;
    public static Bitmap final_bmp ;
    ImageView result_photo;
    ImageView result_photo2;
    //byte [] bitmapdata;
    Bitmap photo;
    Bitmap photo2;
    private int wstep = 75;
    private int hstep = 160;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);

        if(MainActivity.appFlag == 1){
            wstep = 75;
            hstep = 160;
        } else if(MainActivity.appFlag == 2){
            wstep = 75;
            hstep = 175;
        }

        Button click = (Button)findViewById(R.id.Bretake);
        Button click2 = (Button)findViewById(R.id.gen);
        Button click3 = (Button)findViewById(R.id.gen2);

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(MainActivity.accBmp != null){
                    MainActivity.accBmp.recycle();
                }
                if(photo != null){
                    photo.recycle();
                }
                Intent intent = new Intent(display.this, TakeThePicture.class);
                startActivity(intent);
            }
        });

        click2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(display.this, display2.class);
                startActivity(intent);
            }
        });

        click3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(display.this, Record.class);
                startActivity(intent);
            }
        });

        result_photo = (ImageView)findViewById(R.id.imageView);
        result_photo2 = (ImageView)findViewById(R.id.imageView2);

        photo = TakeThePicture.accBmp;
        Bitmap newbmp = processFrame(photo);
        final_bmp = processFrame2(newbmp) ;

        result_photo.setImageBitmap(newbmp);
        result_photo2.setImageBitmap(final_bmp);
    }

    private Bitmap processFrame(Bitmap bmp){

        //Crop the image
        Rect rect = new Rect(0, (int)((double)bmp.getHeight()/2) - hstep - wstep, bmp.getWidth()-1, (int)((double)bmp.getHeight()/2) + hstep + wstep);
        assert(rect.left < rect.right && rect.top < rect.bottom);
        Bitmap cropped = Bitmap.createBitmap(rect.right-rect.left, rect.bottom-rect.top, Bitmap.Config.ARGB_8888);
        new Canvas(cropped).drawBitmap(bmp, -rect.left, -rect.top, null);
        Mat img_cropped = new Mat(cropped.getWidth(), cropped.getHeight(), CvType.CV_8UC1);

        Mat image_big = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
        Mat img_p = new Mat();

        Utils.bitmapToMat(bmp,image_big);
        Utils.bitmapToMat(cropped,img_cropped);

        Mat img_gray = new Mat();
        int w = image_big.width();
        int h = image_big.height();
        Mat image = image_big;

        //Convert to grayscale
        Imgproc.cvtColor(img_cropped,img_gray,Imgproc.COLOR_RGB2GRAY);

        //apply Gaussian blur
        Imgproc.GaussianBlur(img_gray, img_p, new Size(7, 7) ,1 ,1);

        //apply the Canny edge detector
        Imgproc.Canny( img_p, img_p, 5, 40 );

        //apply Hough transform for lines
        Mat lines = new Mat();
        Imgproc.HoughLines(img_p, lines, 1, Math.PI/1800, 100);

        ////////Filter the results

        //Store the data
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


            if(min <1) {
                continue;
            }
            else if(Math.abs(theta1[i] - Math.PI) <= Math.PI*10/180 || Math.abs(theta1[i]) <= Math.PI*10/180 ){
                if(min2 < 1){
                    continue;
                }
            }

            rho_final.add(rho1[i]);
            theta_final.add(theta1[i]);


        }

        double maxR = Math.sqrt((double)Math.pow(w,2)+Math.pow((double)h,2));

        //Find the left verticle edge
        double rhoLeft = (double)(wstep);
        double min3pi = closestDistance(rho_final, -rhoLeft, maxR, theta_final, Math.PI, Math.PI);
        double min3zero = closestDistance(rho_final, rhoLeft, maxR, theta_final, 0, Math.PI);
        int idxLeft = 0;
        if(min3zero < min3pi){
            idxLeft = argClosestDistance(rho_final, rhoLeft, maxR, theta_final, 0, Math.PI);
        }
        else{
            idxLeft = argClosestDistance(rho_final, -rhoLeft, maxR, theta_final, Math.PI, Math.PI);
        }

        //Find right verticle edge
        double rhoRight = img_cropped.width()-wstep;
        double min4pi = closestDistance(rho_final, -rhoRight, maxR, theta_final, Math.PI, Math.PI);
        double min4zero = closestDistance(rho_final, rhoRight, maxR, theta_final, 0, Math.PI);
        int idxRight = 0;
        if(min4zero < min4pi){
            idxRight = argClosestDistance(rho_final, rhoRight, maxR, theta_final, 0, Math.PI);
        }
        else{
            idxRight = argClosestDistance(rho_final, -rhoRight, maxR, theta_final, Math.PI, Math.PI);
        }

        //Find the top edge
        double rhoTop = ((double)img_cropped.height()/2-hstep);
        int idxTop = argClosestDistance(rho_final, rhoTop,maxR,theta_final, Math.PI/2,Math.PI);

        //Find the bottom edge
        double rhoBottom =((double)img_cropped.height()/2+hstep);
        int idxBottom = argClosestDistance(rho_final, rhoBottom, maxR, theta_final, Math.PI/2, Math.PI);

        //Find top left corner: idxTop, idxLeft
        double det1 = Math.cos(theta_final.get(idxTop))*Math.sin(theta_final.get(idxLeft)) - Math.cos(theta_final.get(idxLeft))*Math.sin(theta_final.get(idxTop));
        double x1 = Math.round((Math.sin(theta_final.get(idxLeft))*rho_final.get(idxTop)-Math.sin(theta_final.get(idxTop))*rho_final.get(idxLeft))/det1);
        double y1 = Math.round((Math.cos(theta_final.get(idxTop))*rho_final.get(idxLeft)-Math.cos(theta_final.get(idxLeft))*rho_final.get(idxTop))/det1);
        Point t1 = new Point((int)x1,(int)y1);

        //Find top right corner: idxTop, idxRight
        double det2 = Math.cos(theta_final.get(idxTop))*Math.sin(theta_final.get(idxRight)) - Math.cos(theta_final.get(idxRight))*Math.sin(theta_final.get(idxTop));
        double x2 = Math.round((Math.sin(theta_final.get(idxRight))*rho_final.get(idxTop)-Math.sin(theta_final.get(idxTop))*rho_final.get(idxRight))/det2);
        double y2 = Math.round((Math.cos(theta_final.get(idxTop))*rho_final.get(idxRight)-Math.cos(theta_final.get(idxRight))*rho_final.get(idxTop))/det2);
        Point t2 = new Point((int)x2,(int)y2);

        //Find Bottom right corner: idxBottom, idxRight
        double det3 = Math.cos(theta_final.get(idxBottom))*Math.sin(theta_final.get(idxRight)) - Math.cos(theta_final.get(idxRight))*Math.sin(theta_final.get(idxBottom));
        double x3 = Math.round((Math.sin(theta_final.get(idxRight))*rho_final.get(idxBottom)-Math.sin(theta_final.get(idxBottom))*rho_final.get(idxRight))/det3);
        double y3 = Math.round((Math.cos(theta_final.get(idxBottom))*rho_final.get(idxRight)-Math.cos(theta_final.get(idxRight))*rho_final.get(idxBottom))/det3);
        Point t3 = new Point((int)x3,(int)y3);

        //Find Bottom left corner: idxBottom, idxLeft
        double det4 = Math.cos(theta_final.get(idxBottom))*Math.sin(theta_final.get(idxLeft)) - Math.cos(theta_final.get(idxLeft))*Math.sin(theta_final.get(idxBottom));
        double x4 = Math.round((Math.sin(theta_final.get(idxLeft))*rho_final.get(idxBottom)-Math.sin(theta_final.get(idxBottom))*rho_final.get(idxLeft))/det4);
        double y4 = Math.round((Math.cos(theta_final.get(idxBottom))*rho_final.get(idxLeft)-Math.cos(theta_final.get(idxLeft))*rho_final.get(idxBottom))/det4);
        Point t4 = new Point((int)x4,(int)y4);

        //Draw Points
//        Imgproc.circle(image, t1, 5 , new Scalar(0), 2 ,8 , 0);
//        Imgproc.circle(image, t2, 5 , new Scalar(0), 2 ,8 , 0);
//        Imgproc.circle(image, t3, 5 , new Scalar(0), 2 ,8 , 0);
//        Imgproc.circle(image, t4, 5 , new Scalar(0), 2 ,8 , 0);

        //Prep the source points for finding homography
        List<Point> src = new ArrayList<Point>();
        src.add(t1);
        src.add(t2);
        src.add(t3);
        src.add(t4);

        //Prep the destination points for finding homography
        List<Point> dst = new ArrayList<Point>();
        dst.add(new Point(0.0,0.0));
        dst.add(new Point(349.0,0.0));
        dst.add(new Point(349.0,199.0));
        dst.add(new Point(0.0,199.0));

        MatOfPoint2f m_src = new MatOfPoint2f(); m_src.fromList(src);
        MatOfPoint2f m_dst = new MatOfPoint2f(); m_dst.fromList(dst);

        //Get homography matrix
        Mat perspectiveTransform=Imgproc.getPerspectiveTransform(m_src, m_dst);

//        String print = "";
//        for(int i = 0; i < perspectiveTransform.width(); i++){
//            for(int j = 0; j< perspectiveTransform.height(); j++){
//                double val = perspectiveTransform.get(i,j)[0];
//                print = print+ Double.toString(val) + ", ";
//            }
//        }
//
//        //Log.d(TAG, print);

        Mat result = img_cropped.clone();

        //Apply homography matrix
        Imgproc.warpPerspective(img_cropped, result, perspectiveTransform, new Size(350,200));
        result.convertTo(result,CvType.CV_8UC1, 1.0);

//        //draw Lines on the Image
//        for (int i = 0; i < rho_final.size(); i++) {
//            double rho = rho_final.get(i);
//            double theta = theta_final.get(i);
//            double cosTheta = Math.cos(theta);
//            double sinTheta = Math.sin(theta);
//            double x0 = cosTheta * rho;
//            double y0 = sinTheta * rho;
//            Point pt1 = new Point(x0 + 10000 * (-sinTheta), y0 + 10000 * cosTheta);
//            Point pt2 = new Point(x0 - 10000 * (-sinTheta), y0 - 10000 * cosTheta);
//            Imgproc.line(img_cropped, pt1, pt2, new Scalar(0, 0, 255), 2);
//        }
//
//        //Draw chosen edge reference vlues
//        double [] testRho = {rhoRight, rhoLeft, rhoBottom, rhoTop};
//        double [] testTheta = { 0, 0,Math.PI/2, Math.PI/2};
//        for (int i = 0; i < Array.getLength(testRho); i++) {
//            double rho = testRho[i];
//            double theta = testTheta[i];
//            double cosTheta = Math.cos(theta);
//            double sinTheta = Math.sin(theta);
//            double x0 = cosTheta * rho;
//            double y0 = sinTheta * rho;
//            Point pt1 = new Point(x0 + 10000 * (-sinTheta), y0 + 10000 * cosTheta);
//            Point pt2 = new Point(x0 - 10000 * (-sinTheta), y0 - 10000 * cosTheta);
//            Imgproc.line(image, pt1, pt2, new Scalar(255), 2);
//        }

        //Convert to bitmap
        Bitmap retbmp = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, retbmp);

        return retbmp;
    }

    public static int argClosestDistance(Vector<Double> a1, double a0,double norm1, Vector<Double> b1, double b0, double norm2) {
        double min = Double.MAX_VALUE;
        int argmin = 0;
        for (int i = 0; i < a1.size(); ++i) {
            double temp = Math.sqrt(Math.pow(a1.get(i)-a0,2)/Math.pow(norm1,2) + Math.pow(b1.get(i)-b0,2)/Math.pow(norm2,2));
            if (temp < min) {
                min = temp;
                argmin = i;
            }
        }
        return argmin;
    }

    public static double closestDistance(Vector<Double> a1, double a0,double norm1, Vector<Double> b1, double b0, double norm2) {
        double min = Double.MAX_VALUE;
        int argmin = 0;
        for (int i = 0; i < a1.size(); ++i) {
            double temp = Math.sqrt(Math.pow(a1.get(i)-a0,2)/Math.pow(norm1,2) + Math.pow(b1.get(i)-b0,2)/Math.pow(norm2,2));
            if (temp < min) {
                min = temp;
                argmin = i;
            }
        }
        return min;
    }


    private Bitmap processFrame2(Bitmap bmp){

        //Convert bitmap to
        Mat image = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC4);
        Mat img_yuv = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC3);
        final_img = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC3) ;
        img_label = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC1) ;
        Utils.bitmapToMat(bmp,image);

        // Convert the color space to YUV
        Imgproc.cvtColor(image,img_yuv,Imgproc.COLOR_RGB2YCrCb);

        // Setting Y = 120 for each pixel
        for(int i = 0 ; i<bmp.getHeight() ; i++) {
            for (int j = 0; j < bmp.getWidth(); j++) {
                double[] lum = new double[3] ;
                double[] yuv = img_yuv.get(i,j) ;
                lum[0] = 120 ;
                lum[1] = yuv[1] ;
                lum[2] = yuv[2] ;
                img_yuv.put(i, j , lum) ;

            }
        }

        // Convert the image back to the RGB color space
        Imgproc.cvtColor(img_yuv,img_yuv,Imgproc.COLOR_YCrCb2RGB);

        double[] red = {255,0,0} ;
        double[] green = {0,255,0} ;
        double[] blue = {0,0,255} ;
        double[] black = {0,0,0} ;
        double[] white = {255,255,255} ;
        double thresh_rgb = 100;
        double thresh_k = 160 ; // original: 125
        double thresh = 15 ;
        double margin = 5 ;

        // Thresholding and color differentiation
        for(int i = 0 ; i<bmp.getHeight() ; i++) {
            for (int j = 0; j < bmp.getWidth(); j++) {

                double[] rgb = img_yuv.get(i,j) ;
                double[] rgb_orig = image.get(i,j) ;
                if((i> margin && j> margin) && (bmp.getHeight()-i> margin && bmp.getWidth()-j> margin)){
                    if(rgb_orig[0] > thresh_rgb && rgb_orig[1] > thresh_rgb && rgb_orig[2] > thresh_rgb ){
                        final_img.put(i, j , white) ;
                        img_label.put(i, j , 0) ;
                    }
                    else if(rgb[0] - rgb[1] > thresh && rgb[0] -rgb[2] > thresh ){
                        final_img.put(i, j , red) ;
                        img_label.put(i, j , 1) ;
                    }
                    else if(rgb[1] - rgb[0] > thresh && rgb[1] -rgb[2] > thresh ){
                        final_img.put(i, j , green) ;
                        img_label.put(i, j , 2) ;
                    }
                    else if(rgb[2] - rgb[0] > thresh && rgb[2] -rgb[1] > thresh ){
                        final_img.put(i, j , blue) ;
                        img_label.put(i, j , 3) ;
                    }
                    else if(rgb_orig[0] < thresh_k && rgb_orig[1] < thresh_k && rgb[2] < thresh_k){
                        final_img.put(i, j , black) ;
                        img_label.put(i, j , 4) ;
                    }
                    else{
                        final_img.put(i, j , white) ;
                        img_label.put(i, j , 0) ;
                    }

                }

                else{
                    final_img.put(i, j , white) ;
                }
            }
        }

        // Filtering out noise ( Step 1) : cleaning up the stray pixels among other color pixels

        int size_sqr = 5 ;
        double[] label ;
        int red_count ;
        int green_count ;
        int blue_count ;
        int black_count ;

        for(int x = 0; x< (bmp.getHeight()/size_sqr -1) ; x++ ){
            for(int y = 0; y< (bmp.getWidth()/size_sqr -1) ; y++ ){
                red_count = 0;
                green_count = 0;
                blue_count = 0;
                black_count = 0;

                for(int j =0 ; j<size_sqr ; j++){
                    for(int i =0 ; i<size_sqr ; i++){
                        label = img_label.get(x*size_sqr + i, y*size_sqr + j) ;
                        if((int)label[0] == 1){
                            red_count += 1 ;
                        }
                        else if((int)label[0] == 2){
                            green_count += 1 ;
                        }
                        else if((int)label[0] == 3){
                            blue_count += 1 ;
                        }
                        else if((int)label[0] == 4){
                            black_count += 1 ;
                        }


                    }
                }

                if(red_count > green_count && red_count > blue_count && red_count > black_count){
                    for(int j =0 ; j<size_sqr ; j++){
                        for(int i =0 ; i<size_sqr ; i++){
                            label = img_label.get(x*size_sqr + i, y*size_sqr + j) ;
                            if((int)label[0] != 1 && (int)label[0] != 0 ){
                                img_label.put(x*size_sqr + i, y*size_sqr + j , 1) ;
                            }

                        }
                    }
                }
                else if(green_count > red_count && green_count > blue_count && green_count > black_count){
                    for(int j =0 ; j<size_sqr ; j++){
                        for(int i =0 ; i<size_sqr ; i++){
                            label = img_label.get(x*size_sqr + i, y*size_sqr + j) ;
                            if((int)label[0] != 2 && (int)label[0] != 0 ){
                                img_label.put(x*size_sqr + i, y*size_sqr + j , 2) ;
                            }

                        }
                    }
                }
                else if(blue_count > green_count && blue_count > red_count && blue_count > black_count){
                    for(int j =0 ; j<size_sqr ; j++){
                        for(int i =0 ; i<size_sqr ; i++){
                            label = img_label.get(x*size_sqr + i, y*size_sqr + j) ;
                            if((int)label[0] != 3 && (int)label[0] != 0 ){
                                img_label.put(x*size_sqr + i, y*size_sqr + j , 3) ;
                            }

                        }
                    }
                }
                else if(black_count > green_count && black_count > blue_count && black_count > red_count){
                    for(int j =0 ; j<size_sqr ; j++){
                        for(int i =0 ; i<size_sqr ; i++){
                            label = img_label.get(x*size_sqr + i, y*size_sqr + j) ;
                            if((int)label[0] != 4 && (int)label[0] != 0 ){
                                img_label.put(x*size_sqr + i, y*size_sqr + j , 4) ;
                            }

                        }
                    }
                }



            }
        }


        // Filtering out noise ( Step 2) : Amplitude thresholding
        int amp_thresh = 2;
        int count ;
        double[] label_plus_one ;
        for(int j = 0; j<final_img.width() ; j++){
            count = 0;

            for(int i = 0; i<(final_img.height() -1) ; i++){
                label = img_label.get(i , j) ;
                label_plus_one = img_label.get(i + 1 , j) ;
                if((int)label[0] != 0){
                    count +=1 ;
                }

                else if((int)label[0] == 0 && (int)label_plus_one[0] == 0 && count != 0 && count < amp_thresh){
                    for(int k = 0; k< (count+1) ; k++){
                        img_label.put(i-k , j , 0) ;
                    }

                    count = 0 ;
                }

            }

        }


        // Assigning the color back to the image according to our label matrix
        for(int i = 0 ; i<bmp.getHeight() ; i++) {
            for (int j = 0; j < bmp.getWidth(); j++) {
                label = img_label.get(i , j) ;

                if((int)label[0] == 0){
                    final_img.put(i,j,white) ;
                }
                else if((int)label[0] == 1){
                    final_img.put(i,j,red) ;
                }
                else if((int)label[0] == 2){
                    final_img.put(i,j,green) ;
                }
                else if((int)label[0] == 3){
                    final_img.put(i,j,blue) ;
                }
                else if((int)label[0] == 4){
                    final_img.put(i,j,black) ;
                }
            }
        }


        //convert back to a bitmap
        Bitmap retbmp = Bitmap.createBitmap(final_img.cols(), final_img.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(final_img, retbmp);

        return retbmp;
    }






}

