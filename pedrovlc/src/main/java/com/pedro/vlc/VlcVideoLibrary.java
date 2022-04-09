package com.pedro.vlc;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;

/**
 * Created by pedro on 25/06/17.
 * Play and stop need be in other thread or app can freeze
 */
public class VlcVideoLibrary implements MediaPlayer.EventListener {

    private int width = 0, height = 0;
    private LibVLC vlcInstance;
    private MediaPlayer player;
    private VlcListener vlcListener;
    //The library will select one of this class for rendering depend of constructor called
    private SurfaceView surfaceView;
    private TextureView textureView;
    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private SurfaceHolder surfaceHolder;
    private List<String> options = new ArrayList<>();

    public VlcVideoLibrary(Context context, VlcListener vlcListener, SurfaceView surfaceView) {
        this.vlcListener = vlcListener;
        this.surfaceView = surfaceView;
        vlcInstance = new LibVLC(context, new VlcOptions().getDefaultOptions());
        options.add(":fullscreen");
    }

    public VlcVideoLibrary(Context context, VlcListener vlcListener, TextureView textureView) {
        this.vlcListener = vlcListener;
        this.textureView = textureView;
        vlcInstance = new LibVLC(context, new VlcOptions().getDefaultOptions());
        options.add(":fullscreen");
    }

    public VlcVideoLibrary(Context context, VlcListener vlcListener, SurfaceTexture surfaceTexture) {
        this.vlcListener = vlcListener;
        this.surfaceTexture = surfaceTexture;
        vlcInstance = new LibVLC(context, new VlcOptions().getDefaultOptions());
        options.add(":fullscreen");
    }

    public VlcVideoLibrary(Context context, VlcListener vlcListener, Surface surface) {
        this.vlcListener = vlcListener;
        this.surface = surface;
        surfaceHolder = null;
        vlcInstance = new LibVLC(context, new VlcOptions().getDefaultOptions());
        options.add(":fullscreen");
    }

    public VlcVideoLibrary(Context context, VlcListener vlcListener, Surface surface,
                           SurfaceHolder surfaceHolder) {
        this.vlcListener = vlcListener;
        this.surface = surface;
        this.surfaceHolder = surfaceHolder;
        vlcInstance = new LibVLC(context, new VlcOptions().getDefaultOptions());
        options.add(":fullscreen");
    }

    public VlcVideoLibrary(Context context, VlcListener vlcListener, Surface surface, int width,
                           int height) {
        this.vlcListener = vlcListener;
        this.surface = surface;
        this.width = width;
        this.height = height;
        surfaceHolder = null;
        vlcInstance = new LibVLC(context, new VlcOptions().getDefaultOptions());
        options.add(":fullscreen");
    }

    public VlcVideoLibrary(Context context, VlcListener vlcListener, Surface surface,
                           SurfaceHolder surfaceHolder, int width, int height) {
        this.vlcListener = vlcListener;
        this.surface = surface;
        this.surfaceHolder = surfaceHolder;
        this.width = width;
        this.height = height;
        vlcInstance = new LibVLC(context, new VlcOptions().getDefaultOptions());
        options.add(":fullscreen");
    }

    /**
     * This method should be called after constructor and before play methods.
     *
     * @param options seeted to VLC player.
     */
    public void setOptions(List<String> options) {
        this.options = options;
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    //用于第一次创建media
    public void play(String endPoint) {
        if (player == null || player.isReleased()) {
            setMedia(new Media(vlcInstance, Uri.parse(endPoint)));
        }
//        else if (!player.isPlaying()) {
//            Log.e("PlayerActivity.b", String.valueOf(player.isPlaying()));
//            if (player == null) {
//                Log.e("PlayerActivity.b", String.valueOf(player == null));
//                setMedia(new Media(vlcInstance, Uri.parse(endPoint)));
//            }
//        }
    }

    //用于继续播放该视频
    public void rePlay(String endPoint) {
        if (player == null || player.isReleased()) {
            setMedia(new Media(vlcInstance, Uri.parse(endPoint)));
        } else if (!player.isPlaying()) {
            player.play();
        }
    }

    public void stop() {
//        if (player != null && player.isPlaying()) {
//            player.stop();
//            player.release();
//        }
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    public void pause() {
        if (player != null && player.isPlaying()) {
            player.stop();
            player.release();
        }
    }

    private void setMedia(Media media) {
        //delay = network buffer + file buffer
//        media.addOption(":fullscreen");
//        media.addOption(":network-caching=" + Constants.BUFFER);
//        media.addOption(":rtsp-caching=" + Constants.RTSP_BUFFER);
        //media.addOption(":file-caching=" + Constants.BUFFER);
        if (options != null) {
            for (String s : options) {
                media.addOption(s);
            }
        }
        media.setHWDecoderEnabled(true, false);
        player = new MediaPlayer(vlcInstance);
        player.setMedia(media);
        player.setEventListener(this);

        IVLCVout vlcOut = player.getVLCVout();
        //set correct class for render depend of constructor called
        if (surfaceView != null) {
            vlcOut.setVideoView(surfaceView);
            width = surfaceView.getWidth();
            height = surfaceView.getHeight();
        } else if (textureView != null) {
            vlcOut.setVideoView(textureView);
            width = textureView.getWidth();
            height = textureView.getHeight();
        } else if (surfaceTexture != null) {
            vlcOut.setVideoSurface(surfaceTexture);
        } else if (surface != null) {
            vlcOut.setVideoSurface(surface, surfaceHolder);
        } else {
            throw new RuntimeException("You cant set a null render object");
        }
        if (width != 0 && height != 0) vlcOut.setWindowSize(width, height);
        vlcOut.attachViews();
        player.setVideoTrackEnabled(true);
        player.play();
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.Playing:
                vlcListener.onComplete();
                break;
            case MediaPlayer.Event.EncounteredError:
                vlcListener.onError();
                break;
            case MediaPlayer.Event.Buffering:
                vlcListener.onBuffering(event);
                break;
            case MediaPlayer.Event.Stopped:
                vlcListener.onStopped();
                break;
            default:
                Log.e("MainActivity.this", String.valueOf(event.type));
                break;
        }
    }
}