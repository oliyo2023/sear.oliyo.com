import 'package:flutter_test/flutter_test.dart';

import 'package:vin_calculator/main.dart';

void main() {
  testWidgets('App starts correctly', (WidgetTester tester) async {
    await tester.pumpWidget(const VinCalculatorApp());

    expect(find.text('工程密码计算器'), findsOneWidget);
    expect(find.text('车架号密码计算'), findsNothing);
  });

  testWidgets('Date card is displayed', (WidgetTester tester) async {
    await tester.pumpWidget(const VinCalculatorApp());

    expect(find.text('当前日期'), findsOneWidget);
  });
}
