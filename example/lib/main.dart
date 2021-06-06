import 'package:camera_viewer/camera_viewer.dart';
import 'package:flutter/material.dart';

import 'input_params.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  ///输入控件配置参数
  Map<String, dynamic> inputParamMap = {
    "设备ID": "47448062",
    "设备用户名": "admin",
    "设备密码": "a123456",
  };

  @override
  Widget build(BuildContext context) {
    List<Widget> list = <Widget>[];
    list.addAll(productParamList(inputParamMap));

    list.addAll([
      TextButton(
        child: Text('WIFI配置'),
        onPressed: () async {
          CameraViewer.startWifiConfig();
        },
      ),
      TextButton(
        child: Text('实时预览'),
        onPressed: () async {
          CameraViewer.cameraPreview(
            listParam[0].value,
            listParam[1].value,
            listParam[2].value,
          );
        },
      ),
      TextButton(
        child: Text('录像回放'),
        onPressed: () async {
          CameraViewer.videoPlayback(
            listParam[0].value,
            listParam[1].value,
            listParam[2].value,
          );
        },
      )
    ]);
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: list,
        ),
      ),
    );
  }
}
