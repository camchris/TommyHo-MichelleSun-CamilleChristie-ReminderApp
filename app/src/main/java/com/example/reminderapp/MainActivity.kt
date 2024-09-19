package com.example.reminderapp

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.tooling.preview.Preview
import com.example.reminderapp.ui.theme.ReminderAppTheme
import kotlinx.coroutines.launch
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.logging.SimpleFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReminderAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Reminder (
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Reminder(
    modifier: Modifier = Modifier
) {
    Column (
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var textBox by remember { mutableStateOf("") }
        TextField(
            value = textBox,
            onValueChange = {textBox = it},
            label = {Text("Enter a task")}
        )

        //var dates = remember { mutableStateListOf<Long>() }
        var date = remember { mutableStateOf<Long>(-1) }
        //var times = remember { mutableStateListOf<TimePickerState>() }
        var hour = remember { mutableStateOf<Int>(-1) }
        var minute = remember { mutableStateOf<Int>(-1) }
        var showModal = remember { mutableStateOf(false) }
        var showTime = remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val snackBarHostState = remember { SnackbarHostState() }
        var doSnack = remember { mutableStateOf(false) }
        var displayText = remember { mutableStateOf(false) }
        var text by remember { mutableStateOf("") }
        var dateText by remember { mutableStateOf<Long>(-1) }

        Button(
            onClick = {
                showModal.value = true
            }
        ) {
            Text("Create Reminder")
        }

        // date picker shows when button hit
        if (showModal.value) {
            DatePickerModalInput(
                onDateSelected = { selectedDate ->
                    // selecting date
                    if (selectedDate != null) {
                        date.value = selectedDate
                        showModal.value = false
                        showTime.value = true

                    }
                },
                onDismiss = {
                    // Cancel button hit
                    showModal.value = false
                }
            )
        }

        if (showTime.value) {
            DialWithDialog(
                onConfirm = { selectedTime ->
                    // selecting date
                    hour.value = selectedTime.hour
                    minute.value = selectedTime.minute
                    showTime.value = false
                    text = textBox
                    dateText = date.value
                    doSnack.value = true
                },
                onDismiss = {
                    // Cancel button hit
                    showTime.value = false
                }

            )
        }

        // snackbar
        if (doSnack.value) {
            scope.launch {
                val result = snackBarHostState
                    .showSnackbar(
                        message = "Reminder has been set.",
                        actionLabel = "Dismiss",
                        duration = SnackbarDuration.Indefinite
                    )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        // make MainActivity display reminder
                        displayText.value = true
                        doSnack.value = false

                    }
                    SnackbarResult.Dismissed -> {
                        // idk
                    }
                }
            }
        }

        Button(
            onClick = {
                displayText.value = false

                scope.launch {
                    val result = snackBarHostState
                        .showSnackbar(
                            message = "Reminder has been cleared.",
                            actionLabel = "Dismiss",
                            duration = SnackbarDuration.Indefinite
                        )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            // make MainActivity display reminder

                        }
                        SnackbarResult.Dismissed -> {
                            // idk
                        }
                    }
                }

            }
        ) {
            Text("Clear Reminder")
        }
        if (displayText.value) {
            Row() {
                Text(text= text + "   ")
                Text(text = hour.value.toString() + ":" + minute.value.toString() + "    ")
                val dateDate = Date(dateText)
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateStr = formatter.format(dateDate)
                Text(text = dateStr)
            }
        }
        SnackbarHost(hostState = snackBarHostState)

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModalInput(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialWithDialog(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    TimePickerDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm(timePickerState) }
    ) {
        TimePicker(
            state = timePickerState,
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("OK")
            }
        },
        text = { content() }
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ReminderAppTheme {
        Greeting("Android")
    }
}