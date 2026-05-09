import 'dart:async';
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  runApp(const VinCalculatorApp());
}

class VinCalculatorApp extends StatelessWidget {
  const VinCalculatorApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '工程密码计算器',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.dark,
        useMaterial3: true,
        scaffoldBackgroundColor: const Color(0xFF0A0E27),
        colorScheme: ColorScheme.dark(
          primary: const Color(0xFF00D4FF),
          secondary: const Color(0xFF7B61FF),
          surface: const Color(0xFF1A1F3A),
          tertiary: const Color(0xFF00FFA3),
        ),
      ),
      home: const VinCalculatorScreen(),
    );
  }
}

class VinCalculatorScreen extends StatefulWidget {
  const VinCalculatorScreen({super.key});

  @override
  State<VinCalculatorScreen> createState() => _VinCalculatorScreenState();
}

class _VinCalculatorScreenState extends State<VinCalculatorScreen> {
  final TextEditingController _vinController = TextEditingController();
  String _resultPassword = '';
  String _currentDate = getCurrentDate();
  List<String> _historyList = [];
  Timer? _dateTimer;

  @override
  void initState() {
    super.initState();
    _loadHistory();
    _startDateTimer();
  }

  @override
  void dispose() {
    _vinController.dispose();
    _dateTimer?.cancel();
    super.dispose();
  }

  void _startDateTimer() {
    _dateTimer = Timer.periodic(const Duration(minutes: 1), (_) {
      final newDate = getCurrentDate();
      if (newDate != _currentDate) {
        setState(() {
          _currentDate = newDate;
        });
      }
    });
  }

  Future<void> _loadHistory() async {
    final prefs = await SharedPreferences.getInstance();
    final history = prefs.getStringList('vin_history') ?? [];
    setState(() {
      _historyList = history;
    });
  }

  Future<void> _saveHistory(String vinLastSix) async {
    final prefs = await SharedPreferences.getInstance();
    final history = prefs.getStringList('vin_history') ?? [];

    history.remove(vinLastSix);
    history.insert(0, vinLastSix);

    if (history.length > 20) {
      history.removeLast();
    }

    await prefs.setStringList('vin_history', history);
    _loadHistory();
  }

  Future<void> _deleteHistoryItem(String vinLastSix) async {
    final prefs = await SharedPreferences.getInstance();
    final history = prefs.getStringList('vin_history') ?? [];
    history.remove(vinLastSix);
    await prefs.setStringList('vin_history', history);
    _loadHistory();
  }

  void _calculatePassword(String vin) {
    if (vin.length < 6) return;

    final password = calculatePassword(vin, _currentDate);
    setState(() {
      _resultPassword = password;
    });

    final lastSix = vin.substring(vin.length - 6);
    _saveHistory(lastSix);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              Color(0xFF0A0E27),
              Color(0xFF1A1F3A),
              Color(0xFF0F1629),
            ],
          ),
        ),
        child: SafeArea(
          child: Column(
            children: [
              // Header
              _buildHeader(),
              Expanded(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      const SizedBox(height: 8),
                      _buildDateCard(),
                      const SizedBox(height: 20),
                      _buildInputCard(),
                      const SizedBox(height: 20),
                      _buildCalculateButton(),
                      if (_resultPassword.isNotEmpty) ...[
                        const SizedBox(height: 24),
                        _buildResultCard(),
                        const SizedBox(height: 24),
                        _buildDivider(),
                        const SizedBox(height: 20),
                        _buildHistorySection(),
                      ],
                      const SizedBox(height: 20),
                      _buildFormulaCard(),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      child: const Row(
        children: [
          Icon(Icons.calculate_outlined, color: Color(0xFF00D4FF), size: 28),
          SizedBox(width: 12),
          Text(
            '工程密码计算器',
            style: TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.bold,
              color: Colors.white,
              letterSpacing: 1,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDateCard() {
    return _GlassMorphismCard(
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: const Color(0xFF00D4FF).withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(Icons.calendar_today, color: Color(0xFF00D4FF), size: 18),
              ),
              const SizedBox(width: 10),
              const Text(
                '当前日期',
                style: TextStyle(fontSize: 14, color: Colors.white70),
              ),
            ],
          ),
          Text(
            _currentDate,
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Color(0xFF00D4FF),
              letterSpacing: 3),
          ),
        ],
      ),
    );
  }

  Widget _buildInputCard() {
    return _GlassMorphismCard(
      child: TextField(
        controller: _vinController,
        textCapitalization: TextCapitalization.characters,
        style: const TextStyle(
          fontSize: 18,
          color: Colors.white,
          letterSpacing: 2,
        ),
        decoration: InputDecoration(
          labelText: '请输入车架号',
          labelStyle: const TextStyle(color: Color(0xFF00D4FF)),
          hintText: '输入完整车架号...',
          hintStyle: TextStyle(color: Colors.white.withValues(alpha: 0.3)),
          prefixIcon: const Icon(Icons.directions_car, color: Color(0xFF00D4FF)),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: BorderSide(color: const Color(0xFF00D4FF).withValues(alpha: 0.3)),
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: BorderSide(color: const Color(0xFF00D4FF).withValues(alpha: 0.3)),
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: const BorderSide(color: Color(0xFF00D4FF), width: 2),
          ),
          filled: true,
          fillColor: Colors.white.withValues(alpha: 0.05),
        ),
        onChanged: (value) {
          setState(() {
            _resultPassword = '';
          });
        },
      ),
    );
  }

  Widget _buildCalculateButton() {
    final isEnabled = _vinController.text.length >= 6;
    return Container(
      height: 60,
      decoration: BoxDecoration(
        gradient: isEnabled
            ? const LinearGradient(
                colors: [Color(0xFF00D4FF), Color(0xFF7B61FF)],
              )
            : null,
        color: isEnabled ? null : Colors.grey.withValues(alpha: 0.3),
        borderRadius: BorderRadius.circular(16),
        boxShadow: isEnabled
            ? [
                BoxShadow(
                  color: const Color(0xFF00D4FF).withValues(alpha: 0.4),
                  blurRadius: 20,
                  spreadRadius: 0,
                ),
              ]
            : null,
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: isEnabled ? () => _calculatePassword(_vinController.text) : null,
          borderRadius: BorderRadius.circular(16),
          child: const Center(
            child: Text(
              '计算密码',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
                color: Colors.white,
                letterSpacing: 2,
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildResultCard() {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: const Color(0xFF1A1F3A),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFF00D4FF).withValues(alpha: 0.5), width: 2),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF00D4FF).withValues(alpha: 0.2),
            blurRadius: 20,
            spreadRadius: 0,
          ),
        ],
      ),
      child: Column(
        children: [
          const Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.lock_outline, color: Colors.white70, size: 20),
              SizedBox(width: 8),
              Text(
                '工程密码',
                style: TextStyle(fontSize: 14, color: Colors.white70),
              ),
            ],
          ),
          const SizedBox(height: 16),
          ShaderMask(
            shaderCallback: (bounds) => const LinearGradient(
              colors: [Colors.white, Color(0xFF00D4FF)],
            ).createShader(bounds),
            child: Text(
              _resultPassword,
              style: const TextStyle(
                fontSize: 48,
                fontWeight: FontWeight.bold,
                color: Colors.white,
                letterSpacing: 8,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDivider() {
    return Container(
      height: 1,
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            Colors.transparent,
            const Color(0xFF00D4FF).withValues(alpha: 0.5),
            Colors.transparent,
          ],
        ),
      ),
    );
  }

  Widget _buildHistorySection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Row(
              children: [
                Icon(Icons.history, color: Color(0xFF00D4FF), size: 20),
                SizedBox(width: 8),
                Text(
                  '历史记录',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600, color: Colors.white),
                ),
              ],
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
              decoration: BoxDecoration(
                color: const Color(0xFF00D4FF).withValues(alpha: 0.2),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                '${_historyList.length} 条',
                style: const TextStyle(fontSize: 12, color: Color(0xFF00D4FF)),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        ..._historyList.map((vinLastSix) => Padding(
              padding: const EdgeInsets.only(bottom: 10),
              child: HistoryItem(
                vinLastSix: vinLastSix,
                onTap: () {
                  _vinController.text = vinLastSix;
                  _calculatePassword(vinLastSix);
                },
                onDelete: () => _deleteHistoryItem(vinLastSix),
              ),
            )),
      ],
    );
  }

  Widget _buildFormulaCard() {
    return _GlassMorphismCard(
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.functions, color: Color(0xFF7B61FF), size: 18),
              SizedBox(width: 8),
              Text(
                '计算公式',
                style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: Colors.white),
              ),
            ],
          ),
          SizedBox(height: 12),
          Text(
            '密码 = (车架号后6位 + 123456) × 当天日期\n结果取后6位\n字母用0替换\n\n适用于奇瑞各种车型，OTA密码与工程相同',
            style: TextStyle(fontSize: 13, color: Colors.white60, height: 1.6),
          ),
        ],
      ),
    );
  }
}

class _GlassMorphismCard extends StatelessWidget {
  final Widget child;

  const _GlassMorphismCard({required this.child});

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(16),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 10, sigmaY: 10),
        child: Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            color: Colors.white.withValues(alpha: 0.08),
            borderRadius: BorderRadius.circular(16),
            border: Border.all(
              color: Colors.white.withValues(alpha: 0.15),
              width: 1,
            ),
          ),
          child: child,
        ),
      ),
    );
  }
}

class HistoryItem extends StatelessWidget {
  final String vinLastSix;
  final VoidCallback onTap;
  final VoidCallback onDelete;

  const HistoryItem({
    super.key,
    required this.vinLastSix,
    required this.onTap,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(12),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 8, sigmaY: 8),
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(12),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.06),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(
                color: const Color(0xFF00D4FF).withValues(alpha: 0.2),
                width: 1,
              ),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: const Color(0xFF00D4FF).withValues(alpha: 0.15),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Icon(Icons.directions_car, color: Color(0xFF00D4FF), size: 18),
                    ),
                    const SizedBox(width: 12),
                    Text(
                      vinLastSix,
                      style: const TextStyle(
                        fontWeight: FontWeight.w600,
                        fontSize: 16,
                        color: Colors.white,
                        letterSpacing: 2,
                      ),
                    ),
                  ],
                ),
                Row(
                  children: [
                    const Text(
                      '计算',
                      style: TextStyle(fontSize: 12, color: Color(0xFF00D4FF)),
                    ),
                    const SizedBox(width: 8),
                    IconButton(
                      icon: const Icon(Icons.delete_outline, color: Colors.red, size: 20),
                      onPressed: onDelete,
                      tooltip: '删除',
                      padding: EdgeInsets.zero,
                      constraints: const BoxConstraints(),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

String calculatePassword(String vin, String dateStr) {
  String lastSix = vin.length >= 6 ? vin.substring(vin.length - 6) : vin.padLeft(6, '0');
  String numericPart = lastSix.replaceAll(RegExp(r'[A-Za-z]'), '0');
  int vinNumber = int.tryParse(numericPart) ?? 0;
  int dateNumber = int.tryParse(dateStr) ?? 0;

  int result = (vinNumber + 123456) * dateNumber;

  String resultStr = result.toString();
  String password = resultStr.length >= 6
      ? resultStr.substring(resultStr.length - 6)
      : resultStr.padLeft(6, '0');

  return password;
}

String getCurrentDate() {
  final now = DateTime.now();
  return '${now.year}${now.month.toString().padLeft(2, '0')}${now.day.toString().padLeft(2, '0')}';
}
