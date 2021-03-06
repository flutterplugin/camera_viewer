package com.rich.camera_viewer.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

//import com.macrovideo.pull.lib.CheckSwitchDecodeButton;
//import com.macrovideo.pull.lib.CheckSwitchDecodeCsButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.hyfisheyepano.GLFisheyeView;
import com.macrovideo.sdk.defines.Defines;
import com.macrovideo.sdk.media.IPlaybackCallback;
import com.macrovideo.sdk.media.LibContext;
import com.macrovideo.sdk.media.LoginHandle;
import com.macrovideo.sdk.media.NVPanoPlayer;
import com.macrovideo.sdk.media.Player;
import com.macrovideo.sdk.media.VideoDecoder;
import com.macrovideo.sdk.objects.RecordVideoInfo;
import com.macrovideo.sdk.tools.Functions;
import com.rich.camera_viewer.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("NewApi")
public class NVPlayerPlaybackActivity extends Activity implements
        View.OnClickListener, OnTouchListener, OnSeekBarChangeListener, SurfaceTextureListener {

    static final short SHOWCODE_LOADING = 1001;// ????????????
    static final short SHOWCODE_NEW_IMAGE = 1002;// ?????????
    static final short SHOWCODE_VIDEO = 1004;// ??????
    static final short SHOWCODE_STOP = 2001;// ????????????
    static final short SHOWCODE_HAS_DATA = 3001;// ?????????

    static final short STAT_CONNECTING = 2001;// ?????????????????????
    static final short STAT_LOADING = 2002;// ??????????????????
    static final short STAT_DECODE = 2003;// ??????
    static final short STAT_STOP = 2004;// ??????
    static final short STAT_DISCONNECT = 2005;// ????????????
    static final short STAT_RESTART = 2006;// ????????????
    static final short STAT_MR_BUSY = 2007;// ????????????
    static final short STAT_MR_DISCONNECT = 2008;// ????????????

    private String m_strFileName = "";

    private boolean mPlaySound = true;
    private ImageView mBtnBack, mBtnBackHorizontal; // alter by mai 2015-4-29
    private Button mBtnStopAndPlay, mBtnStopAndPlayHorizontal;// alter by mai
    // 2015-4-29
    private Button mBtnSound, mBtnSoundHorizontal; // alter by mai 2015-4-29

    private TextView mTVTopServer = null;
    private RelativeLayout layoutTopBar = null;
    private boolean mIsPlaying = false;
    private boolean mIsToPlay = false;
    private RelativeLayout layoutBottomBar;
    private LinearLayout LayoutSeekBar;
    private LinearLayout layoutCenter = null;
    private boolean mIsOnDropUp = true;
    private boolean mIsFinish = false;

    private boolean m_bFinish = false;
    int mScreenWidth = 0;// ?????????
    int mScreenHeight = 0;// ?????????

    // ====add by mai 2015-1-30=====
    private TextView tvStartTime, tvStopTime, tvStartTimeHorizontal,
            tvStopTimeHorizontal;
    private Button btnLastFile, btnNextFile, btnCatpure;
    private Button btnLastFileHorizontal, btnNextFileHorizontal,
            btnCatpureHorizontal; // add by mai 2015-4-29
    private int listID = 0;
    private int nPlayerFileTime;
    private int nPlayerTime = 0;
    // ====end add by mai 2019-1-30===
    // add by mai 2015-3-16
    private View iamgeViewConctentView = null;
    private Dialog screenshotDialog = null;
    Bitmap bm = null;
    String folderName = "iCamSeeImages";
    // end add by mai 2015-3-16

    LinearLayout container = null;
    private ImageView img_v[] = new ImageView[Player.PLAYER_NUM];
    private int nScreenOrientation = Configuration.ORIENTATION_PORTRAIT;

    private Button btnRepeat = null;
    private SeekBar seekBarPlayProgress = null,
            seekBarPlayProgressHorizontal = null; // alter by mai 2015-4-29

    private NVPanoPlayer mvMediaPlayer = null;
    private LoginHandle deviceParam = null;// add 2015-12-1

    // add by mai 2015-4-29
    private LinearLayout bottomButtonHorizontal;
    private RelativeLayout bottomButton, RlPlayerDevice;
    private boolean bAnyway = true;
    private LinearLayout llVideoPalyBakc, llVideoPalyBakcHorizontal;

    //add by xie 2016-6-7
    private TextView Time_display_view;

    //add by xie 2016-6-8
    static String endTimetxt, startTimetxt;
    static int time_display, totaltime;
    // end add by mai 2015-8-4-29

    private TextureView textureView = null;
    private LinearLayout textureViewContainer = null;

    //private CheckSwitchDecodeCsButton mCbHWDecode;
    //private CheckSwitchDecodeButton mCbHWDecodeLand;
    private LinearLayout llLandscapeDecode;


    //	private boolean mIsHWChangedOther = false;
    private boolean mIsHWChangedFromPortrait = false;
    private boolean mIsHWChangedFromLandscape = false;
    private SharedPreferences mHWConfigPrefer;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // ??????????????????,?????????????????????
        if (keyCode == KeyEvent.KEYCODE_BACK) { // ??????????????????????????????
            exitCurrentActivity();

        }
        return false;

    }

    // ????????????????????????
    public boolean writeSystemParam() {
        SharedPreferences ap = getSharedPreferences(Defines._fileName,
                MODE_PRIVATE);
        SharedPreferences.Editor editer = ap.edit();
        editer.putBoolean("sound", mPlaySound);
        editer.commit();
        return true;
    }

    // ???????????????
    // msg??????????????????
    private void ShowAlert(String title, String msg) {
        try {
            new AlertDialog.Builder(NVPlayerPlaybackActivity.this)
                    .setTitle(title).setMessage(msg)
                    // .setTitle(getString(R.string.Alert_Title)).setMessage(msg)
                    .setIcon(R.drawable.icon)
                    .setPositiveButton(getString(R.string.alert_btn_OK),
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                    setResult(RESULT_OK);
                                }
                            }).show();

        } catch (Exception e) {

        }

    }

    @Override
    public void onPause() {
        if (mIsPlaying) {
            PausePlay();
        }
        OnPlayersPuase();
        super.onPause();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        OnPlayersResume(); // ????????????
        nToolsViewShowTickCount = 8;
        timerThreadID++;
        new TimerThread(timerThreadID).start();

        if (!mIsFinish) {
            if (mIsPlaying) {
                ResumePlay();
            } else {
                if (mIsToPlay) {
                    startPlay();
                } else {
                    stopPlay(true);
                }
            }
        }

        m_bFinish = false;
        NotificationManager notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notiManager.cancelAll();

    }

    @Override
    public void onDestroy() {
        // @@System.out.println("onDestroy");//add for test
        mvMediaPlayer = null;
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStop() {
        timerThreadID++;
        if (!m_bFinish) {// ??????????????????home?????????????????????????????????????????????
            NotificationManager notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentTitle(getString(R.string.app_name)).setContentText(getString(R.string.app_notice))
                    .setTicker(getString(R.string.app_name)) // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    .setWhen(System.currentTimeMillis())// ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    .setOngoing(false);// ture??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????,??????????????????????????????????????????????????????(??????????????????????????????????????????????????????)??????????????????????????????????????????????????????????????????????????????????????????,??????????????????????????????????????????????????????(??????????????????????????????????????????????????????????????????????????????,?????????????????????????????????????????????,?????????????????????????????????????????????????????????????????????????????????)
            if (Build.VERSION.SDK_INT >= 23) {
                builder.setSmallIcon(R.drawable.my_device_3);
                builder.setLargeIcon(Functions.readBitMap(this, R.drawable.icon));
            } else {
                builder.setSmallIcon(R.drawable.icon_1);
            }


            notiManager.notify(LocalDefines.notificationID, builder.build());

            Intent intent = new Intent(this, NVPlayerPlaybackActivity.class);
            Bundle data = new Bundle();
            data.putParcelable("login_handle", deviceParam);
            data.putInt("play_index", listID);
            intent.putExtras(data);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            LibContext.stopAll();

        } else {
            LibContext.stopAll();// add by lin 2015-12-11
            LibContext.ClearContext();
        }
        m_bFinish = true;
        super.onStop();
    }

    // ??????
    private void ShowLandscapeView() {
        seekBarPlayProgress.setVisibility(View.GONE);
        tvStartTime.setVisibility(View.GONE);
        tvStopTime.setVisibility(View.GONE);

//        if (mCbHWDecode.getVisibility() == View.VISIBLE) {
//            // ??????????????????????????????????????????
//        } else {
//            // ???????????????????????????????????????
//            llLandscapeDecode.setVisibility(View.GONE);
//        }
        synchronized (this) {

            nToolsViewShowTickCount = 5;
            bAnyway = false; // add by mai 2015-4-29
            showToolsViews();

            int nWidth = mScreenWidth;
            int nHeight = mScreenHeight;
            double dWidth = nHeight * 1.7777777;
            if (dWidth < nWidth)
                nWidth = (int) dWidth;

            if (layoutCenter != null) {
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(nWidth, nHeight);
                rlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                layoutCenter.setLayoutParams(rlp);
                layoutCenter.setPadding(0, 0, 0, 0);
            }

            container.setPadding(0, 0, 0, 0);
            textureViewContainer.setPadding(0, 0, 0, 0);

            nScreenOrientation = Configuration.ORIENTATION_LANDSCAPE;
            mvMediaPlayer.onOreintationChange(nScreenOrientation);

        }
    }

    // ??????
    private void ShowPortrailView() {

        //???????????????????????????????????????????????????????????????
        seekBarPlayProgress.setVisibility(View.VISIBLE);
        llLandscapeDecode.setVisibility(View.GONE);
        synchronized (this) {

            // dip?????????px
            // int padding_in_dp = 80;// 6 dps
            int padding_in_dp = 45; // add by xie 2016-6-1
            final float scale = getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

            bAnyway = true; // add by mai 2015-4-29
            showToolsViews();

            int nWidth = mScreenWidth;
//			int nHeight = (int) (nWidth * 0.8);
            // ?????????????????????
            int statusBarHeight = LocalDefines.getStatusBarHeight(this);
            int nHeight = (int) (mScreenHeight - (185 * scale + 0.5f) - statusBarHeight);


            if (layoutCenter != null) {
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(nWidth, nHeight);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                layoutCenter.setLayoutParams(rlp);
                layoutCenter.setPadding(0, padding_in_px, 0, 0);
            }

            int padding = (int) ((nHeight - padding_in_px - nWidth * 0.85) / 2);
            if (padding > 0) {
                container.setPadding(0, padding, 0, padding);
                textureViewContainer.setPadding(0, padding, 0, padding);
            }

            nScreenOrientation = Configuration.ORIENTATION_PORTRAIT;
            mvMediaPlayer.onOreintationChange(nScreenOrientation);

        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {

        super.onConfigurationChanged(config);//

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;// ?????????
        mScreenHeight = dm.heightPixels;// ?????????
        LocalDefines.loadResource(getResources());// ??????????????????

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            /*
             * If the screen is switched from portait mode to landscape mode
			 */
            ShowLandscapeView();

        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            /*
			 * If the screen is switched from landscape mode to portrait mode
			 */
            ShowPortrailView();
        }
        // @@System.out.println("onConfigurationChanged end");//add for test
    }

    /**
     * ?????????????????????????????????
     *
     * @param isVisible true???????????????false???????????????
     */
    void SetCloseButtonVisible(boolean isVisible) {
        if (isVisible) {
            img_v[Player.WND_ID_0].setVisibility(View.VISIBLE);
            img_v[Player.WND_ID_1].setVisibility(View.VISIBLE);
            img_v[Player.WND_ID_2].setVisibility(View.VISIBLE);
            img_v[Player.WND_ID_3].setVisibility(View.VISIBLE);
        } else {
            img_v[Player.WND_ID_0].setVisibility(View.GONE);
            img_v[Player.WND_ID_1].setVisibility(View.GONE);
            img_v[Player.WND_ID_2].setVisibility(View.GONE);
            img_v[Player.WND_ID_3].setVisibility(View.GONE);
        }
    }


    /**
     * ?????? GLES2SurfaceView?????????
     */
    public void ReleaseGLViewPlayer() {
        if (mvMediaPlayer != null) {
            mvMediaPlayer.DisableRender();
            mvMediaPlayer = null;
        }

    }

    // ??????
    public void OnPlayersPuase() {
        if (mvMediaPlayer != null) {
            mvMediaPlayer.getGLFisheyeView().onPause();
        }
    }

    // ????????????
    public void OnPlayersResume() {
        if (mvMediaPlayer != null) {
            mvMediaPlayer.getGLFisheyeView().onResume();
        }
    }

    // ????????????UI?????????
    private Handler handler = new Handler() {
        // @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg) {
            Log.i("demo_test", "msg = " + msg.arg1 + " " + msg.arg2);
            if (msg.arg1 == Defines.MSG_INIT_VIDEO_DECODER_FAIL) {
                //mCbHWDecode.setVisibility(View.INVISIBLE);
                llLandscapeDecode.setVisibility(View.GONE);
                textureViewContainer.setVisibility(View.GONE);
                mvMediaPlayer.setHWDecodeStatus(false, false);
                mvMediaPlayer.restartPlayVideo();
                Toast.makeText(NVPlayerPlaybackActivity.this, getString(R.string.str_use_hw_fail), Toast.LENGTH_SHORT).show();
                return;
            }

            // add by mai 20151316
            if (msg.arg1 == Defines.MSG_SCREENSHOT) {
                return;
            }
            // end add by mai 2015316

            // @@System.out.println("handleMessage");//add for test
            if (msg.arg1 == Defines.MSG_HIDE_TOOLVIEW) {
                if (nScreenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    hideToolsViews();
                }

            } else if (msg.arg1 == Defines.MSG_SET_SEEKBAR_VALUE) {

                // //@@System.out.println("setProgress: "+msg.arg2);//add for
                // test
                mIsOnDropUp = false;
                seekBarPlayProgress.setProgress(msg.arg2);
                seekBarPlayProgressHorizontal.setProgress(msg.arg2); // add by
                // mai
                // 2015-4-29

                nPlayerTime++;
                String strPlayer = null;
                if (nPlayerTime >= 60) {
                    strPlayer = (nPlayerTime / 60) + ":" + (nPlayerTime % 60);

                } else {
                    strPlayer = "00:" + nPlayerTime;


                }

                startTimetxt = strPlayer;
                tvStartTime.setText(strPlayer);
                tvStartTimeHorizontal.setText(strPlayer); // add by mai
            }

        }
    };


    static final int SIZE_CMDPACKET = 128;
    static final int SEND_BUFFER_SIZE = 512;
    static final int SESSION_FRAME_BUFFER_SIZE = 65536;
    static final int SEND_BUFFER_DATA_SIZE = 504; // 512 - 8 (header)
    static final int SEND_BUFFER_HEADER_SIZE = 8;
    static final int SP_DATA = 0x7f;

    static final int CMD_REQUEST = 0x9101;
    static final int CMD_AFFIRM = 0x9102;
    static final int CMD_EXIT = 0x9103;
    static final int CMD_ACCEPT = 0x9104;
    static final int CMD_CONNECTINFO = 0x9105;
    static final int CMD_STREAMHEAD = 0x9106;
    static final int CMD_UDPSHAKE = 0x9107;
    static final int CMD_ASKFORCNLNUM = 0x9108;
    static final int CMD_CHECKPSW = 0x9109;


    // ????????????
    // ????????????

    /**
     * Called when the activity is first created.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ??????????????????
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        NotificationManager notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notiManager.cancel(LocalDefines.notificationID);
        setContentView(R.layout.activity_nvplayer_playbackview);

        textureViewContainer = (LinearLayout) findViewById(R.id.playback_texture_container);
        textureView = (TextureView) findViewById(R.id.texture_view_playback);

        // ?????????????????????
        registerReceiver(this);

        // ??????????????????
        DisplayMetrics dm = getResources().getDisplayMetrics();

        mScreenWidth = dm.widthPixels;// ?????????
        mScreenHeight = dm.heightPixels;// ?????????

        init();
        Bundle data = this.getIntent().getExtras();
        if (data != null) {
            // ===add by mai 2015-1-30===
            listID = data.getInt("play_index"); // ????????????????????????
            deviceParam = data.getParcelable("login_handle"); // ??????????????????????????????LoginHandle
        }

        mTVTopServer = (TextView) findViewById(R.id.tvPlayerDevice);
        mTVTopServer.setText(m_strFileName);

        layoutTopBar = (RelativeLayout) findViewById(R.id.linearLayoutTopBar);
        layoutCenter = (LinearLayout) findViewById(R.id.playbackContainer);
        layoutBottomBar = (RelativeLayout) findViewById(R.id.linearLayoutBottomBarPlayBack);
        LayoutSeekBar = (LinearLayout) findViewById(R.id.bg_seekbar);
        LayoutSeekBar.getBackground().setAlpha(95);
        container = (LinearLayout) findViewById(R.id.playbackContainer1);
        // ???????????????
        mvMediaPlayer = new NVPanoPlayer(getApplication(), false);
        GLFisheyeView fisheyeView = new GLFisheyeView(this);
        fisheyeView.setMode(NVPanoPlayer.PANO_PLAY_MODE_13);
        fisheyeView.setOnTouchListener(this);
        mvMediaPlayer.setGlFishView(fisheyeView);

        mvMediaPlayer.GetHandler(handler);

        mvMediaPlayer.setTimeCallback(new IPlaybackCallback() {
            @Override
            public void setTime(long lTimestamp) {
                int nProgress;
                long lStartTime;
                long lEndTime;

                lStartTime = LocalDefines.listMapPlayerBackFile.get(listID).getRecSegment().getNvtStartTime().getlTime();
                lEndTime = LocalDefines.listMapPlayerBackFile.get(listID).getRecSegment().getNvtEndTime().getlTime();

                if ((lEndTime - lStartTime) != 0) {
                    nProgress = (int) ((lTimestamp - lStartTime) * 100 / (lEndTime - lStartTime));
                    seekBarPlayProgress.setProgress(nProgress);
                    seekBarPlayProgressHorizontal.setProgress(nProgress);
                }
            }

            @Override
            public void onReceiveFinishMSG(int nSegmentID) {
                Log.i("demo_test", "onReceiveFinishMSG " + nSegmentID);
                if (nSegmentID == LocalDefines.listMapPlayerBackFile.get(listID).getnFileID()
                        || nSegmentID == LocalDefines.listMapPlayerBackFile.get(listID).getRecSegment().getnSegmentID()) {
                    seekBarPlayProgress.setProgress(100);
                    seekBarPlayProgressHorizontal.setProgress(100);
                }
            }
        });

        // ????????????????????????
        container.addView(mvMediaPlayer.getGLFisheyeView());

        // ??????????????????
        LibContext.SetContext(mvMediaPlayer, null, null, null);

        Player.SelectWindow(Player.WND_ID_0);

        initDecodeSwitcher();

        btnRepeat = (Button) findViewById(R.id.btnRepeat1);
        btnRepeat.setOnClickListener(this);
        btnRepeat.setVisibility(View.GONE);

        mBtnBack = (ImageView) findViewById(R.id.btnPBBackToLogin);
        mBtnBack.setOnClickListener(this);

        mBtnBackHorizontal = (ImageView) findViewById(R.id.btnPBBackToLoginHprizontal);
        mBtnBackHorizontal.setOnClickListener(this);

        mBtnStopAndPlay = (Button) findViewById(R.id.btnPBStopAndPlay);
        mBtnStopAndPlay.setOnClickListener(this);
        mBtnStopAndPlayHorizontal = (Button) findViewById(R.id.btnPBStopAndPlayHorizontal);
        mBtnStopAndPlayHorizontal.setOnClickListener(this);

        mBtnSound = (Button) findViewById(R.id.btnPBAudio);
        mBtnSound.setOnClickListener(this);
        mBtnSoundHorizontal = (Button) findViewById(R.id.btnPBAudioHorizontal);
        mBtnSoundHorizontal.setOnClickListener(this);

        // ????????????????????????
        if (mPlaySound) {
            mBtnSound.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.play_back_sound_btn));
            mBtnSoundHorizontal.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.play_back_sound_btn_horziontal));
        } else {
            mBtnSound.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.play_back_sound_btn2));
            mBtnSoundHorizontal.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.play_back_sound_btn_horziontal2));
        }

        seekBarPlayProgress = (SeekBar) findViewById(R.id.seekBarPlayProgress);
        seekBarPlayProgress.setOnSeekBarChangeListener(this);

        seekBarPlayProgress.setMax(100);
        seekBarPlayProgress.setProgress(0);

        seekBarPlayProgressHorizontal = (SeekBar) findViewById(R.id.seekBarPlayProgressHorizontal); // add
        // by
        // mai
        // 2015-4-29
        seekBarPlayProgressHorizontal.setOnSeekBarChangeListener(this); // add
        // by
        // mai
        // 2015-4-29
        seekBarPlayProgressHorizontal.setMax(100); // add by mai 2015-4-29
        seekBarPlayProgressHorizontal.setProgress(0); // add by mai 2015-4-29

        mIsPlaying = false;
        mIsToPlay = true;

        ShowPortrailView();
        createDialogs();


    }


    private void initDecodeSwitcher() {

        //mCbHWDecode = (CheckSwitchDecodeCsButton) findViewById(R.id.cb_hw_decode_playback);
        //mCbHWDecodeLand = (CheckSwitchDecodeButton) findViewById(R.id.CWB_Decode);
        llLandscapeDecode = (LinearLayout) findViewById(R.id.ll_playback_land_cwb_decode);
        llLandscapeDecode.setVisibility(View.GONE);
        // ?????????????????????????????????
        if (VideoDecoder.sH264HWDecodeEnable || VideoDecoder.sH265HWDecodeEnable) {
            // ??????????????????????????????????????????
        } else {
            // ???????????????????????????????????????
            //mCbHWDecode.setVisibility(View.INVISIBLE);
        }
        mHWConfigPrefer = PreferenceManager.getDefaultSharedPreferences(this);
        mvMediaPlayer.setHWDecodeStatus(false, false);

        textureViewContainer.setVisibility(View.GONE);

        //mCbHWDecode.setOnCheckedChangeListener(new HWDecodeCheckChangeListener());

        //????????????????????????
        //mCbHWDecodeLand.setOnCheckedChangeListener(new HWDecodeCheckChangeListener());
    }

    private class HWDecodeCheckChangeListener implements OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mIsHWChangedFromPortrait && buttonView.getId() == R.id.CWB_Decode) {
                mIsHWChangedFromPortrait = false;
                return;
            }
//            if (mIsHWChangedFromLandscape && buttonView.getId() == R.id.cb_hw_decode_playback) {
//                mIsHWChangedFromLandscape = false;
//                return;
//            }
            if (mIsPlaying) {
                if (isChecked) {
                    // ???????????????
                    textureViewContainer.setVisibility(View.VISIBLE);
                    mvMediaPlayer.setHWDecodeStatus(isChecked, isChecked);
                } else {
                    // ????????????????????????
                    textureViewContainer.setVisibility(View.GONE);
                    mvMediaPlayer.setHWDecodeStatus(isChecked, isChecked);
                }
                mvMediaPlayer.restartPlayVideo();
            } else {
                if (isChecked) {
                    // ???????????????
                    textureViewContainer.setVisibility(View.VISIBLE);
                    mvMediaPlayer.setHWDecodeStatus(isChecked, isChecked);
                } else {
                    // ????????????????????????
                    textureViewContainer.setVisibility(View.GONE);
                    mvMediaPlayer.setHWDecodeStatus(isChecked, isChecked);
                }
                startPlay();
            }
//            if (buttonView.getId() == R.id.cb_hw_decode_playback) {
//                mIsHWChangedFromPortrait = true;
//                //mCbHWDecodeLand.setChecked(isChecked);
//
//            } else if (buttonView.getId() == R.id.CWB_Decode) {
//                mIsHWChangedFromLandscape = true;
//                //mCbHWDecode.setChecked(isChecked);
//            }
        }

    }

    // ===add by mai 2015-1-30====

    /**
     * ???????????????
     */
    private void init() {
        tvStartTime = (TextView) findViewById(R.id.tvStartTime);
        tvStopTime = (TextView) findViewById(R.id.tvStopTime);
        tvStartTimeHorizontal = (TextView) findViewById(R.id.tvStartTimeHorizontal); // add
        // by
        // mai
        // 2015-4-29
        tvStopTimeHorizontal = (TextView) findViewById(R.id.tvStopTimeHorizontal); // add
        // by
        // mai
        // 2015-4-29
        btnLastFile = (Button) findViewById(R.id.btnLastFlie);
        btnLastFile.setOnClickListener(this);
        btnLastFileHorizontal = (Button) findViewById(R.id.btnLastFlieHorizontal);
        btnLastFileHorizontal.setOnClickListener(this);

        btnNextFile = (Button) findViewById(R.id.btnNextFile);
        btnNextFile.setOnClickListener(this);
        btnNextFileHorizontal = (Button) findViewById(R.id.btnNextFileHorizontal);
        btnNextFileHorizontal.setOnClickListener(this);

        btnCatpure = (Button) findViewById(R.id.btnCatpure);
        btnCatpure.setOnClickListener(this);
        btnCatpureHorizontal = (Button) findViewById(R.id.btnCatpureHorizontal);
        btnCatpureHorizontal.setOnClickListener(this);

        bottomButtonHorizontal = (LinearLayout) findViewById(R.id.bottomButtonHorizontal); // add
        bottomButtonHorizontal.getBackground().setAlpha(95);                                                                                // by
        // mai2015-4-29
        bottomButton = (RelativeLayout) findViewById(R.id.bottomButton); // add
        // by
        // mai
        // 2015-4-29
        llVideoPalyBakc = (LinearLayout) findViewById(R.id.llVideoPalyBakc); // add
        // by
        // mai
        // 2015-4-29
        llVideoPalyBakcHorizontal = (LinearLayout) findViewById(R.id.llVideoPalyBakcHorizontal); // add
        // by
        // mai
        // 2015-4-29
        RlPlayerDevice = (RelativeLayout) findViewById(R.id.RlPlayerDevice); // add
        // by
        // xie
        // 2016-6-2
        //add by xie 2016-6-7
        Time_display_view = (TextView) findViewById(R.id.Time_display);
        Time_display_view.getBackground().setAlpha(95);
    }

    // ===end add by mai 2015-1-30====

    @SuppressWarnings("deprecation")
    private void stopPlay(boolean bFlag) {

        if (seekBarPlayProgress != null) {
            seekBarPlayProgress.setProgress(0);
            seekBarPlayProgress.setEnabled(false);

        }

        if (seekBarPlayProgressHorizontal != null) { // add by mai 2015-4-29
            seekBarPlayProgressHorizontal.setProgress(0);
            seekBarPlayProgressHorizontal.setEnabled(false);
        }
        mIsFinish = false;
        mIsPlaying = false;
        mTVTopServer.setText(m_strFileName);
        mvMediaPlayer.stopPlayBack();// add by lin 20151211
        mvMediaPlayer.pauseAudio();// add by lin 20151211
        mBtnStopAndPlay.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.play_back_play_btn));
        mBtnStopAndPlayHorizontal.setBackgroundDrawable(getResources()
                .getDrawable(R.drawable.play_back_play_btn_horziontal));
        mIsFinish = true;
    }

    @SuppressWarnings("deprecation")
    private void startPlay() { // ????????????

        mIsFinish = false;
        if (btnRepeat != null) {
            btnRepeat.setVisibility(View.GONE);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        mvMediaPlayer.pauseAudio();
        mTVTopServer.setText(m_strFileName);

        if (Player.CurrentSelPlayer() < 4) {
            Player.setPlaying(Player.CurrentSelPlayer(), true);
            mvMediaPlayer.EnableRender();
            seekBarPlayProgress.setProgress(0);
            seekBarPlayProgressHorizontal.setProgress(0); // add by mai
            // 2015-4-29
            // ========add by lin 20151211==========
            RecordVideoInfo recFile = LocalDefines.listMapPlayerBackFile
                    .get(listID); // ????????????????????????????????????

            if (recFile == null) {
                return;
            }
            m_strFileName = recFile.getStrFileName();
            mTVTopServer.setText(m_strFileName);
            // mTVTopServer.setText(recFile.getStrFileName());
            nPlayerFileTime = recFile.getuFileTimeLen();
            String strTime = "";
            nPlayerTime = 0;
            if (nPlayerFileTime >= 60) {
                strTime = (nPlayerFileTime / 60) + ":" + (nPlayerFileTime % 60);
                //add by xie 2016-6-8
                endTimetxt = strTime;
                totaltime = totalSeconds("00:00", endTimetxt);
            } else {
                strTime = "00:" + nPlayerFileTime;
                endTimetxt = strTime;
                totaltime = totalSeconds("00:00", endTimetxt);
            }


            tvStopTime.setText(strTime);
            tvStopTimeHorizontal.setText(strTime); // add by mai 2015-4-29
            // =======add by lin 2015-12-11=====
            if (mvMediaPlayer.startPlayBack(0, deviceParam, recFile, mPlaySound)) {
                mvMediaPlayer.playAudio();
                mBtnStopAndPlay.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.play_back_stop_btn));
                mBtnStopAndPlayHorizontal.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.play_back_stop_btn_horziontal));
                mIsPlaying = true;

            }

            if (seekBarPlayProgress != null) {
                seekBarPlayProgress.setEnabled(true);
            }

            if (seekBarPlayProgressHorizontal != null) { // add by mai 2015-4-29
                seekBarPlayProgressHorizontal.setEnabled(true);
            }

        }
    }

    private void PausePlay() {
        if (seekBarPlayProgress != null) {
            seekBarPlayProgress.setProgress(0);
            seekBarPlayProgress.setEnabled(false);

        }

        if (seekBarPlayProgressHorizontal != null) { // add by mai 2015-4-29
            seekBarPlayProgressHorizontal.setProgress(0);
            seekBarPlayProgressHorizontal.setEnabled(false);
        }
        mIsFinish = false;
        mIsPlaying = false;
        mBtnStopAndPlay.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.play_back_play_btn));
        mBtnStopAndPlayHorizontal.setBackgroundDrawable(getResources()
                .getDrawable(R.drawable.play_back_play_btn_horziontal));

        btnRepeat.setVisibility(View.VISIBLE);
        mIsPlaying = false;
        mIsPlaying = false;
        mTVTopServer.setText(m_strFileName);
        mvMediaPlayer.pauseAudio();
        mvMediaPlayer.FinishPlayback();
        mIsFinish = true;
    }

    private void ResumePlay() {

    }

    @SuppressWarnings("deprecation")
    public void onClick(View v) {

        nToolsViewShowTickCount = 5;
        if (v == null)
            return;
        int id = v.getId();// ????????????
// ?????????????????????
// ??????
// ???????????????
// ???????????????
// ??????
        if (id == R.id.btnRepeat1) {// replay
            stopPlay(true);
            startPlay();
        } else if (id == R.id.btnPBBackToLogin || id == R.id.btnPBBackToLoginHprizontal) { // add by mai 2015-4-29

            exitCurrentActivity();
        } else if (id == R.id.btnPBStopAndPlay || id == R.id.btnPBStopAndPlayHorizontal) { // add by mai 2015-4-29
            mIsPlaying = !mIsPlaying;
            if (mIsPlaying) {
                startPlay();

            } else {
                stopPlay(true);
            }
        } else if (id == R.id.btnPBAudio || id == R.id.btnPBAudioHorizontal) { // add by mai 2015-4-29
            mPlaySound = !mPlaySound;
            if (mPlaySound) {
                mBtnSound.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.play_back_sound_btn));
                mBtnSoundHorizontal.setBackgroundDrawable(getResources()
                        .getDrawable(
                                R.drawable.play_back_sound_btn_horziontal));
                mvMediaPlayer.playAudio();
            } else {
                mBtnSound.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.play_back_sound_btn2));
                mBtnSoundHorizontal.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.play_back_sound_btn_horziontal2));
                mvMediaPlayer.pauseAudio();
            }

            int nChn = Player.CurrentSelPlayer();
            if (nChn >= 0 && nChn < 4) {
                mvMediaPlayer.SetAudioParam(mPlaySound);
            }

            writeSystemParam();
        } else if (id == R.id.btnLastFlie || id == R.id.btnLastFlieHorizontal) { // add by mai 2015-4-29
            if ((listID - 1) >= 0) { // ?????????????????????
                listID = listID - 1;
                stopPlay(false);
                final Handler han = new Handler();
                Runnable runn = new Runnable() {

                    @Override
                    public void run() {

                        if (mIsFinish) { // ??????????????????
                            startPlay();

                        } else {

                            han.postDelayed(this, 1000);
                        }

                    }
                };

                han.postDelayed(runn, 1000);
            } else {
                Toast.makeText(NVPlayerPlaybackActivity.this,
                        getString(R.string.FileFirst), Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (id == R.id.btnNextFile || id == R.id.btnNextFileHorizontal) { // add by mai 2015-4-29
            if ((listID + 1) < LocalDefines.listMapPlayerBackFile.size()) { // ???????????????
                listID = listID + 1;
                stopPlay(true);
                final Handler han = new Handler();
                Runnable runn = new Runnable() {

                    @Override
                    public void run() {

                        if (mIsFinish) { // ??????????????????
                            startPlay();

                        } else {

                            han.postDelayed(this, 1000);
                        }

                    }
                };

                han.postDelayed(runn, 1000);

            } else {
                Toast.makeText(NVPlayerPlaybackActivity.this,
                        getString(R.string.FileFinally), Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (id == R.id.btnCatpure || id == R.id.btnCatpureHorizontal) { // add by mai 2015-4-29
            // ??????  ??????????????????
            if (Build.VERSION.SDK_INT >= 23
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat
                        .requestPermissions(
                                this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSION_REQUEST_STORAGE);
                return;
            }
            screenshotDialog.show();
            ScreenShot();
        }

    }

    private static final int MY_PERMISSION_REQUEST_STORAGE = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_REQUEST_STORAGE) {
            if (permissions[0]
                    .equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // ????????????????????????????????????
                new AlertDialog.Builder(this)
                        .setTitle(
                                getResources().getString(
                                        R.string.str_permission_request))
                        // .setMessage("???????????????????????????????????????????????????????????????????????????-??????-V380??????????????????")
                        .setMessage(
                                getResources().getString(
                                        R.string.str_permission_storage1))
                        .setNegativeButton(
                                getResources().getString(
                                        R.string.str_permission_neglect), null)
                        .setPositiveButton(
                                getResources().getString(
                                        R.string.str_permission_setting2),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                        Intent intent = new Intent();
                                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                        Uri uri = Uri.fromParts("package",
                                                NVPlayerPlaybackActivity.this.getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                        // ((HomePageActivity)relateAtivity).closeActivity();
                                    }
                                }).show();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        String time_display = getCheckTimeBySeconds((totaltime * progress / 100), "00:00");
        Time_display_view.setText(time_display);


        if (!mIsOnDropUp && mIsPlaying && progress >= 100) {
            mIsFinish = false;
            mIsPlaying = false;
            mBtnStopAndPlay.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.play_back_play_btn));
            mBtnStopAndPlayHorizontal.setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.play_back_play_btn_horziontal));
            seekBarPlayProgress.setEnabled(false);
            seekBarPlayProgressHorizontal.setEnabled(false);
            btnRepeat.setVisibility(View.VISIBLE);
            mTVTopServer.setText(m_strFileName);

            mvMediaPlayer.pauseAudio();
            mvMediaPlayer.FinishPlayback();
            mIsFinish = true;
        }

        if (seekBarPlayProgress != null) {
            if (seekBarPlayProgress.getProgress() == 100) {
                Time_display_view.setVisibility(View.GONE);
                seekBarPlayProgress.setProgress(0);

            }
        }

        if (seekBarPlayProgressHorizontal != null) { // add by mai 2015-4-29
            if (seekBarPlayProgressHorizontal.getProgress() == 100) {
                Time_display_view.setVisibility(View.GONE);
                seekBarPlayProgressHorizontal.setProgress(0);
            }
        }

        mIsOnDropUp = true;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //add by xie 2016-6-8
        Time_display_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Time_display_view.setVisibility(View.GONE);
        int nValue = seekBar.getProgress();
        mvMediaPlayer.setPlayBackProgress(nValue);
    }

    // ???????????????
    private void showToolsViews() {
        nToolsViewShowTickCount = 5;
        layoutBottomBar.setVisibility(View.VISIBLE);
        layoutTopBar.setVisibility(View.VISIBLE);
        // add by mai 2015-4-29
        if (bAnyway) // ???????????????
        {

            bottomButton.setVisibility(View.VISIBLE);
            bottomButtonHorizontal.setVisibility(View.GONE);
            llVideoPalyBakc.setVisibility(View.VISIBLE);
            llVideoPalyBakcHorizontal.setVisibility(View.GONE);
            RlPlayerDevice.setVisibility(View.VISIBLE);

        } else {

            bottomButtonHorizontal.setVisibility(View.VISIBLE);
            bottomButton.setVisibility(View.GONE);
            llVideoPalyBakc.setVisibility(View.GONE);
            llVideoPalyBakcHorizontal.setVisibility(View.VISIBLE);
            RlPlayerDevice.setVisibility(View.GONE);
        }
        // end add by mai 2015-4-29

    }

    // ??????
    private void hideToolsViews() {

        nToolsViewShowTickCount = 0;
        layoutBottomBar.setVisibility(View.GONE);
        layoutTopBar.setVisibility(View.GONE);
        // add by mai 2015-4-29
        bottomButtonHorizontal.setVisibility(View.GONE);
        bottomButton.setVisibility(View.GONE);
        // end add by mai 2015-4-29
    }

    // /timer task
    private int nToolsViewShowTickCount = 8;

    private int timerThreadID = 0;

    // ??????????????????
    class TimerThread extends Thread {

        int mThreadID = 0;

        public TimerThread(int nThreadID) {
            mThreadID = nThreadID;
        }

        public void run() {
            while (mThreadID == timerThreadID) {

                nToolsViewShowTickCount -= 1;
                if (nToolsViewShowTickCount <= 0 && mThreadID == timerThreadID) {
                    Message message = new Message();
                    message.arg1 = Defines.MSG_HIDE_TOOLVIEW;
                    handler.sendMessage(message);
                    nToolsViewShowTickCount = 0;
                }

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

    private void createDialogs() { // ??????????????? ?????????????????????

        // add by luo 20141226
        iamgeViewConctentView = LayoutInflater.from(this).inflate(
                R.layout.screenshotdialog, null);
        screenshotDialog = new Dialog(this, R.style.progressDialog);
        screenshotDialog.setContentView(iamgeViewConctentView);
        screenshotDialog
                .setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {
                        // TODO Auto-generated method stub

                        Message msg = handler.obtainMessage();
                        msg.arg1 = Defines.MSG_SCREENSHOT;
                        handler.sendMessage(msg);
                    }
                });

        screenshotDialog
                .setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // TODO Auto-generated method stub

                    }

                });
    }

    // add by luo 20141115 ????????????
    @SuppressLint("SimpleDateFormat")
    private void ScreenShot() {

        // ????????????SDCard
        String strSavePath = Functions.GetSDPath();
        if (strSavePath == null) {

            screenshotDialog.dismiss();// ???????????????

            Toast.makeText(getApplicationContext(),
                    getString(R.string.noticeSDCardNotExist),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ?????????????????????
        bm = mvMediaPlayer.Screenshot();

        if (bm != null) {
            bm.recycle();
            bm = null;
            bm = Bitmap.createBitmap(Defines._capWidth, Defines._capHeight,
                    Config.RGB_565);
            bm.copyPixelsFromBuffer(Defines._capbuffer);
            Defines._capbuffer.position(0);
        }

        if (bm != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateString = formatter.format(new Date());
            String strFileName = dateString + "(" + m_strFileName + ")"
                    + ".jpg";
            if (saveToSDCard(bm, strFileName)) {// ???????????????
                screenshotDialog.dismiss();
                Toast.makeText(getApplicationContext(),
                        getString(R.string.noticeFileSaveToAlbumsOK),
                        Toast.LENGTH_SHORT).show();
            } else {

                strFileName = strSavePath + "/" + strFileName;
                if (saveToSDCard(bm, strFileName)) {
                    screenshotDialog.dismiss();
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.noticeScreenShotOK),
                            Toast.LENGTH_SHORT).show();
                } else {
                    screenshotDialog.dismiss();

                    Toast.makeText(getApplicationContext(),
                            getString(R.string.noticeScreenShotFail),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else {//  ????????????
            screenshotDialog.dismiss();

            Toast.makeText(getApplicationContext(),
                    getString(R.string.noticeScreenShotFail),
                    Toast.LENGTH_SHORT).show();
        }

    }

    // end add by luo 20141115

    // ?????????SD???
    private boolean saveToSDCard(Bitmap image, String strFileName) {
        boolean bResult = false;

        if (image == null)
            return bResult;
        try {

            File file = new File(Functions.GetSDPath() + File.separator + LocalDefines.SDCardPath);
            if (!file.exists())
                file.mkdir();

            File imageFile = new File(file.getAbsolutePath() + File.separator + strFileName);
            FileOutputStream out = new FileOutputStream(imageFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            bResult = true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bResult;
    }

    // end add by luo 20141226


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO ???????????????????????????
        return false;
    }

    //add by xie 2016-6-8
    private static String getCheckTimeBySeconds(int progress, String startTime) {

        String return_h = "", return_m = "", return_s = "";

        String[] st = startTime.split(":");

//		int st_h = Integer.valueOf(st[0]);
        int st_m = Integer.valueOf(st[0]);
        int st_s = Integer.valueOf(st[1]);

        int m = (progress % 3600) / 60;

        int s = progress % 60;

        if ((s + st_s) >= 60) {

            int tmpSecond = (s + st_s) % 60;

            m = m + 1;

            if (tmpSecond >= 10) {
                return_s = tmpSecond + "";
            } else {
                return_s = "0" + (tmpSecond);
            }

        } else {
            if ((s + st_s) >= 10) {
                return_s = s + st_s + "";
            } else {
                return_s = "0" + (s + st_s);
            }

        }

        if ((m + st_m) >= 60) {

            int tmpMin = (m + st_m) % 60;

//			h = h + 1;

            if (tmpMin >= 10) {
                return_m = tmpMin + "";
            } else {
                return_m = "0" + (tmpMin);
            }

        } else {
            if ((m + st_m) >= 10) {
                return_m = (m + st_m) + "";
            } else {
                return_m = "0" + (m + st_m);
            }

        }

        return return_m + ":" + return_s;
    }

    /**
     * ?????????????????????????????????
     */
    //add by xie 2016-6-8
    private static int totalSeconds(String startTime, String endTime) {

        String[] st = startTime.split(":");
        String[] et = endTime.split(":");

//		int st_h = Integer.valueOf(st[0]);
        int st_m = Integer.valueOf(st[0]);
        int st_s = Integer.valueOf(st[1]);

//		int et_h = Integer.valueOf(et[0]);
        int et_m = Integer.valueOf(et[0]);
        int et_s = Integer.valueOf(et[1]);

        int totalSeconds = (et_m - st_m) * 60
                + (et_s - st_s);

        return totalSeconds;

    }


    /**
     * ??????????????????????????????????????????????????????(??????????????????OnKeyDown???????????????)
     */
    private void exitCurrentActivity() {
        if (mIsPlaying == false) { // ??????
            stopPlay(false);
            //Intent intent = new Intent(NVPlayerPlaybackActivity.this, MainActivity.class);
            m_bFinish = true;
            Bundle data = new Bundle();
            data.putParcelable("login_handle", deviceParam);
            data.putInt("play_index", listID);
            data.putBoolean("is_file_list_visible", true);
            data.putBoolean("rec_load_from_db", true);
            //intent.putExtras(data);
            //startActivity(intent);
            LocalDefines.PLAY_BACK_BACK = true;
            unRegisterReceiver();
            NVPlayerPlaybackActivity.this.finish();

            overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
            return;
        }

        View view = View.inflate(this, R.layout.show_alert_dialog, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.alert_stop_play));
        TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
        tv_content.setVisibility(View.GONE);
        new AlertDialog.Builder(this)//
                .setView(view).setNegativeButton(getString(R.string.alert_btn_Cancel), null)
                .setPositiveButton(getString(R.string.alert_btn_OK), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        try {
                            stopPlay(false);
                        } catch (Exception e) {

                        }

                        setResult(RESULT_OK);

                        mIsPlaying = false;
                        //Intent intent = new Intent(NVPlayerPlaybackActivity.this, MainActivity.class);
                        m_bFinish = true;
                        Bundle data = new Bundle();
                        data.putParcelable("login_handle", deviceParam);
                        data.putInt("play_index", listID);
                        data.putBoolean("is_file_list_visible", true);
                        data.putBoolean("rec_load_from_db", true);
                        //intent.putExtras(data);
                        //startActivity(intent);
                        LocalDefines.PLAY_BACK_BACK = true;
                        unRegisterReceiver();
                        NVPlayerPlaybackActivity.this.finish();
                        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);

                    }

                }).show();
    }


    private CloseActivityReceiver mReceiver;

    /**
     * ????????????????????????????????????????????????????????????Activity??????
     *
     * @param context
     */
    private void registerReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocalDefines.getReceiverActionString(context));
        mReceiver = new CloseActivityReceiver();
        registerReceiver(mReceiver, filter);
    }

    /**
     * ???????????????????????????
     */
    private void unRegisterReceiver() {
        if (mReceiver != null) {
            try {
                unregisterReceiver(mReceiver);
            } catch (IllegalArgumentException e) {
                return;
            }
        }
    }

    /**
     * ??????????????????????????????????????????Activity?????????
     *
     * @author Administrator
     */
    class CloseActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = LocalDefines.getReceiverActionString(NVPlayerPlaybackActivity.this);
            if (intent != null && intent.getAction().equals(action)) {
                stopCurrentActivityFromBroadcast();
            }
        }
    }

    /**
     * ???????????????????????????????????? add 2016???10???25???
     */
    private void stopCurrentActivityFromBroadcast() {
        if (mIsPlaying == false) {
            stopPlay(false);
            m_bFinish = true;
            LocalDefines.PLAY_BACK_BACK = true;
            unRegisterReceiver();
            NVPlayerPlaybackActivity.this.finish();

        } else {
            try {
                stopPlay(false);
            } catch (Exception e) {

            }

            setResult(RESULT_OK);

            mIsPlaying = false;
            m_bFinish = true;

            LocalDefines.PLAY_BACK_BACK = true;
            unRegisterReceiver();
            NVPlayerPlaybackActivity.this.finish();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1, int arg2) {
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int arg1, int arg2) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture arg0) {

    }
}