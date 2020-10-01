package com.sdkpub.face.age.photo.Activty;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.MobileAds;
import com.sdkpub.face.age.photo.Ads.Admob;
import com.sdkpub.face.age.photo.Ads.GoogleAnalyticsApplication;
import com.sdkpub.face.age.photo.R;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cdflynn.android.library.checkview.CheckView;

import static com.sdkpub.face.age.photo.Ads.Admob.adsLayout1;

public class MainActivity extends AppCompatActivity {

    Button process, takePicture, bt_pickImageFromGallery;
    ImageView imageView, hidden;
    private Tracker mTracker;

    LinearLayout ly_beforePick, ly_beforePickGallery;
    RelativeLayout rl_loading;
    ImageView iv_settings, loadingLogo,iv_premium;

    CheckView checkViewTakePhoto, checkViewPickImage;

    public View view;
    private FaceServiceClient faceServiceClient;
    Bitmap mBitmap;
    Boolean ready = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rl_loading = findViewById(R.id.ly_loading);
        loadingLogo = findViewById(R.id.loadingLogo);

        view=getWindow().getDecorView().getRootView();

        MobileAds.initialize(this, getResources().getString(R.string.admob_app_id)); //TEST KEY
        Admob.createLoadBanner(getApplicationContext(), view);


        //Admob.createLoadInterstitial(getApplicationContext(),null);
      //  AudienceNetworkAds.facebookLoadBanner(getApplicationContext(), view);

        GoogleAnalyticsApplication application = (GoogleAnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.setScreenName("MainActivity");

// Send a screen view.
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        //IMPORTANT!!------------------------------------------------------------------------------
        //Replace the below tags <> with your own endpoint and API Subscription Key.
        //For help with this, read the project's README file.
        faceServiceClient = new FaceServiceRestClient("https://faceage.cognitiveservices.azure.com/face/v1.0", "8c9bec588fd34167a8d22c1a356c6e49");
        ly_beforePickGallery = findViewById(R.id.ly_beforePickGallery);
        ly_beforePick = findViewById(R.id.ly_beforePick);
        checkViewTakePhoto = findViewById(R.id.checkTakePhoto);
        checkViewPickImage = findViewById(R.id.checkPickImage);
        bt_pickImageFromGallery = findViewById(R.id.bt_pickImageFromGallery);
        iv_settings = findViewById(R.id.iv_sett);

        iv_premium = findViewById(R.id.iv_premium);
        iv_premium.setVisibility(View.INVISIBLE);
        takePicture = findViewById(R.id.takePic);
        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);

        iv_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        iv_premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPremiumDialog();
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Premium")
                        .build());
            }
        });

        process = findViewById(R.id.processClick);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 100);
                }
            }
        });

        bt_pickImageFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


//                showPremiumDialog();

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, 101);

//                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
//                } else {
//                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//                    startActivityForResult(intent, 101);
//                }
            }


        });

        process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Analiz sayısı")
                        .build());
//                Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                        R.drawable.murat2);
//
//

//

                //detectandFrame(mBitmap);

                if (ready) {

                    detectandFrame(mBitmap);


                } else {
                    makeToast(getString(R.string.please_take_pic));
                }
            }
        });


        dismissLoading();

    }

    private void dismissLoading(){

        loadingLogo.setVisibility(View.GONE);
        rl_loading.setVisibility(View.GONE);

    }

    private void showLoading(){

        loadingLogo.setVisibility(View.VISIBLE);
        rl_loading.setVisibility(View.VISIBLE);
        rotate_Clockwise(loadingLogo);


    }

    public void rotate_Clockwise(View view) {
        ObjectAnimator rotate = ObjectAnimator.ofFloat(view, "rotation", 360f ,0f);
        rotate.setRepeatCount(10000);
        rotate.setDuration(1000);
        rotate.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Image Picked
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {

                imagePicked();
                final Uri imageUri = data.getData();
                final InputStream imageStream;
                try {

                    this.mBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                    ready = true;

                }catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }

        //Photo Taken
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {

                //Check photo is taken
                photoTaken();

                mBitmap = (Bitmap) data.getExtras().get("data");
                // imageView.setImageBitmap(mBitmap);
                ready = true;
            }
        }
    }

    private void detectandFrame(final Bitmap mBitmap)  {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream((outputStream.toByteArray()));

        AsyncTask<InputStream, String, Face[]> detectTask = new AsyncTask<InputStream, String, Face[]>() {

            @Override
            protected Face[] doInBackground(InputStream... inputStreams) {

                publishProgress("Detecting...");
                Log.i("progress:","Detecting...");
                //This is where you specify the FaceAttributes to detect. You can change this for your own use.
                FaceServiceClient.FaceAttributeType[] faceAttr = new FaceServiceClient.FaceAttributeType[]{
                        FaceServiceClient.FaceAttributeType.HeadPose,
                        FaceServiceClient.FaceAttributeType.Age,
                        FaceServiceClient.FaceAttributeType.Gender,
                        FaceServiceClient.FaceAttributeType.Emotion,
                        FaceServiceClient.FaceAttributeType.FacialHair,
                        FaceServiceClient.FaceAttributeType.Makeup,
                        FaceServiceClient.FaceAttributeType.Glasses,

                };

                try {
                    Face[] result = faceServiceClient.detect(inputStreams[0],
                            true,
                            false,
                            faceAttr);

                    if (result == null) {
                        publishProgress("Detection failed. Nothing detected.");
                        Log.i("progress:","Failed");

                    }

                    publishProgress(String.format("Detection Finished. %d face(s) detected", result.length));
                    Log.i("progress:","Finished... %d face(s) detected" + result.length);

                    return result;
                } catch (Exception e) {
                    publishProgress("Detection Failed: " + e.getMessage());
                    Log.i("progress:","Failed..." + e.getMessage());

                    return null;
                }


            }

            @Override
            protected void onPreExecute() {
                showLoading();
            }

            @Override
            protected void onProgressUpdate(String... values) {
                showLoading();
            }

            @Override
            protected void onPostExecute(Face[] faces) {
                dismissLoading();
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                Gson gson = new Gson();
                String data = gson.toJson(faces);
                if (faces == null || faces.length == 0) {
                    noFaceDetected();
                } else {
                    intent.putExtra("list_faces", data);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    intent.putExtra("image", byteArray);

                    try {
                        startActivity(intent);
                    }catch (Exception e){}

                }

            }
        };
        detectTask.execute(inputStream);
    }

    private void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void noFaceDetected() {
        makeToast(getString(R.string.no_face_detected));

        //For Photo
        ly_beforePick.setVisibility(View.VISIBLE);
        checkViewTakePhoto.setVisibility(View.INVISIBLE);

        ly_beforePickGallery.setVisibility(View.VISIBLE);
        checkViewPickImage.setVisibility(View.INVISIBLE);

        //For Image

    }

    private void photoTaken() {

        ly_beforePick.setVisibility(View.INVISIBLE);
        checkViewTakePhoto.setVisibility(View.VISIBLE);
        checkViewTakePhoto.check();

        ly_beforePickGallery.setVisibility(View.VISIBLE);
        checkViewPickImage.setVisibility(View.INVISIBLE);
    }

    private void imagePicked() {

        ly_beforePickGallery.setVisibility(View.INVISIBLE);
        checkViewPickImage.setVisibility(View.VISIBLE);
        checkViewPickImage.check();

        ly_beforePick.setVisibility(View.VISIBLE);
        checkViewTakePhoto.setVisibility(View.INVISIBLE);

    }

    private void showDialog(){

        final Dialog settings_dialog = new Dialog(this);
        settings_dialog.setCancelable(true);
        settings_dialog.setContentView(R.layout.settings_dialog);
        settings_dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        settings_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tv_rateUsOnGooglePlay = settings_dialog.findViewById(R.id.tv_rateUsOnGooglePlay);
        TextView tv_aboutUs = settings_dialog.findViewById(R.id.tv_aboutUs);
        TextView tv_privacy = settings_dialog.findViewById(R.id.tv_privacy);
        tv_aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                makeToast("About Clicked");

                Intent i = new Intent(MainActivity.this,AboutActivity.class);
                startActivity(i);

                settings_dialog.dismiss();
            }
        });


        tv_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = "http://www.sdkpub.com/faceage_privacy";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        tv_rateUsOnGooglePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                makeToast("Rate Us Clicked");

                Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                }

                settings_dialog.dismiss();
            }
        });


        settings_dialog.show();

    }

    private void showPremiumDialog(){

        final Dialog premium_dialog = new Dialog(this);
        premium_dialog.setCancelable(true);
        premium_dialog.setContentView(R.layout.premium_dialog);
        premium_dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        premium_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button bt_getPremium = premium_dialog.findViewById(R.id.bt_getPremium);
        bt_getPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeToast(getString(R.string.coming_soon));
                premium_dialog.dismiss();
            }
        });



        premium_dialog.show();

    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        final Dialog dialog = new Dialog(this);
//        dialog.setCancelable(true);
//        dialog.setContentView(R.layout.alert_label_editor);
//        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//        TextView tv_aboutUs = dialog.findViewById(R.id.tv_aboutUs);
//
//        tv_aboutUs.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                makeToast("About Clicked");
//
//                Intent i = new Intent(MainActivity.this,AboutActivity.class);
//                startActivity(i);
//
//                dialog.dismiss();
//            }
//        });

       moveTaskToBack(true);

    }
}