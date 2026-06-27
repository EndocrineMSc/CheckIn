package com.endocrine.checkin.presentation.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.endocrine.checkin.R
import com.endocrine.checkin.notification.BatteryOptimization
import com.endocrine.checkin.presentation.components.TimePickerDialog
import com.endocrine.checkin.presentation.theme.CheckInTheme
import com.endocrine.checkin.presentation.util.ObserveAsEvents
import com.endocrine.checkin.presentation.util.OnResume
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Root: owns the [SettingsViewModel], wires the Android-only interactions (notification-permission
 * request, battery-exemption dialog, SAF export picker) via Activity Result launchers, and
 * re-reads live permission status whenever the screen resumes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoot(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val exportUnavailable = stringResource(R.string.settings_export_unavailable)

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.onAction(SettingsAction.RefreshPermissions) }

    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { viewModel.onAction(SettingsAction.RefreshPermissions) }

    // Step 9 owns CSV writing; for now the picker is launched so the flow is wired end-to-end.
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { scope.launch { snackbarHostState.showSnackbar(exportUnavailable) } }

    OnResume { viewModel.onAction(SettingsAction.RefreshPermissions) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SettingsEvent.RequestNotificationPermission -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // < 33: no runtime permission. Send the user to system settings to re-enable.
                    batteryLauncher.launch(BatteryOptimization.settingsIntent())
                }
            }
            SettingsEvent.LaunchBatteryExemption -> batteryLauncher.launch(BatteryOptimization.requestIntent(context))
            SettingsEvent.LaunchExport -> exportLauncher.launch(defaultExportFileName())
        }
    }

    SettingsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onAction: (SettingsAction) -> Unit,
) {
    // Local UI state for the add/edit time picker (Compose-owned dialog state, not app state).
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_settings_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // ── Reminders ──────────────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_reminders))
            Card(modifier = Modifier.fillMaxWidth()) {
                if (state.reminders.isEmpty()) {
                    Text(
                        text = stringResource(R.string.settings_reminder_off),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                state.reminders.forEachIndexed { index, reminder ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ReminderRow(
                        reminder = reminder,
                        onEditTime = { pickerTarget = PickerTarget.Edit(reminder.id, reminder.hour, reminder.minute) },
                        onToggle = { onAction(SettingsAction.ToggleReminder(reminder.id, it)) },
                        onDelete = { onAction(SettingsAction.DeleteReminder(reminder.id)) },
                    )
                }
            }
            FilledTonalButton(
                onClick = { pickerTarget = PickerTarget.Add },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.settings_add_time))
            }

            // ── Data ───────────────────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_data))
            Card(modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    icon = Icons.Default.Download,
                    title = stringResource(R.string.settings_export_title),
                    subtitle = stringResource(R.string.settings_export_subtitle),
                    onClick = { onAction(SettingsAction.ExportData) },
                )
            }

            // ── Permissions ────────────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_permissions))
            Card(modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.settings_perm_notifications),
                    subtitle = null,
                    trailing = { StatusBadge(ok = state.notificationsEnabled) },
                    onClick = if (state.notificationsEnabled) null else {
                        { onAction(SettingsAction.RequestNotifications) }
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(
                    icon = Icons.Default.BatteryFull,
                    title = stringResource(R.string.settings_perm_battery),
                    subtitle = stringResource(
                        if (state.batteryExempt) R.string.settings_perm_battery_ok
                        else R.string.settings_perm_battery_action,
                    ),
                    trailing = { StatusBadge(ok = state.batteryExempt) },
                    onClick = if (state.batteryExempt) null else {
                        { onAction(SettingsAction.RequestBatteryExemption) }
                    },
                )
            }

            Spacer(Modifier.size(24.dp))
        }
    }

    pickerTarget?.let { target ->
        TimePickerDialog(
            initialHour = target.hour,
            initialMinute = target.minute,
            onConfirm = { hour, minute ->
                when (target) {
                    is PickerTarget.Add -> onAction(SettingsAction.AddReminder(hour, minute))
                    is PickerTarget.Edit -> onAction(SettingsAction.EditReminderTime(target.id, hour, minute))
                }
                pickerTarget = null
            },
            onDismiss = { pickerTarget = null },
        )
    }
}

/** What the time picker is currently editing — a brand-new reminder, or an existing one. */
private sealed interface PickerTarget {
    val hour: Int
    val minute: Int

    data object Add : PickerTarget {
        override val hour = 9
        override val minute = 0
    }

    data class Edit(val id: String, override val hour: Int, override val minute: Int) : PickerTarget
}

@Composable
private fun ReminderRow(
    reminder: ReminderUi,
    onEditTime: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditTime)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = reminder.time, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(
                    if (reminder.enabled) R.string.settings_reminder_daily else R.string.settings_reminder_off,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(R.string.cd_delete_reminder),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = reminder.enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun StatusBadge(ok: Boolean) {
    val bg = if (ok) Color(0xFFD7F0DC) else MaterialTheme.colorScheme.errorContainer
    val fg = if (ok) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onErrorContainer
    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (ok) Icons.Default.Check else Icons.Default.Warning,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(if (ok) R.string.settings_badge_ok else R.string.settings_badge_action),
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp, start = 4.dp),
    )
}

/** Suggested filename `checkins_YYYY-MM.csv` (year-month resolved at export time). */
private fun defaultExportFileName(): String {
    val cal = java.util.Calendar.getInstance()
    val year = cal.get(java.util.Calendar.YEAR)
    val month = cal.get(java.util.Calendar.MONTH) + 1
    return String.format(java.util.Locale.US, "checkins_%04d-%02d.csv", year, month)
}

private val previewState = SettingsState(
    reminders = listOf(
        ReminderUi("1", 9, 0, "09:00", true),
        ReminderUi("2", 14, 0, "14:00", true),
        ReminderUi("3", 20, 30, "20:30", false),
    ),
    notificationsEnabled = true,
    batteryExempt = false,
)

@Preview
@Composable
private fun SettingsPreview() {
    CheckInTheme {
        SettingsScreen(
            state = previewState,
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onAction = {},
        )
    }
}
