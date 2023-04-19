import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

const val uiUpdateTimeoutMillis = 20L

@Composable
fun route(monitor: NotificationsMonitor) {
    val coroutineScope = rememberCoroutineScope()
    var alive by remember { mutableStateOf(emptyList<String>()) }
    var timeoutMillis by remember { mutableStateOf(monitor.timeoutMillis) }
    var patience by remember { mutableStateOf(monitor.patience) }

    coroutineScope.launch { // Updating alive addresses in UI thread
        while (true) {
            delay(uiUpdateTimeoutMillis)
            alive = monitor.aliveAddresses
        }
    }

    MaterialTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row {
                Text("Timeout, ms:", modifier = Modifier.align(Alignment.CenterVertically))
                TextField(
                    modifier = Modifier.width(150.dp).align(Alignment.CenterVertically).scale(0.8f, 0.8f),
                    value = timeoutMillis.toString(),
                    onValueChange = { newValue ->
                        val intValue = newValue.toIntOrNull()
                        if (intValue != null && intValue > 0) {
                            timeoutMillis = intValue
                            monitor.timeoutMillis = intValue
                        }
                    },
                    textStyle = TextStyle(fontSize = 20.sp),
                    singleLine = true,
                )
                Text("Patience:", modifier = Modifier.align(Alignment.CenterVertically))
                TextField(
                    modifier = Modifier.width(150.dp).align(Alignment.CenterVertically).scale(0.8f, 0.8f),
                    value = patience.toString(),
                    onValueChange = { newValue ->
                        val intValue = newValue.toIntOrNull()
                        if (intValue != null && intValue > 0) {
                            patience = intValue
                            monitor.patience = intValue
                        }
                    },
                    textStyle = TextStyle(fontSize = 20.sp),
                    singleLine = true,
                )
            }
            Row {
                Text("Number of launched copies: ${alive.size}")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                Text("Addresses of launched copies:")
                Text(alive.joinToString("\n"))
            }
        }
    }
}

fun main() = application {
    val monitor = NotificationsMonitor()

    val monitorThread = thread {
        monitor.run()
    }

    Window(onCloseRequest = {
        monitor.isRunning = false
        monitorThread.join()
        exitApplication()
    }) {
        route(monitor)
    }
}