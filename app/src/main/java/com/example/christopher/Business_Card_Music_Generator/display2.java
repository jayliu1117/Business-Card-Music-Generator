package com.example.christopher.Business_Card_Music_Generator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by Yu-Jeh liu on 4/30/2017.
 */

public class display2 extends Activity {

    private double[] initial = {255,255,255} ;
    private double[] increment = {100,100,100} ;
    private double length = (display.final_img.width())*1372 ;
    private short[] sound_signal_short = new short[((int)length)+1372] ;
    private int[] label_fixed = new int[display.img_label.width()*display.img_label.height()];
    private Mat progress_bar = new Mat(10 , display.final_img.width() , CvType.CV_8UC3) ;
    private int w = 0;
    ImageView result_photo;
    ImageView Progress_bar ;
    Bitmap photo;
    Bitmap bar = Bitmap.createBitmap(progress_bar.cols(), progress_bar.rows(), Bitmap.Config.ARGB_8888);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display2);

        result_photo = (ImageView)findViewById(R.id.imageView);
        Progress_bar = (ImageView)findViewById(R.id.ProgressBar) ;
        for(int i = 0; i<progress_bar.rows(); i++){
            for(int j = 0 ; j<progress_bar.cols(); j++){
                progress_bar.put(i , j , initial) ;
            }

        }
        Utils.matToBitmap(progress_bar,bar);
        photo = display.final_bmp;


        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        result_photo.setImageBitmap(photo);
        Progress_bar.setImageBitmap(bar);

        CharSequence text_after = "Voila !";

        Toast toast2 = Toast.makeText(context, text_after, duration);
        toast2.show();




    }


    public void button_Home(View v){
        Intent intent = new Intent(display2.this, MainActivity.class);
//                Intent intent = new Intent(display.this, display2.class);
        startActivity(intent);

        // Initialization toast

        }

    // On click for the "Play" button
    public void button_play(View v){
        // Initialization toast

        Context context = getBaseContext() ;
        int duration = Toast.LENGTH_SHORT;
        CharSequence text_before = "Generating sound signal ...... ";
        final Toast toast1 = Toast.makeText(context, text_before, duration);
        toast1.show();

        new Handler().postDelayed(new Runnable() {
//            private static final String TAG = "Display signal short: " ;

            @Override public void run() {

                for(int i=0; i<sound_signal_short.length ; i++){
                    sound_signal_short[i] = 0 ;
                }

                Pitch_and_sound();
                play();

                Context context = getBaseContext() ;
                int duration = Toast.LENGTH_SHORT;
                CharSequence text_before = "Done Playing!! ";
                final Toast toast1 = Toast.makeText(context, text_before, duration);
                toast1.show();

//                }
            }
        }, 1000);




    }

    // Arange method
    private double[] arange(double start , double end , double interval){
        int number = (int)((end-start)/interval) ;
        double[] result = new double[number] ;
        for(int i = 0; i< number ; i++){
            result[i] = start + i*interval ;
        }

        return result ;
    }


    // Hanning window method
    public double[] hanningWindow(double[] recordedData) {


        for (int n = 0; n < recordedData.length; n++) {
            // reduce unnecessarily performed frequency part of each and every frequency
            recordedData[n] *= 0.5 * (1.0 - Math.cos((2.0 * Math.PI * n) / (recordedData.length - 1.0)));
        }
        // return modified buffer to the FFT function
        return recordedData;
    }

    // play method to play the byte array signal
    private void play() {

//        // Initialization toast
//        Context context = getBaseContext() ;
//        int duration = Toast.LENGTH_SHORT;
//        CharSequence text_before = "Now Playing! ";
//
//        Toast toast1 = Toast.makeText(context, text_before, duration);
//        toast1.show();
        new Thread(new Runnable() {
            public void run() {

                final int TEST_SR = 48000;
                final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
                final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
                final int TEST_MODE = AudioTrack.MODE_STREAM; // Stream mode.
                final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
                final int buffer_size = 1372 * 8;
                AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, buffer_size, TEST_MODE);
                track.play();

                short[] samples = new short[buffer_size];


                int n = 0;

                while (n < sound_signal_short.length - buffer_size) {

                    for (int m = 0; m < buffer_size; m++) {
                        samples[m] = sound_signal_short[n + m];

                        if ((n + m + 1) / 1372 >= w) {
                            for (int i = 0; i < 5; i++) {
                                progress_bar.put(i, w, increment);

                            }
                            w++;
                        }

                    }

                    track.write(samples, 0, buffer_size);

                    Progress_bar.post(new Runnable() {
                        public void run() {

                            Utils.matToBitmap(progress_bar, bar);
                            Progress_bar.setImageBitmap(bar);
                        }


                    });


                    n += buffer_size;
                }


                track.stop();

                w = 0 ;

                for (int j = 0; j < progress_bar.width(); j++) {
                    for (int i = 0; i < 5; i++) {
                        progress_bar.put(i, j, initial);

                    }

                }

                Progress_bar.post(new Runnable() {
                    public void run() {

                        Utils.matToBitmap(progress_bar, bar);
                        Progress_bar.setImageBitmap(bar);
                    }


                });

            }
        }).start();



//        for(int i = 0; i<progress_bar.rows(); i++){
//            for(int j = 0 ; j<progress_bar.cols(); j++){
//                progress_bar.put(i , j , initial) ;
//            }
//
//        }
//        Utils.matToBitmap(progress_bar,bar);
//        Progress_bar.setImageBitmap(bar);

    }

    // method to determine the pitch and generate the byte array sound signal
    private void Pitch_and_sound(){

//        String TAG = "Label after:" ;
//
//        int maxLogSize = 1000;
//                for(int i = 0; i <= label_fixed.height(); i++) {
//                    for(int j = 0; j<= label_fixed.width(); j++){
//                        double[] lab = label_fixed.get(i,j);
//                        String label_string = Double.toString(lab[0]) ;
////                        end = end > sound_signal_short.length  ? sound_signal_short.length  : end;
//                        Log.v(TAG + i, j + label_string );
//
//                    }
//                    int start = i * maxLogSize;
//                    int end = (i+1) * maxLogSize;
//                    String signal_string = Arrays.toString(sound_signal_short) ;
//                    end = end > sound_signal_short.length  ? sound_signal_short.length  : end;
//                    Log.v(TAG, signal_string.substring(start, end));
//                }

        // Initialization toast
        Context context = getBaseContext() ;
        int duration = Toast.LENGTH_SHORT;
        CharSequence text_before = "Generating sound signal ...... ";

        Toast toast1 = Toast.makeText(context, text_before, duration);
        toast1.show();
        // Assigning the pitch

        double[] pre_label ;
        int label ;
        int label_minus_one ;
        int count ;
        int note_class ;
        int color_class ;
        double height_for_pitch ;
        int[][][] sig = new int[display.final_img.width()][6][4] ;
        double gap = Math.floor(display.final_img.height()/5) ;

        for(int i = 0 ; i<display.img_label.height() ; i++) {
            for (int j = 0; j < display.img_label.width(); j++) {
                pre_label = display.img_label.get(i , j) ;


                if((int)pre_label[0] == 0){
                    label_fixed[i*display.img_label.width()+j] = 0 ;
                }
                else if((int)pre_label[0] == 1){
                    label_fixed[i*display.img_label.width()+j] = 1 ;
                }
                else if((int)pre_label[0] == 2){
                    label_fixed[i*display.img_label.width()+j] = 2 ;
                }
                else if((int)pre_label[0] == 3){
                    label_fixed[i*display.img_label.width()+j] = 3 ;
                }
                else if((int)pre_label[0] == 4){
                    label_fixed[i*display.img_label.width()+j] = 4 ;
                }
//                Log.v("fixed_label:", Integer.toString(label_fixed[i*MainActivity.img_label.width()+j]));
            }
        }


        for(int j = 0; j<display.final_img.width() ; j++){
            count = 0;

            for(int i = 1; i<display.final_img.height() ; i++){

                label = label_fixed[i*display.img_label.width()+j] ;
                label_minus_one = label_fixed[(i-1)*display.img_label.width()+j] ;

                if(label!= 0){
                    count +=1 ;
                }

                else if(label == 0 && count != 0 ){
                    for(int k = 0; k< (count+1) ; k++){
                        height_for_pitch = i - 1 - Math.floor(count/2) ;
                        note_class = (int)Math.round(height_for_pitch/gap) ;
                        color_class = (label_minus_one - 1) ;
                        for(int l=0; l<4 ;l++){
                            if(l != color_class){
                                sig[j][note_class][l] = 0;
                            }
                            else{
                                sig[j][note_class][l] = count ;
//                                Log.v("count:", Integer.toString(count));
//
                            }
                        }

                    }

                    count = 0 ;
                }

            }

        }

        // Construct the signal ( Assuming sampling rate = 48000 Hz)

//        for(int i = 0; i <= label_fixed.height(); i++) {
//                    for(int j = 0; j<= label_fixed.width(); j++){
//                        double[] lab = label_fixed.get(i,j);
//                        String label_string = Arrays.toString(lab[0]) ;
////                        end = end > sound_signal_short.length  ? sound_signal_short.length  : end;
//                        Log.v("sig:" ,  + label_string );
//
//                    }


        double samples_pixels = 1372 ;
        double bufferlength = 2*samples_pixels ;
        double[] frq_n = {1.0, 9.0/12 , 7.0/12 ,4.0/12 ,2.0/12 ,0} ;
        double interval = 1.0/48000 ;
        double[] t = arange(0 , bufferlength/48000 , interval) ;

//        double length = (MainActivity.final_img.width())*samples_pixels ;
//        sound_signal_short = new byte[(int)length] ;


        double start ;
        double[] color_array ;
        double[] insig = new double[(int)bufferlength] ;
        double[] sound_signal_double = new double[sound_signal_short.length] ;

        double max_val = 0 ;

        for(int j = 0; j<display.final_img.width() ; j++){

            start = bufferlength/2*(1+j) ;

            for(int i =0; i<6 ; i++){
                for(int k =0; k<4 ;k++){

//                    String label_color_array = Arrays.toString(color_array) ;
////                        end = end > sound_signal_short.length  ? sound_signal_short.length  : end;
//                    Log.v("color array:" ,  label_color_array );


                    if(sig[j][i][k] != 0 ){
                        for(int b = 0; b<bufferlength ; b++){
//                            insig[b] = (Float.MAX_VALUE)*(sig[j][i][k]]/10.0)*Math.cos(2.0*Math.PI*220.0*(Math.pow(2.0, k + frq_n[i]))*t[b]) ;
                            insig[b] = (Float.MAX_VALUE)*(sig[j][i][k]*20.0)*Math.cos(2.0*Math.PI*220.0*(Math.pow(2.0, k + frq_n[i]))*t[b]) ;

                        }

                        insig = hanningWindow(insig) ;

                        for(int a = 0; a<bufferlength ; a++){
                            sound_signal_double[(int)((start - bufferlength/2) + a)] = sound_signal_double[(int)((start - bufferlength/2) + a)] + insig[a]  ;
                            if(Math.abs(sound_signal_double[(int)((start - bufferlength/2) + a)]) > max_val){
                                max_val = Math.abs(sound_signal_double[(int)((start - bufferlength/2) + a)]) ;
                            }

                        }

                    }



                }
            }
        }

        for(int p = 0 ; p<sound_signal_double.length ; p++){
            sound_signal_short[p] = (short)((sound_signal_double[p]/max_val)*(Short.MAX_VALUE)) ;
            sound_signal_double[p] = 0 ;
        }

        CharSequence text_after = "Sound signal generated! ";
        final Toast toast2 = Toast.makeText(context, text_after, duration);
        toast2.show();

    }




}
