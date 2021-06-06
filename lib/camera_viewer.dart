import 'dart:async';

import 'package:flutter/services.dart';

class CameraViewer {
  static const MethodChannel _channel = const MethodChannel('camera_viewer');

  ///打开wifi配置页面
  static Future<String> startWifiConfig() async {
    final String version = await _channel.invokeMethod('startWifiConfig');
    return version;
  }

  ///打开实时预览页面
  static Future<String> cameraPreview(
      String deviceId, String deviceUser, String devicePwd) async {
    final String version = await _channel.invokeMethod('cameraPreview', {
      'deviceId': deviceId,
      'deviceUser': deviceUser,
      'devicePwd': devicePwd,
    });
    return version;
  }

  ///打开录像回放页面
  static Future<String> videoPlayback(
      String deviceId, String deviceUser, String devicePwd) async {
    final String version = await _channel.invokeMethod('videoPlayback', {
      'deviceId': deviceId,
      'deviceUser': deviceUser,
      'devicePwd': devicePwd,
    });
    return version;
  }
}
