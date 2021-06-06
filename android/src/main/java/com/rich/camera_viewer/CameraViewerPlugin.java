package com.rich.camera_viewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.macrovideo.sdk.defines.Defines;
import com.macrovideo.sdk.defines.ResultCode;
import com.macrovideo.sdk.media.ILoginDeviceCallback;
import com.macrovideo.sdk.media.LoginHandle;
import com.macrovideo.sdk.media.LoginHelper;
import com.macrovideo.sdk.objects.DeviceInfo;
import com.macrovideo.sdk.objects.LoginParam;
import com.rich.camera_viewer.demo.LocalDefines;
import com.rich.camera_viewer.demo.LoginActivity;
import com.rich.camera_viewer.demo.MainActivity;
import com.rich.camera_viewer.demo.NVPlayerPlayActivity;
import com.rich.camera_viewer.demo.NVPlayerPlayFishEyeActivity;
import com.rich.camera_viewer.demo.RecordActivity;
import com.rich.camera_viewer.demo.SmartLinkQuickWifiConfigActivity;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** CameraViewerPlugin */
public class CameraViewerPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Context context;
  String deviceId;
  String deviceUser;
  String devicePwd;
  public static DeviceInfo deviceInfo = null;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "camera_viewer");
    channel.setMethodCallHandler(this);
    context = flutterPluginBinding.getApplicationContext();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if(call.method.equals("startWifiConfig")){
//      Intent intent = new Intent(context, SmartLinkQuickWifiConfigActivity.class);
      Intent intent = new Intent(context, MainActivity.class);
      context.startActivity(intent);
    } else if(call.method.equals("cameraPreview")){
      getParams(call);
      checkParam();
      Intent intent = new Intent(context, LoginActivity.class);
      context.startActivity(intent);
    } else if(call.method.equals("videoPlayback")){
      getParams(call);
      checkParam();
      LocalDefines._severInfoListData.clear();
      LocalDefines._severInfoListData.add(deviceInfo);
      Intent intent = new Intent(context, RecordActivity.class);
      context.startActivity(intent);
    } else {
      result.notImplemented();
    }
  }

  private void getParams(@NonNull MethodCall call){
    deviceId = call.argument("deviceId");
    deviceUser = call.argument("deviceUser");
    devicePwd = call.argument("devicePwd");
  }

  private void checkParam(){
    if (TextUtils.isEmpty(deviceId)) {
      if (deviceInfo == null) {
        Toast.makeText(context, "请输入设备ID", Toast.LENGTH_SHORT).show();
        return;
      }
    }

    if (TextUtils.isEmpty(deviceUser)) {
      if (deviceInfo == null) {
        Toast.makeText(context, "请输入设备用户名", Toast.LENGTH_SHORT).show();
        return;
      }
    }

    if (!TextUtils.isEmpty(deviceId)) {
      deviceInfo = new DeviceInfo(-1, Integer.parseInt(deviceId), deviceId,
              "192.168.1.1", 8800, deviceUser, devicePwd, "ABC", deviceId + ".nvdvr.net",
              Defines.SERVER_SAVE_TYPE_ADD);
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
