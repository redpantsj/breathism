package com.breathism.app.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

import com.pushwoosh.PushManager;
import com.pushwoosh.BasePushMessageReceiver;
import com.pushwoosh.BaseRegistrationReceiver;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    WebView mWebView;
    String main_page_url = "http://breathism.com/";
    ProgressDialog mProgress;
    final Activity activity = this;
    private BackPressCloseHandler backPressCloseHandler;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        backPressCloseHandler = new BackPressCloseHandler(this);

        startActivity(new Intent(this, splash.class));

        mWebView = (WebView)findViewById(R.id.webView);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.loadUrl(main_page_url);

        mWebView.setWebViewClient(new sumWebViewClient());


        //Register receivers for push notifications
        registerReceivers();

        //Create and start push manager
        PushManager pushManager = PushManager.getInstance(this);

        //Start push manager, this will count app open for Pushwoosh stats as well
        try {
            pushManager.onStartup(this);
        }
        catch(Exception e) {
            //push notifications are not available or AndroidManifest.xml is not configured properly
        }

        //Register for push!
        pushManager.registerForPushNotifications();

        checkMessage(getIntent());

        //Clear application badge number
        pushManager.setBadgeNumber(0);



        /**
         * 하단 버튼을 여기서 처리
         */


        ImageButton gotohome = (ImageButton)findViewById(R.id.btn_home);
        gotohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("OnClick", "gotohome");
                mWebView.loadUrl(main_page_url);

            }
        });

        ImageButton gotoback = (ImageButton)findViewById(R.id.btn_back);
        gotoback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("OnClick", "gotoback");
                if (mWebView.canGoBack())
                    mWebView.goBack();

            }
        });

        ImageButton gorefresh = (ImageButton)findViewById(R.id.btn_refresh);
        gorefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("OnClick", "gorefresh");
                mWebView.reload();

            }
        });

        ImageButton goforward = (ImageButton)findViewById(R.id.btn_forward);
        goforward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("OnClick", "goforward");
                if (mWebView.canGoForward())
                    mWebView.goForward();

            }
        });

        ImageButton gokatalk = (ImageButton)findViewById(R.id.btn_katalk);
        gokatalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("OnClick", "gokatalk");
                Intent intentSubActivity = new Intent(Intent.ACTION_VIEW, Uri.parse("http://plus.kakao.com/home/jyucfr3o"));

                startActivity(intentSubActivity);

            }
        });









    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK) {

            if(mWebView.canGoBack()){
                if(mWebView.getOriginalUrl().equalsIgnoreCase(main_page_url) || mWebView.getOriginalUrl().equalsIgnoreCase(main_page_url+"index.php")){
                    onBackPressed();

                    //android.os.Process.killProcess(android.os.Process.myPid());
                }else{
                    mWebView.goBack();
                }

            }else if(!mWebView.getOriginalUrl().equalsIgnoreCase(main_page_url) && !mWebView.getOriginalUrl().equalsIgnoreCase(main_page_url+"index.php")){
                mWebView.loadUrl(main_page_url);
            }else{

                onBackPressed();
                //android.os.Process.killProcess(android.os.Process.myPid());
            }

            return true;

        }


        return super.onKeyDown(keyCode, event);
    }



    private class sumWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {


            if(!url.startsWith("http")) {

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);



            }
            else if (url.startsWith("http://plus.kakao.com/home/jyucfr3o")) {
                Intent intentSubActivity = new Intent(Intent.ACTION_VIEW, Uri.parse("http://plus.kakao.com/home/jyucfr3o"));
                startActivity(intentSubActivity);
            }
            else {
                view.loadUrl(url);
            }

            return true;
        }


        /**
         * 웹페이지 로딩이 시작할 때 처리
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (mProgress == null) {
                mProgress = new ProgressDialog(activity);
                mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgress.setTitle("Loading...");
                mProgress.setMessage("Please wait for few second...");
                mProgress.setCancelable(false);

                mProgress.show();
            }
        }

        /**
         * 웹페이지 로딩중 에러가 발생했을때 처리
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(activity, "Loading Error" + description, Toast.LENGTH_SHORT).show();

            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
        }

        /**
         * 웹페이지 로딩이 끝났을 때 처리
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
        }




    }





    //Registration receiver
    BroadcastReceiver mBroadcastReceiver = new BaseRegistrationReceiver()
    {
        @Override
        public void onRegisterActionReceive(Context context, Intent intent)
        {
            checkMessage(intent);
        }
    };

    //Push message receiver
    private BroadcastReceiver mReceiver = new BasePushMessageReceiver()
    {
        @Override
        protected void onMessageReceive(Intent intent)
        {
            //JSON_DATA_KEY contains JSON payload of push notification.
            //showMessage("개발자용 : push message is " + intent.getExtras().getString(JSON_DATA_KEY));
            showMessage("메세지를 수신했습니다");
        }
    };

    //Registration of the receivers
    public void registerReceivers()
    {
        IntentFilter intentFilter = new IntentFilter(getPackageName() + ".action.PUSH_MESSAGE_RECEIVE");

        registerReceiver(mReceiver, intentFilter, getPackageName() +".permission.C2D_MESSAGE", null);

        registerReceiver(mBroadcastReceiver, new IntentFilter(getPackageName() + "." + PushManager.REGISTER_BROAD_CAST_ACTION));
    }

    public void unregisterReceivers()
    {
        //Unregister receivers on pause
        try
        {
            unregisterReceiver(mReceiver);
        }
        catch (Exception e)
        {
            // pass.
        }

        try
        {
            unregisterReceiver(mBroadcastReceiver);
        }
        catch (Exception e)
        {
            //pass through
        }
    }



    @Override
    public void onResume()
    {
        super.onResume();

        //Re-register receivers on resume
        registerReceivers();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        //Unregister receivers on pause
        unregisterReceivers();
    }



    private void checkMessage(Intent intent)
    {
        if (null != intent)
        {
            if (intent.hasExtra(PushManager.PUSH_RECEIVE_EVENT))
            {
                doOnMessageReceive(intent.getExtras().getString(PushManager.PUSH_RECEIVE_EVENT));
            }
            else if (intent.hasExtra(PushManager.REGISTER_EVENT))
            {
                //showMessage("개발자용 메세지 : register");
                showMessage("푸시알람을 수신합니다");

            }
            else if (intent.hasExtra(PushManager.UNREGISTER_EVENT))
            {
                showMessage("unregister");
            }
            else if (intent.hasExtra(PushManager.REGISTER_ERROR_EVENT))
            {
                showMessage("register error");
            }
            else if (intent.hasExtra(PushManager.UNREGISTER_ERROR_EVENT))
            {
                showMessage("unregister error");
            }

            resetIntentValues();
        }
    }



    public void doOnMessageReceive(String message)
    {


        // Parse custom JSON data string.
        // You can set background color with custom JSON data in the following format: { "r" : "10", "g" : "200", "b" : "100" }
        // Or open specific screen of the app with custom page ID (set ID in the { "id" : "2" } format)
        try
        {
            JSONObject messageJson = new JSONObject(message);
            JSONObject customJson = new JSONObject(messageJson.getString("u"));

            if (customJson.has("r") && customJson.has("g") && customJson.has("b"))
            {
                int r = customJson.getInt("r");
                int g = customJson.getInt("g");
                int b = customJson.getInt("b");
                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                rootView.setBackgroundColor(Color.rgb(r, g, b));
            }
            if (customJson.has("id"))
            {
                String customdata_url = customJson.getString("id");
                mWebView.loadUrl(customdata_url);
            }
        }
        catch (JSONException e)
        {
            // No custom JSON. Pass this exception
        }
    }



    /**
     * Will check main Activity intent and if it contains any PushWoosh data, will clear it
     */
    private void resetIntentValues()
    {
        Intent mainAppIntent = getIntent();

        if (mainAppIntent.hasExtra(PushManager.PUSH_RECEIVE_EVENT))
        {
            mainAppIntent.removeExtra(PushManager.PUSH_RECEIVE_EVENT);
        }
        else if (mainAppIntent.hasExtra(PushManager.REGISTER_EVENT))
        {
            mainAppIntent.removeExtra(PushManager.REGISTER_EVENT);
        }
        else if (mainAppIntent.hasExtra(PushManager.UNREGISTER_EVENT))
        {
            mainAppIntent.removeExtra(PushManager.UNREGISTER_EVENT);
        }
        else if (mainAppIntent.hasExtra(PushManager.REGISTER_ERROR_EVENT))
        {
            mainAppIntent.removeExtra(PushManager.REGISTER_ERROR_EVENT);
        }
        else if (mainAppIntent.hasExtra(PushManager.UNREGISTER_ERROR_EVENT))
        {
            mainAppIntent.removeExtra(PushManager.UNREGISTER_ERROR_EVENT);
        }

        setIntent(mainAppIntent);
    }

    private void showMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);

        checkMessage(intent);
    }


}
