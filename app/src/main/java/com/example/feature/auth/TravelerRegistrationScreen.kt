package com.example.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Museum
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Terrain
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.model.TravelerStyles

@Composable
fun TravelerRegistrationScreen(
    initialName: String,
    email: String,
    isSaving: Boolean,
    errorMessage: String?,
    onCompleteRegistration: (fullName: String, homeCity: String, travelerStyle: String) -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember(initialName) { mutableStateOf(initialName.ifBlank { "" }) }
    var homeCity by remember { mutableStateOf("Batangas") }
    var selectedStyle by remember { mutableStateOf(TravelerStyles.all.first()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 560.dp)) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Step 2 of 2 • Traveler Registration",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Personalize your San Juan journey",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Signed in as $email. Complete your traveler profile to sync itineraries with Cloud Firestore.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(22.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    validationError = null
                },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("registration_name_input"),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = homeCity,
                onValueChange = {
                    homeCity = it
                    validationError = null
                },
                label = { Text("Home City / Municipality") },
                leadingIcon = { Icon(Icons.Rounded.LocationCity, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("registration_city_input"),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Preferred San Juan Travel Style",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            TravelerStyles.all.forEach { style ->
                val isSelected = selectedStyle == style
                val icon = styleIcon(style)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedStyle = style }
                        .testTag("style_option_${style.lowercase().replace(" ", "_")}"),
                    shape = MaterialTheme.shapes.medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = style,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            val displayError = validationError ?: errorMessage
            if (displayError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = displayError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val trimmedName = fullName.trim()
                    val trimmedCity = homeCity.trim()
                    when {
                        trimmedName.length < 2 -> validationError = "Please enter your full name (at least 2 characters)."
                        trimmedCity.isEmpty() -> validationError = "Please enter your home city or municipality."
                        else -> onCompleteRegistration(trimmedName, trimmedCity, selectedStyle)
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("complete_registration_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Creating Profile...")
                } else {
                    Text(stringResource(id = R.string.action_complete_registration))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onSignOutClick,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("registration_sign_out_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Switch Google Account")
            }
        }
    }
}

private fun styleIcon(style: String): ImageVector = when (style) {
    "Beach & Marine" -> Icons.Rounded.BeachAccess
    "Eco-Hiker" -> Icons.Rounded.Terrain
    "Heritage & Crafts" -> Icons.Rounded.Museum
    else -> Icons.Rounded.Restaurant
}
