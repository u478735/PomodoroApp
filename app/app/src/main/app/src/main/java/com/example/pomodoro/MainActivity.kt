package com.example.pomodoro

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                PomodoroScreen()
            }
        }
    }
}

enum class TimerMode(val title: String, val minutes: Int, val color: Color) {
    WORK("專注 25m", 25, Color(0xFFFF5252)),
    SHORT_BREAK("短休 5m", 5, Color(0xFF4CAF50)),
    LONG_BREAK("長休 15m", 15, Color(0xFF2196F3))
}

@Composable
fun PomodoroScreen() {
    val context = LocalContext.current
    var currentMode by remember { mutableStateOf(TimerMode.WORK) }
    var totalSeconds by remember { mutableIntStateOf(currentMode.minutes * 60) }
    var remainingSeconds by remember { mutableIntStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
            triggerVibration(context)
        }
    }

    val progress by animateFloatAsState(
        targetValue = remainingSeconds.toFloat() / totalSeconds.toFloat(),
        label = "Progress"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.background(Color(0xFF1E1E1E), RoundedCornerShape(30.dp)).padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimerMode.entries.forEach { mode ->
                val selected = mode == currentMode
                Button(
                    onClick = {
                        isRunning = false
                        currentMode = mode
                        totalSeconds = mode.minutes * 60
                        remainingSeconds = totalSeconds
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) mode.color else Color.Transparent,
                        contentColor = if (selected) Color.White else Color.Gray
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(mode.title, fontWeight = FontWeight.Bold)
                }
            }
        }

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color(0xFF2C2C2C), startAngle = 0f, sweepAngle = 360f,
                    useCenter = false, style = Stroke(width = 24f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = currentMode.color, startAngle = -90f, sweepAngle = 360f * progress,
                    useCenter = false, style = Stroke(width = 24f, cap = StrokeCap.Round)
                )
            }
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            Text(
                text = "%02d:%02d".format(mins, secs), fontSize = 64.sp,
                fontWeight = FontWeight.Bold, color = Color.White
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { isRunning = !isRunning },
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = currentMode.color),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (isRunning) "暫停" else "開始", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { isRunning = false; remainingSeconds = totalSeconds },
                modifier = Modifier.weight(0.5f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("重置", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

fun triggerVibration(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
}
