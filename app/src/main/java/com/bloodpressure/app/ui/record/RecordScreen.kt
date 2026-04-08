package com.bloodpressure.app.ui.record

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.domain.model.classifyBloodPressure
import com.bloodpressure.app.domain.model.toComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    date: String,
    period: String,
    viewModel: RecordViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(date, period) {
        viewModel.initialize(date, period)
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    val periodText = if (uiState.period.name == "MORNING") "早上" else "晚上"
    val periodIcon = if (uiState.period.name == "MORNING") "🌅" else "🌙"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$periodIcon $periodText 血压记录",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            BloodPressureDisplay(
                systolic = uiState.systolic,
                diastolic = uiState.diastolic,
                onSystolicChange = viewModel::updateSystolic,
                onDiastolicChange = viewModel::updateDiastolic
            )

            // Blood Pressure Level Display
            val systolicInt = uiState.systolic.toIntOrNull()
            val diastolicInt = uiState.diastolic.toIntOrNull()
            if (systolicInt != null && diastolicInt != null && systolicInt > 0 && diastolicInt > 0) {
                val level = classifyBloodPressure(systolicInt, diastolicInt)
                val levelColor = level.toComposeColor()
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = levelColor.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = levelColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = level.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = levelColor
                            )
                            Text(
                                text = level.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            NumberPad(
                onSystolicInput = viewModel::updateSystolic,
                onDiastolicInput = viewModel::updateDiastolic,
                systolic = uiState.systolic,
                diastolic = uiState.diastolic
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.heartRate,
                onValueChange = viewModel::updateHeartRate,
                label = { Text("心率 (可选)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                label = { Text("备注 (可选)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(Icons.Default.Notes, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            uiState.errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = viewModel::saveRecord,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.systolic.isNotEmpty() && uiState.diastolic.isNotEmpty() && !uiState.isSaving,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isEditing) "更新记录" else "保存记录",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun BloodPressureDisplay(
    systolic: String,
    diastolic: String,
    onSystolicChange: (String) -> Unit,
    onDiastolicChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = systolic.ifEmpty { "--" },
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "高压",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "收缩压",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Text(
            text = "/",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = diastolic.ifEmpty { "--" },
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = "低压",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "舒张压",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberPad(
    onSystolicInput: (String) -> Unit,
    onDiastolicInput: (String) -> Unit,
    systolic: String,
    diastolic: String
) {
    var activeField by remember { mutableStateOf("systolic") }

    LaunchedEffect(systolic) {
        if (activeField == "systolic" && systolic.length == 3) {
            activeField = "diastolic"
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = activeField == "systolic",
                onClick = { activeField = "systolic" },
                label = { Text("输入高压") },
                leadingIcon = if (activeField == "systolic") {
                    { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = activeField == "diastolic",
                onClick = { activeField = "diastolic" },
                label = { Text("输入低压") },
                leadingIcon = if (activeField == "diastolic") {
                    { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val currentValue = if (activeField == "systolic") systolic else diastolic
        val onInput = if (activeField == "systolic") onSystolicInput else onDiastolicInput

        val buttons = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "⌫")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { button ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when (button) {
                                    "C" -> MaterialTheme.colorScheme.errorContainer
                                    "⌫" -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .clickable {
                                when (button) {
                                    "C" -> onInput("")
                                    "⌫" -> if (currentValue.isNotEmpty()) {
                                        onInput(currentValue.dropLast(1))
                                    }
                                    else -> if (currentValue.length < 3) {
                                        onInput(currentValue + button)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = button,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Medium,
                            color = when (button) {
                                "C" -> MaterialTheme.colorScheme.error
                                "⌫" -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}
