package com.example.robotcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.pedro.vlc.VlcListener;
import com.pedro.vlc.VlcVideoLibrary;

import org.videolan.libvlc.MediaPlayer;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity implements VlcListener, View.OnClickListener {

    private VlcVideoLibrary vlcVideoLibrary;
    private ImageButton mStartStop;
    private Button mSocket;
    private Button mSave;
    private EditText mEditTextURI;
    private SurfaceView surfaceView;
    private ProgressBar mProgressBar;
    private FrameLayout mLayoutServerSettings;
    private SharedPreferences mPrefs;
    private boolean ifClicked = false;                   //设置只能点击一次


    private String[] options = new String[]{":fullscreen", ":network-caching=300", ":rtsp-caching=300"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("MainActivity1", "OnCreate()");
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(R.layout.activity_player);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mStartStop = (ImageButton) findViewById(R.id.start_stop);
        mSocket = (Button) findViewById(R.id.socket);
        mEditTextURI = (EditText) findViewById(R.id.uri);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_circular);
        mLayoutServerSettings = (FrameLayout) findViewById(R.id.server_layout);
        mSave = (Button) findViewById(R.id.save);

        mLayoutServerSettings.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mStartStop.setVisibility(View.GONE);
        mSocket.setVisibility(View.GONE);

        mStartStop.setOnClickListener(this);
        mSocket.setOnClickListener(this);
        mSave.setOnClickListener(this);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(PlayerActivity.this);
        mEditTextURI.setText(mPrefs.getString("uri", getString(R.string.default_stream)));

        vlcVideoLibrary = new VlcVideoLibrary(this, this, surfaceView);
        vlcVideoLibrary.setOptions(Arrays.asList(options));
        vlcVideoLibrary.play(mEditTextURI.getText().toString());
    }

    @Override
    public void onPause() {
        super.onPause();
        vlcVideoLibrary.pause();
        Log.e("PlayerActivity", "onPause()");
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        vlcVideoLibrary.pause();
//        Log.e("PlayerActivity", "onStop()");
//    }

    @Override
    public void onRestart() {
        super.onRestart();
        vlcVideoLibrary.rePlay(mEditTextURI.getText().toString());
        Log.e("PlayerActivity", "onRestart()");
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        vlcVideoLibrary.stop();
//        Log.e("PlayerActivity", "onDestroy()");
//    }

    @Override
    public void onComplete() {
        Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show();
        ifClicked = true;
//        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onError() {
        Toast.makeText(this, "Error, make sure your stream is correct", Toast.LENGTH_SHORT).show();
        vlcVideoLibrary.stop();
        mProgressBar.setVisibility(View.GONE);
        mLayoutServerSettings.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBuffering(MediaPlayer.Event event) {     //缓冲
        if (event.getBuffering() < 100 && event.getBuffering() >= 0) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else
            mProgressBar.setVisibility(View.GONE);
        Log.e("PlayerActivity", String.valueOf(event.getBuffering()));
    }

    @Override
    public void onStopped() {
        Toast.makeText(this, "Error, the connection is broken", Toast.LENGTH_SHORT).show();
        vlcVideoLibrary.stop();
        mLayoutServerSettings.setVisibility(View.VISIBLE);
        ifClicked = false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_stop && !ifClicked) {
//            Toast.makeText(PlayerActivity.this, "vlcVideoLibrary.isPlaying()=" + vlcVideoLibrary.isPlaying(), Toast.LENGTH_SHORT).show();
            if (!vlcVideoLibrary.isPlaying()) {
                vlcVideoLibrary.play(mEditTextURI.getText().toString());
            }
        } else if (v.getId() == R.id.socket) {
            Intent intent = new Intent(PlayerActivity.this, DemoActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.save) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString("uri", mEditTextURI.getText().toString());
            editor.apply();
            mLayoutServerSettings.setVisibility(View.GONE);
        }
    }
}