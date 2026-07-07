// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.android.example.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import ch.sbb.maps.android.example.R
import ch.sbb.maps.design.SBBColors

@Composable
fun VersionDialog(
    version: String,
    showDialog: MutableState<Boolean>,
) {
    if (showDialog.value) {
        AlertDialog(
            containerColor = SBBColors.milk,
            onDismissRequest = { showDialog.value = false },
            title = {
                Text(
                    text = stringResource(id = R.string.app_name),
                )
            },
            text = { Text(text = "Version: $version") },
            confirmButton = {
                Button(
                    onClick = { showDialog.value = false },
                ) {
                    Text(text = "OK", color = SBBColors.white)
                }
            },
        )
    }
}
