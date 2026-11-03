package com.example.service

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class SafetyToolkitManager(private val context: Context) {

  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  private val _isSirenActive = MutableStateFlow(false)
  val isSirenActive: StateFlow<Boolean> = _isSirenActive.asStateFlow()

  private val _isStrobeActive = MutableStateFlow(false)
  val isStrobeActive: StateFlow<Boolean> = _isStrobeActive.asStateFlow()

  private var sirenJob: Job? = null
  private var strobeJob: Job? = null
  private var audioTrack: AudioTrack? = null

  private val cameraManager: CameraManager? by lazy {
    context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
  }

  private val torchCameraId: String? by lazy {
    try {
      cameraManager?.cameraIdList?.firstOrNull { id ->
        cameraManager?.getCameraCharacteristics(id)
          ?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
      }
    } catch (e: Exception) {
      null
    }
  }

  // --- SIREN ALARM SYNTHESIS ---
  fun toggleSiren() {
    if (_isSirenActive.value) {
      stopSiren()
    } else {
      startSiren()
    }
  }

  fun startSiren() {
    if (_isSirenActive.value) return
    _isSirenActive.value = true
    triggerSosVibration()

    sirenJob = coroutineScope.launch {
      val sampleRate = 22050
      val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
      )

      val track = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(bufferSize * 2)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

      audioTrack = track
      track.play()

      val buffer = ShortArray(bufferSize)
      var currentFreq = 700.0
      var freqIncreasing = true
      var phase = 0.0

      try {
        while (isActive && _isSirenActive.value) {
          // Modulate frequency between 700 Hz and 1400 Hz for high-pitch emergency siren
          if (freqIncreasing) {
            currentFreq += 18.0
            if (currentFreq >= 1400.0) freqIncreasing = false
          } else {
            currentFreq -= 18.0
            if (currentFreq <= 700.0) freqIncreasing = true
          }

          val phaseIncrement = 2.0 * Math.PI * currentFreq / sampleRate
          for (i in buffer.indices) {
            buffer[i] = (sin(phase) * 32000).toInt().toShort()
            phase += phaseIncrement
            if (phase > 2.0 * Math.PI) {
              phase -= 2.0 * Math.PI
            }
          }
          track.write(buffer, 0, buffer.size)
        }
      } catch (e: Exception) {
        Log.e("SafetyToolkit", "Siren error", e)
      } finally {
        try {
          track.stop()
          track.release()
        } catch (e: Exception) {
          // ignore
        }
        audioTrack = null
      }
    }
  }

  fun stopSiren() {
    _isSirenActive.value = false
    sirenJob?.cancel()
    sirenJob = null
    try {
      audioTrack?.stop()
      audioTrack?.release()
    } catch (e: Exception) {
      // ignore
    }
    audioTrack = null
  }

  // --- STROBE FLASHLIGHT ---
  fun toggleStrobe() {
    if (_isStrobeActive.value) {
      stopStrobe()
    } else {
      startStrobe()
    }
  }

  fun startStrobe() {
    val camId = torchCameraId ?: return
    if (_isStrobeActive.value) return
    _isStrobeActive.value = true

    strobeJob = coroutineScope.launch {
      var flashOn = false
      try {
        while (isActive && _isStrobeActive.value) {
          flashOn = !flashOn
          try {
            cameraManager?.setTorchMode(camId, flashOn)
          } catch (e: CameraAccessException) {
            Log.e("SafetyToolkit", "Torch toggle error", e)
          }
          delay(85) // Rapid disorientation strobe frequency (~11 Hz)
        }
      } finally {
        try {
          cameraManager?.setTorchMode(camId, false)
        } catch (e: Exception) {
          // ignore
        }
      }
    }
  }

  fun stopStrobe() {
    _isStrobeActive.value = false
    strobeJob?.cancel()
    strobeJob = null
    torchCameraId?.let { camId ->
      try {
        cameraManager?.setTorchMode(camId, false)
      } catch (e: Exception) {
        // ignore
      }
    }
  }

  // --- HAPTIC SOS VIBRATION ---
  fun triggerSosVibration() {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    vibrator?.let { v ->
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // SOS Morse code pattern: 3 shorts, 3 longs, 3 shorts
        val timings = longArrayOf(0, 150, 100, 150, 100, 150, 200, 400, 100, 400, 100, 400, 200, 150, 100, 150, 100, 150)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255)
        v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        v.vibrate(longArrayOf(0, 200, 100, 200, 100, 200), -1)
      }
    }
  }

  // --- FULL SOS PROTOCOL ---
  fun triggerFullSos(phoneNumbers: List<String>, currentAddress: String, lat: Double, lng: Double) {
    startSiren()
    startStrobe()
    triggerSosVibration()
    sendSosSmsIntent(phoneNumbers, currentAddress, lat, lng)
  }

  fun stopAllEmergencyProtocols() {
    stopSiren()
    stopStrobe()
  }

  // --- COMPLIANT ACTION_SENDTO INTENT ---
  fun sendSosSmsIntent(phoneNumbers: List<String>, currentAddress: String, lat: Double, lng: Double) {
    val targetNumber = phoneNumbers.firstOrNull() ?: ""
    val locationUrl = "https://maps.google.com/?q=$lat,$lng"
    val smsBody = "EMERGENCY: I am walking alone and need immediate assistance! Current location: $currentAddress ($locationUrl). Please check on me or contact help!"

    val intent = Intent(Intent.ACTION_SENDTO).apply {
      data = Uri.parse("smsto:$targetNumber")
      putExtra("sms_body", smsBody)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
      context.startActivity(intent)
    } catch (e: Exception) {
      Log.e("SafetyToolkit", "Failed to launch SMS intent", e)
    }
  }

  // --- COMPLIANT ACTION_DIAL INTENT ---
  fun dialEmergencyNumber(phoneNumber: String = "911") {
    val intent = Intent(Intent.ACTION_DIAL).apply {
      data = Uri.parse("tel:$phoneNumber")
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
      context.startActivity(intent)
    } catch (e: Exception) {
      Log.e("SafetyToolkit", "Failed to launch dialer intent", e)
    }
  }
}
