// SPDX-License-Identifier: MIT
// Copyright (c) 2024 Schweizerische Bundesbahnen AG (Swiss Federal Railways)

package ch.sbb.maps.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ch.sbb.maps.R

/**
 * Based on [SBB Mobile Design System](https://digital.sbb.ch/de/design-system/mobile/overview/).
 * Note: This component may be replaced by the official SBB Design System library in the future.
 *
 * Center aligned [CenterAlignedTopAppBar] with optional back navigation and logo action.
 *
 * @param title text displayed in the centre of the app bar.
 * @param navController [NavHostController] used for back navigation.
 * @param canNavigateBack whether the back button is shown.
 * @param navigationIcon optional custom navigation icon composable.
 * @param onNavigationClick optional callback overriding the default navigateUp.
 * @param onSBBLogoClick optional callback when the SBB logo is tapped.
 * @param actions trailing actions; defaults to the SBB logo icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun SBBHeaderSmall(
    title: String,
    navController: NavHostController,
    canNavigateBack: Boolean,
    navigationIcon: @Composable (() -> Unit)? = null,
    onNavigationClick: (() -> Unit)? = null,
    onSBBLogoClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = {
        Icon(
            ImageVector.vectorResource(id = R.drawable.logo_sbb),
            contentDescription = "SBB logo",
            tint = SBBColors.white,
            modifier = Modifier.clickable { onSBBLogoClick?.invoke() },
        )
    },
) {
    CenterAlignedTopAppBar(
        modifier =
            Modifier
                .background(SBBColors.red)
                .padding(horizontal = 14.dp),
        colors =
            TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = SBBColors.red,
                titleContentColor = SBBColors.white,
            ),
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SBBColors.white,
                style = SBBTypography.bodyLarge,
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                Box(
                    modifier =
                        Modifier
                            .clickable(
                                onClick = { onNavigationClick?.invoke() ?: navController.navigateUp() },
                                indication = null, // remove ripple effect
                                interactionSource = remember { MutableInteractionSource() }, // set interaction
                                role = Role.Button,
                            ),
                ) {
                    navigationIcon ?: Icon(
                        ImageVector.vectorResource(id = R.drawable.chevron_left_small),
                        contentDescription = "Back",
                        tint = SBBColors.white,
                    )
                }
            }
        },
        actions = actions ?: {},
    )
}

@Preview
@Composable
public fun SBBHeaderSmallPreview() {
    SBBHeaderSmall(
        title = "SBB App Bar",
        navController = rememberNavController(),
        canNavigateBack = false,
    )
}

@Preview
@Composable
public fun SBBHeaderSmallWithBackButtonPreview() {
    SBBHeaderSmall(
        title = "SBB App Bar",
        navController = rememberNavController(),
        canNavigateBack = true,
    )
}
