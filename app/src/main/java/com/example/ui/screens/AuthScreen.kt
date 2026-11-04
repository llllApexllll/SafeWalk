package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.SafetyTeal
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AppleIcon(modifier: Modifier = Modifier, tint: Color = Color.White) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
      // Leaf
      moveTo(w * 0.54f, h * 0.05f)
      quadraticBezierTo(w * 0.68f, h * 0.06f, w * 0.65f, h * 0.22f)
      quadraticBezierTo(w * 0.50f, h * 0.20f, w * 0.54f, h * 0.05f)
      close()
      // Main body
      moveTo(w * 0.50f, h * 0.32f)
      cubicTo(w * 0.42f, h * 0.25f, w * 0.28f, h * 0.26f, w * 0.20f, h * 0.36f)
      cubicTo(w * 0.08f, h * 0.50f, w * 0.12f, h * 0.76f, w * 0.25f, h * 0.94f)
      cubicTo(w * 0.32f, h * 1.02f, w * 0.41f, h * 1.00f, w * 0.50f, h * 0.96f)
      cubicTo(w * 0.59f, h * 1.00f, w * 0.68f, h * 1.02f, w * 0.75f, h * 0.94f)
      cubicTo(w * 0.88f, h * 0.76f, w * 0.92f, h * 0.50f, w * 0.80f, h * 0.36f)
      cubicTo(w * 0.72f, h * 0.26f, w * 0.58f, h * 0.25f, w * 0.50f, h * 0.32f)
      close()
    }
    drawPath(path = path, color = tint)
  }
}

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit,
  onContinueAsGuest: () -> Unit = {}
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MidnightBlack)
      .testTag("auth_screen")
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
    ) {
      // Hero Emblem
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(96.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              colors = listOf(AmberGlow.copy(alpha = 0.35f), Color.Transparent)
            )
          )
          .border(2.dp, AmberGlow, CircleShape)
      ) {
        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = null,
          tint = AmberGlow,
          modifier = Modifier.size(52.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "SafeWalk",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )

      Text(
        text = "Night Route Guardian & Real-Time Security",
        style = MaterialTheme.typography.bodyMedium,
        color = SafetyTeal,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))

      Card(
        colors = CardDefaults.cardColors(containerColor = MidnightCard),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(20.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SafetyTeal, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Cloud-Verified Safety Network",
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 14.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Sign in to access real-time route alerts, cloud-synchronized emergency guardians, and verified crowdsourced hazard updates.",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              isLoading = true
              errorMessage = null
              onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                  isLoading = false
                  onAuthSuccess()
                },
                onAuthError = { err ->
                  isLoading = false
                  errorMessage = err
                },
                scope = coroutineScope,
                onAuthCancelled = {
                  isLoading = false
                }
              )
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
              containerColor = SafetyTeal,
              contentColor = MidnightBlack
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("google_sign_in_button")
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MidnightBlack,
                strokeWidth = 2.dp
              )
            } else {
              Text(
                text = "Sign in with Google",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MidnightBlack
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Apple Sign In (Supports Apple / iPhone users and cross-platform identity)
          Button(
            onClick = {
              val activity = context as? Activity
              if (activity == null) {
                errorMessage = "Activity unavailable for Apple Sign-In"
                return@Button
              }
              isLoading = true
              errorMessage = null
              onAppleSignInClicked(
                activity = activity,
                onAuthSuccess = {
                  isLoading = false
                  onAuthSuccess()
                },
                onAuthError = { err ->
                  isLoading = false
                  errorMessage = err
                },
                scope = coroutineScope
              )
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF0F172A),
              contentColor = Color.White
            ),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("apple_sign_in_button")
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
            } else {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                AppleIcon(modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Sign in with Apple",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = Color.White
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
          ) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(MidnightBorder))
            Text(
              text = "OR",
              color = Color(0xFF64748B),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 12.dp)
            )
            Box(modifier = Modifier.weight(1f).height(1.dp).background(MidnightBorder))
          }

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedButton(
            onClick = onContinueAsGuest,
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = AmberGlow
            ),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = androidx.compose.ui.graphics.SolidColor(AmberGlow)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("continue_as_guest_button")
          ) {
            Icon(
              imageVector = Icons.Default.DirectionsWalk,
              contentDescription = null,
              tint = AmberGlow,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Free Guest Access (No Sign-In)",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = AmberGlow
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "⚡ Instant access to safe route mapping, 24/7 Safe Havens, and offline SOS tools on this device.",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
          )

          if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = errorMessage ?: "",
              color = Color(0xFFFCA5A5),
              fontSize = 12.sp,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }
  }
}

fun onGoogleSignInClicked(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  onAuthError: (String) -> Unit,
  scope: CoroutineScope,
  onAuthCancelled: () -> Unit = {}
) {
  val clientId = try {
    context.getString(R.string.default_web_client_id)
  } catch (e: Exception) {
    onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
    return
  }

  val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
  val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

  scope.launch {
    try {
      val result = credentialManager.getCredential(context as Activity, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        Firebase.auth.signInWithCredential(authCredential).await()
        onAuthSuccess()
      } else {
        onAuthError("Unexpected credential type returned")
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
      onAuthCancelled()
    } catch (e: Exception) {
      Log.e("Auth", "Google Sign-In failed", e)
      onAuthError(e.localizedMessage ?: "Sign in failed")
    }
  }
}

fun onAppleSignInClicked(
  activity: Activity,
  onAuthSuccess: () -> Unit,
  onAuthError: (String) -> Unit,
  scope: CoroutineScope
) {
  val provider = OAuthProvider.newBuilder("apple.com")
  provider.scopes = listOf("email", "name")

  scope.launch {
    try {
      Firebase.auth.startActivityForSignInWithProvider(activity, provider.build())
        .addOnSuccessListener { authResult ->
          Log.d("Auth", "Apple Sign-In succeeded for user: ${authResult.user?.uid}")
          onAuthSuccess()
        }
        .addOnFailureListener { exception ->
          Log.e("Auth", "Apple Sign-In failed", exception)
          onAuthError(exception.localizedMessage ?: "Apple Sign-In could not be completed.")
        }
    } catch (e: Exception) {
      Log.e("Auth", "Failed to launch Apple Sign-In", e)
      onAuthError(e.localizedMessage ?: "Apple Sign-In launch error")
    }
  }
}

fun attemptAutoSignIn(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  onUnauthenticated: () -> Unit,
  scope: CoroutineScope
) {
  if (Firebase.auth.currentUser != null) {
    onAuthSuccess()
    return
  }
  val clientId = try {
    context.getString(R.string.default_web_client_id)
  } catch (e: Exception) {
    onUnauthenticated()
    return
  }

  val googleIdOption = GetGoogleIdOption.Builder()
    .setFilterByAuthorizedAccounts(true)
    .setServerClientId(clientId)
    .setAutoSelectEnabled(true)
    .build()

  val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

  scope.launch {
    try {
      val result = credentialManager.getCredential(context, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        Firebase.auth.signInWithCredential(authCredential).await()
        onAuthSuccess()
      } else {
        onUnauthenticated()
      }
    } catch (e: Exception) {
      onUnauthenticated()
    }
  }
}

fun signOutUser(
  context: Context,
  credentialManager: CredentialManager,
  onSignOutComplete: () -> Unit,
  scope: CoroutineScope
) {
  Firebase.auth.signOut()
  scope.launch {
    try {
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    } catch (e: Exception) {
      Log.e("Auth", "Failed to clear credential state", e)
    } finally {
      onSignOutComplete()
    }
  }
}
