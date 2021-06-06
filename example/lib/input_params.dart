import 'package:flutter/material.dart';

List<InputParamsWidget> listParam;

///批量生成参数输入组件
List<InputParamsWidget> productParamList(Map<String, dynamic> paramMap) {
  List<InputParamsWidget> list = [];
  paramMap.forEach((key, value) {
    list.add(InputParamsWidget(
      hint: key,
      value: value.toString(),
    ));
  });
  listParam = list;
  return listParam;
}

class InputParamsWidget extends StatefulWidget {
  final String hint;
  String value;

  InputParamsWidget({
    this.hint,
    this.value,
  });

  @override
  _InputParamsState createState() => _InputParamsState();
}

class _InputParamsState extends State<InputParamsWidget> {
  TextEditingController controller = TextEditingController();

  @override
  void initState() {
    super.initState();
    String value;
    if (value != null) {
      controller.text = value;
      widget.value = value;
    } else {
      controller.text = widget.value;
    }
  }

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      children: <Widget>[
        Container(
          alignment: Alignment.center,
          width: 100,
          child: Text("${widget.hint}:"),
        ),
        SizedBox(width: 10),
        Expanded(
          child: Container(
            height: 50,
            child: TextField(
              controller: controller,
              maxLines: 1,
              decoration: InputDecoration(
                hintText: widget.hint,
              ),
              onChanged: (val) {
                widget.value = val;
              },
            ),
          ),
        ),
        SizedBox(width: 30),
      ],
    );
  }
}
