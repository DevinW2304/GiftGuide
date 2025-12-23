package com.devin.giftguide.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onContinue: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = GG_Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = GG_Green
            ) {
                Text(
                    text = "SmartGift Guide",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GG_Black
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Find thoughtful gifts fast.\nSave what you like.",
                style = MaterialTheme.typography.bodyLarge,
                color = androidx.compose.ui.graphics.Color(0xB3000000)
            )

            Spacer(Modifier.height(22.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onContinue,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GG_Blue,
                    contentColor = GG_Black
                )
            ) {
                Text("Continue as guest", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Mock login for now — real auth later.",
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color(0x99000000)
            )
        }
    }
}
