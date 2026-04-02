package com.bloodpressure.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onExportData: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    @Suppress("UNUSED_VARIABLE")
    val context = LocalContext.current
    var appIdInput by remember { mutableStateOf("") }
    var appSecretInput by remember { mutableStateOf("") }
    var tableTokenInput by remember { mutableStateOf("") }

    if (uiState.showFeishuDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissFeishuDialog,
            title = { Text("配置飞书多维表格") },
            text = {
                Column {
                    Text(
                        text = "请输入飞书开放平台配置：",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (uiState.connectionStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.connectionStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.connectionStatus.contains("成功"))
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = appIdInput,
                        onValueChange = {
                            appIdInput = it
                            viewModel.updateFeishuAppId(it)
                        },
                        label = { Text("App ID") },
                        placeholder = { Text("cli_xxxxx") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = appSecretInput,
                        onValueChange = {
                            appSecretInput = it
                            viewModel.updateFeishuAppSecret(it)
                        },
                        label = { Text("App Secret") },
                        placeholder = { Text("xxxxxx") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tableTokenInput,
                        onValueChange = {
                            tableTokenInput = it
                            viewModel.updateFeishuTableToken(it)
                        },
                        label = { Text("表格Token") },
                        placeholder = { Text("xxxXXXX") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "如何获取？创建飞书应用 → 获取App ID/Secret → 创建多维表格 → 复制表格Token",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    TextButton(
                        onClick = {
                            viewModel.saveFeishuConfig()
                            appIdInput = ""
                            appSecretInput = ""
                            tableTokenInput = ""
                        },
                        enabled = appIdInput.isNotEmpty() && appSecretInput.isNotEmpty() && tableTokenInput.isNotEmpty()
                    ) {
                        Text("测试并保存")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissFeishuDialog) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "飞书同步") {
                SettingsItem(
                    title = "多维表格配置",
                    subtitle = if (uiState.isFeishuConnected) "已连接" else "点击配置",
                    onClick = {
                        viewModel.showFeishuDialog()
                    }
                )
                SettingsItem(
                    title = "实时同步",
                    subtitle = "记录后同步到飞书表格",
                    onClick = { if (uiState.isFeishuConnected) viewModel.toggleSync(!uiState.syncEnabled) },
                    trailing = {
                        Switch(
                            checked = uiState.syncEnabled,
                            onCheckedChange = if (uiState.isFeishuConnected) viewModel::toggleSync else null,
                            enabled = uiState.isFeishuConnected
                        )
                    }
                )
                if (uiState.isFeishuConnected) {
                    SettingsItem(
                        title = "断开连接",
                        subtitle = "清除飞书配置",
                        onClick = { viewModel.disconnectFeishu() },
                        isDestructive = true
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSection(title = "提醒") {
                SettingsItem(
                    title = "早上提醒",
                    subtitle = uiState.morningReminderTime,
                    onClick = { viewModel.showTimePicker("morning") }
                )
                SettingsItem(
                    title = "晚上提醒",
                    subtitle = uiState.eveningReminderTime,
                    onClick = { viewModel.showTimePicker("evening") }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSection(title = "显示") {
                SettingsItem(
                    title = "深色模式",
                    subtitle = uiState.darkMode,
                    onClick = { }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSection(title = "数据") {
                SettingsItem(
                    title = "导出数据",
                    subtitle = "导出为CSV文件",
                    onClick = onExportData
                )
                SettingsItem(
                    title = "清空数据",
                    subtitle = "删除所有记录",
                    onClick = { },
                    isDestructive = true
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSection(title = "关于") {
                SettingsItem(
                    title = "版本",
                    subtitle = "1.0.1",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (uiState.showTimePickerDialog) {
        val currentTime = when (uiState.editingReminderType) {
            "morning" -> uiState.morningReminderTime
            "evening" -> uiState.eveningReminderTime
            else -> "09:00"
        }
        val parts = currentTime.split(":")
        var selectedHour by remember { mutableStateOf(parts.getOrElse(0) { "09" }.toIntOrNull() ?: 9) }
        var selectedMinute by remember { mutableStateOf(parts.getOrElse(1) { "00" }.toIntOrNull() ?: 0) }

        AlertDialog(
            onDismissRequest = viewModel::dismissTimePicker,
            title = { Text(if (uiState.editingReminderType == "morning") "早上提醒时间" else "晚上提醒时间") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("小时", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        NumberPicker(
                            value = selectedHour,
                            range = 0..23,
                            onValueChange = { selectedHour = it }
                        )
                    }
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("分钟", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        NumberPicker(
                            value = selectedMinute,
                            range = 0..59,
                            onValueChange = { selectedMinute = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.updateReminderTime(selectedHour, selectedMinute) }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissTimePicker) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier
                .clickable { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = String.format("%02d", value),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            range.forEach { number ->
                DropdownMenuItem(
                    text = { Text(String.format("%02d", number)) },
                    onClick = {
                        onValueChange(number)
                        expanded = false
                    }
                )
            }
        }
    }
}