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
 * Created by jayli on 5/2/2017.
 */

public class Human_display extends Activity {

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
        setContentView(R.layout.human_display);

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
        Intent intent = new Intent(Human_display.this, MainActivity.class);
        startActivity(intent);


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


    // Hanning window method
    public short[] hanningWindow_short(short[] recordedData) {


        for (int n = 0; n < recordedData.length; n++) {
            // reduce unnecessarily performed frequency part of each and every frequency
            recordedData[n] *= 0.5 * (1.0 - Math.cos((2.0 * Math.PI * n) / (recordedData.length - 1.0)));
        }
        // return modified buffer to the FFT function
        return recordedData;
    }

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
                            }
                        }

                    }

                    count = 0 ;
                }

            }

        }

        // Construct the signal ( Assuming sampling rate = 48000 Hz)

        double samples_pixels = 1372 ;
        double bufferlength = 2*samples_pixels ;
        double[] frq_n = {1.0, 9.0/12 , 7.0/12 ,4.0/12 ,2.0/12 ,0} ;

        double start ;
        double[] insig = new double[(int)bufferlength] ;
        double[] sound_signal_double = new double[sound_signal_short.length] ;

        short[] short_sig = new short[(int)Math.pow(2 , 13)] ;
        for(int i = 0 ; i< (int)Math.pow(2 , 13) ; i++){
            short_sig[i] = Record.sig[Record.sig.length/2 + i] ;
        }
        short[] cropped = windowed(short_sig) ;


        double desired_freq ;


        double max_val = 0 ;

        for(int j = 0; j<display.final_img.width() ; j++){

            start = bufferlength/2*(1+j) ;

            for(int i =0; i<6 ; i++){
                for(int k =0; k<4 ;k++){
                    if(sig[j][i][k] != 0 ){

                        desired_freq = 220.0*Math.pow(2.0, k + frq_n[i]) ;

                        for(int b = 0; b< (bufferlength - cropped.length); b += (int)(Record.F_S/desired_freq)){
                            for(int c = 0 ; c< cropped.length ; c++){

                                insig[b + c] = (sig[j][i][k])*cropped[c] ;
                            }
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



    public short[] windowed(short[] recorded_raw){

        Complex[] signal = new Complex[recorded_raw.length] ;
        Complex[] time_reversal_signal = new Complex[recorded_raw.length] ;

        for(int i = 0; i<recorded_raw.length ; i++){
            signal[i] = new Complex((double)recorded_raw[i] , 0.0) ;
            time_reversal_signal[i] = new Complex((double)recorded_raw[recorded_raw.length - 1 - i] , 0.0) ;
        }

        Complex[] signal_FFT = new Complex[recorded_raw.length] ;
        Complex[] time_reversal_signal_FFT = new Complex[recorded_raw.length] ;

        signal_FFT = FFT.fft(signal) ;
        time_reversal_signal_FFT = FFT.fft(time_reversal_signal) ;

        Complex[] mult_FFT = new Complex[recorded_raw.length] ;

        for(int i = 0; i<recorded_raw.length ; i++){
            mult_FFT[i] = signal_FFT[i].times(time_reversal_signal_FFT[i]) ;
        }

        Complex[] corre = new Complex[recorded_raw.length] ;
        corre = FFT.ifft(mult_FFT) ;

        double[] mag = new double[recorded_raw.length] ;
        int sig_max_idx = 0 ;
        short sig_max = 0 ;


        for(int i = 0; i<recorded_raw.length ; i++){

            mag[i] = corre[i].abs() ;

            if(recorded_raw[i] > sig_max && i < recorded_raw.length - (Record.F_S/100)){

                sig_max_idx = i ;
            }

        }

        double max = 0 ;
        int max_idx = 0 ;

        for(int i = Record.F_S/1200; i< Record.F_S/100 ; i++){
            if(mag[i] > max){
                max = mag[i] ;
                max_idx = i ;
            }
        }

        short[] cropped = new short[max_idx] ;

        for(int i = sig_max_idx; i< sig_max_idx + max_idx   ; i++){

            cropped[i - sig_max_idx] = recorded_raw[i - max_idx/2] ;
        }


        cropped = hanningWindow_short(cropped) ;

        return cropped ;

    }





}
