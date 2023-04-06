import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.io.path.createTempFile
import kotlin.io.path.readText
import kotlin.io.path.writeText

fun FtpSession.walk(dir: String): List<String> = buildList {
    val dirs = lsDirs(dir)
    val files = lsFiles(dir)

    addAll(files.map { "F: $dir/$it" })
    addAll(dirs.flatMap { listOf("D: $dir/$it") + walk("$dir/$it") })
}

fun FtpSession.walk(): String = walk(".").joinToString("\n")

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun App() {
    val notConnected = "not connected"
    var host by remember { mutableStateOf("127.0.0.1") }
    var globText by remember { mutableStateOf("not logged in") }
    var port by remember { mutableStateOf(21) }
    var username by remember { mutableStateOf("TestUser") }
    var password by remember { mutableStateOf<String?>(null) }
    var session by remember { mutableStateOf<FtpSession?>(null) }
    var files by remember { mutableStateOf(notConnected) }
    var path by remember { mutableStateOf("") }
    var editor by remember { mutableStateOf("Your file will be here") }

    MaterialTheme {
        Column(Modifier.fillMaxHeight(), Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(15.dp)) {
                Column(Modifier.align(Alignment.Top), Arrangement.spacedBy(5.dp)) {
                    Text("Server address")
                    TextField(
                        modifier = Modifier.width(150.dp),
                        value = host,
                        onValueChange = { host = it },
                        singleLine = true
                    )
                }

                Column(Modifier.align(Alignment.Top), Arrangement.spacedBy(5.dp)) {
                    Text("Port")
                    TextField(
                        modifier = Modifier.width(80.dp),
                        value = port.toString(),
                        onValueChange = { port = it.toIntOrNull() ?: 21 },
                        singleLine = true
                    )
                }

                Column(Modifier.align(Alignment.Top), Arrangement.spacedBy(5.dp)) {
                    Text("Username")
                    TextField(
                        modifier = Modifier.width(150.dp),
                        value = username,
                        onValueChange = { username = it },
                        singleLine = true
                    )
                }

                Column(Modifier.align(Alignment.Top), Arrangement.spacedBy(5.dp)) {
                    Text("Password")
                    TextField(
                        modifier = Modifier.width(150.dp),
                        value = password ?: "",
                        onValueChange = { password = it.ifEmpty { null } },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                var failedLogin by remember { mutableStateOf<String?>(null) }

                Button(
                    modifier = Modifier.align(Alignment.Bottom),
                    onClick = {
                        session?.close()
                        try {
                            session = FtpSession(host, port, username, password)
                            globText = "logged in"
                        } catch (e: FTPConnectionException) {
                            failedLogin = e.message
                            session = null
                        }
                        files = session?.walk() ?: notConnected
                    }
                ) {
                    Text("Connect")
                }

                if (failedLogin != null) {
                    AlertDialog(
                        text = {
                            Text(failedLogin.toString())
                        },
                        onDismissRequest = {
                            // Dismiss the dialog when the user clicks outside the dialog or on the back
                            // button. If you want to disable that functionality, simply use an empty
                            // onCloseRequest.
                            failedLogin = null
                        },
                        confirmButton = {
                            Button(onClick = { failedLogin = null }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
            Row(Modifier.fillMaxWidth().fillMaxHeight(0.7f), Arrangement.spacedBy(15.dp)) {
                Column(Modifier.fillMaxWidth(0.5f), Arrangement.spacedBy(1.dp)) {
                    Text(files)
                }
                Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(1.dp)) {
                    TextField(
                        modifier = Modifier.fillMaxSize(),
                        value = editor,
                        onValueChange = { editor = it },
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(15.dp)) {
                Column(Modifier.align(Alignment.Top), Arrangement.spacedBy(5.dp)) {
                    Text("Enter path:")
                    TextField(
                        modifier = Modifier.width(150.dp),
                        value = path,
                        onValueChange = { path = it },
                        singleLine = true
                    )
                }
                Button(modifier = Modifier.align(Alignment.Bottom), onClick = {
                    session?.let {
                        val file = createTempFile()
                        file.writeText(editor)
                        it.upload(file.toFile(), path)
                        files = it.walk()
                    }
                }) {
                    Text("Create")
                }

                Button(modifier = Modifier.align(Alignment.Bottom), onClick = {
                    session?.let {
                        val file = createTempFile()
                        it.download(path, file.toFile())
                        editor = file.readText()
                        files = it.walk()
                    }
                }) {
                    Text("Retrieve")
                }

                Button(modifier = Modifier.align(Alignment.Bottom), onClick = {
                    session?.let {
                        val file = createTempFile()
                        if (it.download(path, file.toFile())) {
                            file.writeText(editor)
                            it.upload(file.toFile(), path)
                            files = it.walk()
                        }
                    }
                }) {
                    Text("Update")
                }

                Button(modifier = Modifier.align(Alignment.Bottom), onClick = {
                    session?.let {
                        it.delete(path)
                        files = it.walk()
                        path = ""
                    }
                }) {
                    Text("Delete")
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
