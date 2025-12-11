package com.example.controlerapppromtai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controlerapppromtai.viewmodel.AuthViewModel
import com.example.controlerapppromtai.viewmodel.AuthState

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (authState is AuthState.Authenticated) {
            val auth = authState as AuthState.Authenticated
            Text(text = "Email: ${auth.user.email}")
            Text(text = "Fullname: ${auth.user.fullname}")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                authViewModel.logout(auth.token.refreshToken)
                onLogout()
            }) {
                Text("Logout")
            }
        }
    }
}

