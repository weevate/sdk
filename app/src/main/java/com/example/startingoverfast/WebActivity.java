package com.example.startingoverfast;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.productactivations.geoadsdk.Config;
import com.productactivations.geoadsdk.EasyLogger;
import com.productactivations.geoadsdk.MVideoView;

public class WebActivity extends Activity {


    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;


    RelativeLayout bar;

    WebView webView;
    ImageView errorMessage;

    boolean videoLoaded = false;

    MVideoView loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_web_view);

        bar = findViewById(R.id.progress);

        errorMessage = findViewById(R.id.error);
        webView = (WebView) findViewById(R.id.webview);
        WebSettings ws = webView.getSettings();

        loader = findViewById(R.id.progressBar);
        loader.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.loader));

        loader.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                loader.start();
            }
        });
        //  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //     loader.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE);
        //  }

        loader.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (!videoLoaded) {
                    // bar.setVisibility(View.VISIBLE);
                    videoLoaded = true;
                    //loader.mediaPlayer = mp;
                    //loader.mute();

                }
            }
        });
        loader.start();

        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setAllowContentAccess(true);
        ws.setAllowFileAccess(true);


        webView.loadUrl(getUrl());



        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO show you progress image

                errorMessage.setVisibility(View.GONE);

                if (!loader.isPlaying()) loader.start();
                bar.setVisibility(View.VISIBLE);

                webView.setVisibility(View.GONE);
                super.onPageStarted(view, url, favicon);


                // Toast.makeText(getApplicationContext(), "started loading", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO hide your progress image

                if (errorMessage.getVisibility() == View.VISIBLE) {
                    //     Toast.makeText(getApplicationContext(), "ERROR displayed " + bar.getVisibility(), Toast.LENGTH_LONG).show();
                    bar.setVisibility(View.GONE);
                    return;
                }

                bar.setVisibility(View.GONE);
                super.onPageFinished(view, url);

                loader.pause();
                webView.setVisibility(View.VISIBLE);

                //   Toast.makeText(getApplicationContext(), "FINISHED L loading", Toast.LENGTH_LONG).show();

            }


            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                //Your code to do
                errorMessage.setVisibility(View.VISIBLE);
                bar.setVisibility(View.GONE);
                //     Toast.makeText(getApplicationContext(), error.getErrorCode() + " " + error.getDescription(), Toast.LENGTH_LONG).show();

            }
        });



        webView.setWebChromeClient(new WebChromeClient()
        {
            // For 3.0+ Devices (Start)
// onActivityResult attached before constructor
            protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }


            // For Lollipop 5.0+ Devices
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    intent = fileChooserParams.createIntent();
                }
                try
                {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                    Toast.makeText(getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
            {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }
        });


        clearNotifications();
        startNotificationService();


    }


    private String getUrl(){
        SharedPreferences prefs  = getApplicationContext().getSharedPreferences("geofences", Context.MODE_PRIVATE);
        int notification_id = prefs.getInt("pending_notification_id", -1);//editPrefs.putInt("pending_notification_id", notification.id);
        String package_name = prefs.getString("pending_package_name", null);

        String url =  Config.url+"/performed_click/"+notification_id+"/"+package_name;
        EasyLogger.log("url is " + url);

        return url;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)
            {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
        else if (requestCode == FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage)
                return;
// Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
// Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
        else
            Toast.makeText(getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }




    private void clearNotifications(){

        //  Memory.save(getApplicationContext(), "pending_notification", null);
    }

    private void startNotificationService(){

          /*  Intent serviceIntent = new Intent(this, NotificationService.class);
            startService(serviceIntent);
            */
    }




    private void checkUserId(){

        Log.d("checking for id ", "checking for id");
   /*         webView.evaluateJavascript(
                    "(function() { return (document.getElementById('user-id').value); })();",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String html) {
                            Log.d("checked user id", html);
                            Memory.save(getApplicationContext(), "user_id", html);
                        }
                    }); */
    }

    @Override
    public void onBackPressed(){

        if(webView.canGoBack()){

            webView.goBack();
        }
        else{


            super.onBackPressed();
        }
    }

    public void reload(View v){

        webView.reload();
    }
}
