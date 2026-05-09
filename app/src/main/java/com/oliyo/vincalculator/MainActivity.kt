package com.oliyo.vincalculator

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

const val PREFS_NAME = "vin_prefs"
const val KEY_HISTORY = "vin_history"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VinCalculatorScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VinCalculatorScreen() {
    var vinNumber by remember { mutableStateOf("") }
    var resultPassword by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf(getCurrentDate()) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Load history
    var historyList by remember {
        mutableStateOf(loadHistory(context))
    }

    // Refresh date when screen becomes visible
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000) // Check every minute
            currentDate = getCurrentDate()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "工程密码计算器",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showHistoryDialog = true }) {
                        Icon(Icons.Default.History, contentDescription = "历史记录")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                "车架号密码计算",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Current Date Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "当前日期",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        currentDate,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // VIN Input
            OutlinedTextField(
                value = vinNumber,
                onValueChange = { vinNumber = it.uppercase() },
                label = { Text("请输入车架号") },
                placeholder = { Text("输入完整车架号...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text
                ),
                leadingIcon = {
                    Text("🚗", fontSize = 24.sp)
                }
            )

            // Quick History Buttons (if any)
            if (historyList.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "快速选择",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    historyList.take(5).forEach { historyItem ->
                        HistoryQuickButton(
                            vinLastSix = historyItem,
                            onClick = {
                                vinNumber = historyItem
                                resultPassword = calculatePassword(historyItem, currentDate)
                            }
                        )
                    }
                }
            }

            // Calculate Button
            Button(
                onClick = {
                    resultPassword = calculatePassword(vinNumber, currentDate)
                    // Save to history
                    val lastSix = vinNumber.takeLast(6)
                    saveHistory(context, lastSix)
                    historyList = loadHistory(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = vinNumber.length >= 6
            ) {
                Text("计算密码", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Result Card
            if (resultPassword.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "工程密码",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            resultPassword,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                copyToClipboard(context, resultPassword)
                                scope.launch {
                                    Toast.makeText(context, "密码已复制", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "复制")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("复制密码")
                        }
                    }
                }
            }

            // Formula Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "计算公式",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "密码 = (车架号后6位 + 123456) × 当天日期\n结果取后6位",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }

    // History Dialog
    if (showHistoryDialog) {
        HistoryDialog(
            historyList = historyList,
            onSelect = { vinLastSix ->
                vinNumber = vinLastSix
                resultPassword = calculatePassword(vinLastSix, currentDate)
                showHistoryDialog = false
            },
            onDelete = { vinLastSix ->
                deleteHistoryItem(context, vinLastSix)
                historyList = loadHistory(context)
            },
            onDismiss = { showHistoryDialog = false }
        )
    }
}

@Composable
fun HistoryQuickButton(
    vinLastSix: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "🚗",
                    fontSize = 20.sp
                )
                Column {
                    Text(
                        vinLastSix,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        "点击快速计算",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Text(
                "→",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun HistoryDialog(
    historyList: List<String>,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "历史记录",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onDismiss) {
                        Text("关闭")
                    }
                }

                if (historyList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "📂",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "暂无历史记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historyList) { vinLastSix ->
                            HistoryItem(
                                vinLastSix = vinLastSix,
                                onSelect = { onSelect(vinLastSix) },
                                onDelete = { onDelete(vinLastSix) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    vinLastSix: String,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSelect)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🚗",
                        fontSize = 18.sp
                    )
                }
                Text(
                    vinLastSix,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

fun calculatePassword(vin: String, dateStr: String): String {
    // Get last 6 characters of VIN
    val lastSix = vin.takeLast(6)

    // Replace letters with 0
    val numericPart = lastSix.map { if (it.isDigit()) it else '0' }.joinToString()

    // Parse to integer
    val vinNumber = numericPart.toIntOrNull() ?: 0

    // Parse date
    val dateNumber = dateStr.toIntOrNull() ?: 0

    // Calculate: (vinNumber + 123456) * dateNumber
    val result = (vinNumber + 123456) * dateNumber

    // Get last 6 digits
    return result.toString().takeLast(6).padStart(6, '0')
}

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return sdf.format(System.currentTimeMillis())
}

suspend fun copyToClipboard(context: Context, text: String) {
    withContext(Dispatchers.Main) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("密码", text)
        clipboard.setPrimaryClip(clip)
    }
}

fun saveHistory(context: Context, vinLastSix: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val currentHistory = prefs.getStringSet(KEY_HISTORY, emptySet()) ?: emptySet()
    val newHistory = currentHistory.toMutableSet()

    // Remove if exists (to move to top) and add new entry
    newHistory.remove(vinLastSix)
    newHistory.add(vinLastSix)

    // Keep only last 20 entries
    if (newHistory.size > 20) {
        val toRemove = newHistory.first()
        newHistory.remove(toRemove)
    }

    prefs.edit().putStringSet(KEY_HISTORY, newHistory).apply()
}

fun loadHistory(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val historySet = prefs.getStringSet(KEY_HISTORY, emptySet()) ?: emptySet()
    // Return as list with newest first (reversed from set order)
    return historySet.toList().reversed()
}

fun deleteHistoryItem(context: Context, vinLastSix: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val currentHistory = prefs.getStringSet(KEY_HISTORY, emptySet()) ?: emptySet()
    val newHistory = currentHistory.toMutableSet()
    newHistory.remove(vinLastSix)
    prefs.edit().putStringSet(KEY_HISTORY, newHistory).apply()
}
