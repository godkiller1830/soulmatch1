package com.example.matrimony

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SoulMatchApp()
        }
    }
}

@Composable
fun SoulMatchApp() {
    // This would be replaced with a proper Navigation component in a real app
    var isLoggedIn by remember { mutableStateOf(false) }
    if (isLoggedIn) {
        //MainScreen() // Composable function for the main part of your app
        ProfileSetupScreen(onProfileCompleted = {})
    } else {
        LoginScreen(onLoginSuccess = { isLoggedIn = true })
    }
}

@Composable
fun MainScreen() {
    // Placeholder for the main content of your app
    Text("Welcome to Fast Feast!")
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.fastfeast_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp).align(Alignment.CenterHorizontally) // Adjust the size as needed
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Login to SoulMatch", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { loginWithEmail(email, password, context, { isLoading = it }, onLoginSuccess) },
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Login")
        }
    }
}

fun loginWithEmail(email: String, password: String, context: Context, isLoading: (Boolean) -> Unit, onLoginSuccess: () -> Unit) {
    isLoading(true)
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        isLoading(false)
        if (task.isSuccessful) {
            onLoginSuccess()
        } else {
            Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
        }
    }
}


@Composable
fun ProfileSetupScreen(onProfileCompleted: () -> Unit) {
    // Instantiate a ViewModel using the viewModel() function.
    val viewModel: ProfileViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsState().value
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var interests by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.isComplete, uiState.errorMessage) {
        when {
            uiState.isComplete -> onProfileCompleted()
            uiState.errorMessage != null -> Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    if (uiState.isLoading) {
        CircularProgressIndicator()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Create Your Profile", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") }
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.height(150.dp)
            )
            OutlinedTextField(
                value = interests,
                onValueChange = { interests = it },
                label = { Text("Interests") }
            )

            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                if (name.isNotBlank() && age.isNotBlank() && bio.isNotBlank() && interests.isNotBlank()) {
                    viewModel.addUserProfile(name, age, bio, interests)
                } else {
                    Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Complete Profile")
            }

            // Display error messages if any
            uiState.errorMessage?.let { errorMessage ->
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    MaterialTheme {
        LoginScreen(onLoginSuccess = {})
    }
}


fun addUserProfileToFirestore(userProfile: UserProfile) {
    val db = FirebaseFirestore.getInstance()
    db.collection("userProfiles").document(userProfile.uid).set(userProfile)
        .addOnSuccessListener {
            Log.d("Firestore", "DocumentSnapshot successfully written!")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error writing document", e)
        }
}

fun getUserProfileFromFirestore(uid: String, onResult: (UserProfile?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("userProfiles").document(uid).get()
        .addOnSuccessListener { document ->
            if (document != null) {
                val userProfile = document.toObject(UserProfile::class.java)
                onResult(userProfile)
            } else {
                Log.d("Firestore", "No such document")
            }
        }
        .addOnFailureListener { exception ->
            Log.d("Firestore", "get failed with ", exception)
            onResult(null)
        }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MaterialTheme {
        MainScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileSetupScreen() {
    // This preview won't be interactive with ViewModel
    ProfileSetupScreen(onProfileCompleted = {})
}