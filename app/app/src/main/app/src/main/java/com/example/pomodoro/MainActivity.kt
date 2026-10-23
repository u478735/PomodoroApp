package com.example.pomodoro

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 讓計時期間螢幕保持常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        val webView = WebView(this)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false // 確保音效可以自動播放
        }
        webView.webChromeClient = WebChromeClient()
        
        // 載入輕量且絕對穩定的介面
        val htmlCode = """
            <!DOCTYPE html>
            <html lang="zh-TW">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    :root { --bg: #121212; --surface: #1E1E1E; --primary: #FF5252; --text: #FFFFFF; --text-sub: #AAAAAA; }
                    * { box-sizing: border-box; margin: 0; padding: 0; user-select: none; -webkit-tap-highlight-color: transparent; }
                    body { background-color: var(--bg); color: var(--text); font-family: sans-serif; display: flex; flex-direction: column; align-items: center; justify-content: space-between; height: 100vh; padding: 32px 20px; }
                    .mode-tabs { display: flex; background: var(--surface); border-radius: 30px; padding: 6px; gap: 6px; }
                    .tab { padding: 10px 18px; border-radius: 24px; border: none; background: transparent; color: var(--text-sub); font-size: 14px; font-weight: 600; cursor: pointer; transition: 0.25s; }
                    .tab.active { background: var(--primary); color: #FFF; }
                    .timer-container { position: relative; width: 280px; height: 280px; display: flex; align-items: center; justify-content: center; }
                    svg { position: absolute; top: 0; left: 0; width: 100%; height: 100%; transform: rotate(-90deg); }
                    circle { fill: none; stroke-width: 8; stroke-linecap: round; }
                    .bg-ring { stroke: var(--surface); }
                    .progress-ring { stroke: var(--primary); stroke-dasharray: 816.8; stroke-dashoffset: 0; transition: stroke-dashoffset 0.5s linear, stroke 0.3s ease; }
                    .time-display { font-size: 64px; font-weight: 700; letter-spacing: 2px; }
                    .status-text { font-size: 16px; color: var(--text-sub); margin-top: -20px; }
                    .controls { display: flex; gap: 20px; width: 100%; max-width: 320px; justify-content: center; }
                    .btn { flex: 1; padding: 16px 0; border-radius: 18px; border: none; font-size: 18px; font-weight: 600; cursor: pointer; }
                    .btn-main { background: var(--primary); color: #FFF; }
                    .btn-reset { background: var(--surface); color: var(--text); flex: 0.5; }
                </style>
            </head>
            <body>
                <div class="mode-tabs">
                    <button class="tab active" onclick="switchMode(25, '#FF5252', '專注工作', this)">專注 25m</button>
                    <button class="tab" onclick="switchMode(5, '#4CAF50', '短暫休息', this)">短休 5m</button>
                    <button class="tab" onclick="switchMode(15, '#2196F3', '長休充電', this)">長休 15m</button>
                </div>
                <div class="timer-container">
                    <svg><circle class="bg-ring" cx="140" cy="140" r="130"></circle><circle class="progress-ring" id="ring" cx="140" cy="140" r="130"></circle></svg>
                    <div class="time-display" id="time">25:00</div>
                </div>
                <div class="status-text" id="status">準備開始專注</div>
                <div class="controls">
                    <button class="btn btn-main" id="startBtn" onclick="toggleTimer()">開始</button>
                    <button class="btn btn-reset" onclick="resetTimer()">重置</button>
                </div>
                <script>
                    let totalSeconds = 25 * 60; let remainingSeconds = totalSeconds; let timerInterval = null; let isRunning = false; let currentLabel = '專注工作'; const circumference = 2 * Math.PI * 130;
                    const timeEl = document.getElementById('time'); const ringEl = document.getElementById('ring'); const startBtn = document.getElementById('startBtn'); const statusEl = document.getElementById('status');
                    
                    function updateUI() { 
                        const mins = String(Math.floor(remainingSeconds / 60)).padStart(2, '0'); 
                        const secs = String(remainingSeconds % 60).padStart(2, '0'); 
                        timeEl.textContent = mins + ':' + secs; 
                        ringEl.style.strokeDashoffset = circumference - (remainingSeconds / totalSeconds) * circumference; 
                    }
                    function switchMode(minutes, color, label, btnEl) { 
                        pauseTimer(); totalSeconds = minutes * 60; remainingSeconds = totalSeconds; currentLabel = label; 
                        document.documentElement.style.setProperty('--primary', color); 
                        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active')); btnEl.classList.add('active'); 
                        statusEl.textContent = '準備開始：' + label; updateUI(); 
                    }
                    function toggleTimer() { if (isRunning) pauseTimer(); else startTimer(); }
                    function startTimer() { 
                        isRunning = true; startBtn.textContent = '暫停'; statusEl.textContent = currentLabel + '進行中...'; 
                        const targetTime = Date.now() + remainingSeconds * 1000; 
                        timerInterval = setInterval(() => { 
                            const diff = Math.round((targetTime - Date.now()) / 1000); 
                            if (diff <= 0) { remainingSeconds = 0; updateUI(); completeTimer(); } 
                            else { remainingSeconds = diff; updateUI(); } 
                        }, 250); 
                    }
                    function pauseTimer() { isRunning = false; clearInterval(timerInterval); startBtn.textContent = '開始'; statusEl.textContent = '已暫停'; }
                    function resetTimer() { pauseTimer(); remainingSeconds = totalSeconds; statusEl.textContent = '準備開始：' + currentLabel; updateUI(); }
                    function completeTimer() { 
                        pauseTimer(); statusEl.textContent = currentLabel + '完成！'; 
                        if (navigator.vibrate) navigator.vibrate([300, 150, 300, 150, 500]); 
                        playBeep(); alert(currentLabel + '時間到！'); resetTimer(); 
                    }
                    function playBeep() { 
                        try { 
                            const ctx = new (window.AudioContext || window.webkitAudioContext)(); 
                            const osc = ctx.createOscillator(); const gain = ctx.createGain(); 
                            osc.connect(gain); gain.connect(ctx.destination); osc.frequency.value = 880; 
                            gain.gain.setValueAtTime(0.3, ctx.currentTime); gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.8); 
                            osc.start(); osc.stop(ctx.currentTime + 0.8); 
                        } catch (e) {} 
                    }
                    updateUI();
                </script>
            </body>
            </html>
        """.trimIndent()
        
        webView.loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
        setContentView(webView)
    }
}
