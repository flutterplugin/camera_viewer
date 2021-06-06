package com.rich.camera_viewer.demo;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.macrovideo.sdk.defines.Defines;
import com.macrovideo.sdk.defines.ResultCode;
import com.macrovideo.sdk.media.ILoginDeviceCallback;
import com.macrovideo.sdk.media.LoginHandle;
import com.macrovideo.sdk.media.LoginHelper;
import com.macrovideo.sdk.objects.LoginParam;
import com.rich.camera_viewer.R;

import static com.rich.camera_viewer.CameraViewerPlugin.deviceInfo;

public class LoginActivity extends Activity {

    ProgressBar progress;

    static final int HANDLE_MSG_CODE_LOGIN_RESULT = 0x10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(LocalDefines.notificationID);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.login);
        progress = findViewById(R.id.progressbar);
        login();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    // new SDK
    private void login() {
        LoginParam loginParam = new LoginParam(deviceInfo, Defines.LOGIN_FOR_PLAY);

        int loginResult = LoginHelper.loginDevice(this,loginParam, new ILoginDeviceCallback() {
            @Override
            public void onLogin(LoginHandle loginHandle) {
                if (loginHandle != null && loginHandle.getnResult() == ResultCode.RESULT_CODE_SUCCESS) {
                    // 登录成功
                    Message msg = handler.obtainMessage();
                    msg.arg1 = HANDLE_MSG_CODE_LOGIN_RESULT;
                    msg.arg2 = ResultCode.RESULT_CODE_SUCCESS;
                    Bundle data = new Bundle();
                    data.putParcelable("device_param", loginHandle);
                    msg.setData(data);
                    handler.sendMessage(msg);
                } else if (loginHandle != null) {
                    // 登录失败，可查看具体错误码
                    Message msg = handler.obtainMessage();
                    msg.arg1 = HANDLE_MSG_CODE_LOGIN_RESULT;
                    msg.arg2 = loginHandle.getnResult();
                    handler.sendMessage(msg);
                } else {
                    // 登录失败
                    Message msg = handler.obtainMessage();
                    msg.arg1 = HANDLE_MSG_CODE_LOGIN_RESULT;
                    msg.arg2 = ResultCode.RESULT_CODE_FAIL_SERVER_CONNECT_FAIL;
                    handler.sendMessage(msg);
                }
            }
        });

        if (loginResult != 0) {
            // 登录失败，参数错误等
            Message msg = handler.obtainMessage();
            msg.arg1 = HANDLE_MSG_CODE_LOGIN_RESULT;
            msg.arg2 = ResultCode.RESULT_CODE_FAIL_SERVER_CONNECT_FAIL;
            handler.sendMessage(msg);
        }
    }

    private final Handler handler = new Handler() {
        // @SuppressLint("HandlerLeak")
        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg) {

            if (msg.arg1 == HANDLE_MSG_CODE_LOGIN_RESULT) {
                progress.setVisibility(View.GONE);
                switch (msg.arg2) {
                    case ResultCode.RESULT_CODE_SUCCESS: {

                        Bundle bundle = msg.getData();

                        LoginHandle loginHandle = bundle.getParcelable("device_param");
                        LocalDefines.Device_LoginHandle = loginHandle;
                        int camType = loginHandle.getCamType();

                        if (camType == LocalDefines.CAMTYPE_WALL
                                || camType == LocalDefines.CAMTYPE_CEIL) {
                            Intent intent = new Intent(LoginActivity.this, NVPlayerPlayFishEyeActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            //MainActivity.this.finish();
                        } else {

                            Intent intent = new Intent(LoginActivity.this, NVPlayerPlayActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            //MainActivity.this.finish();
                        }
                        break;
                    }
                    case ResultCode.RESULT_CODE_FAIL_SERVER_CONNECT_FAIL: {
                        ShowAlert(
                                getString(R.string.alert_title_login_failed)
                                        + "  ("
                                        + getString(R.string.notice_Result_BadResult)
                                        + ")",
                                getString(R.string.alert_connect_tips));
                    }
                    break;
                    case ResultCode.RESULT_CODE_FAIL_VERIFY_FAILED: {
                        ShowAlert(getString(R.string.alert_title_login_failed),
                                getString(R.string.notice_Result_VerifyFailed));
                    }
                    break;
                    case ResultCode.RESULT_CODE_FAIL_USER_NOEXIST: {
                        progress.setVisibility(View.GONE);
                        ShowAlert(getString(R.string.alert_title_login_failed),
                                getString(R.string.notice_Result_UserNoExist));
                    }
                    break;
                    case ResultCode.RESULT_CODE_FAIL_PWD_ERROR: {
                        ShowAlert(getString(R.string.alert_title_login_failed),
                                getString(R.string.notice_Result_PWDError));
                    }
                    break;
                    case ResultCode.RESULT_CODE_FAIL_OLD_VERSON: {
                        ShowAlert(getString(R.string.alert_title_login_failed),
                                getString(R.string.notice_Result_Old_Version));
                    }
                    break;
                    default:
                        ShowAlert(
                                getString(R.string.alert_title_login_failed)
                                        + "  ("
                                        + getString(R.string.notice_Result_ConnectServerFailed)
                                        + ")", "");
                        break;

                }
            }
        }
    };


    private void ShowAlert(String title, String msg) {
        try {
            new AlertDialog.Builder(LoginActivity.this).setTitle(title)
                    .setMessage(msg)
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
}
