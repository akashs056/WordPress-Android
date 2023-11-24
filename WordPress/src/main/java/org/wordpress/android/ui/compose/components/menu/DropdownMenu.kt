package org.wordpress.android.ui.compose.components.menu

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import org.wordpress.android.R
import org.wordpress.android.ui.compose.components.menu.DropdownMenuItemData.Item
import org.wordpress.android.ui.compose.components.menu.DropdownMenuItemData.SubMenu
import org.wordpress.android.ui.compose.theme.AppThemeEditor

/**
 * DropdownMenu component. Consists of a DropdownMenuButton that opens a DropdownMenuItemsContainer when tapped.
 * @param items the dropdown menu items to be shown. There should be only one default item.
 */
@Composable
fun DropdownMenu(items: List<DropdownMenuItemData>) {
    require(items.hasSingleDefaultItem()) { "DropdownMenu must have one default item." }
    Column {
        var isMenuOpen by remember { mutableStateOf(false) }
        DropdownMenuButton(
            selectedItem = items.defaultItem(),
            onClick = {
                isMenuOpen = !isMenuOpen
            }
        )
        if (isMenuOpen) {
            DropdownMenuItemList(items)
        }
    }
}

private fun List<DropdownMenuItemData>.hasSingleDefaultItem() = filter { it.isDefault }.size == 1

private fun List<DropdownMenuItemData>.defaultItem() =
    find { it.isDefault } ?: throw IllegalArgumentException("Default item must not be null.")

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditPostSettingsJetpackSocialSharesContainerPreview() {
    AppThemeEditor {
        DropdownMenu(
            items = listOf(
                Item(
                    id = "text1",
                    text = "Text only",
                    onClick = {},
                ),
                Item(
                    id = "textAndIcon1",
                    text = "Text and Icon",
                    isDefault = true,
                    leftIcon = R.drawable.ic_jetpack_logo_white_24dp,
                    hasDivider = true,
                    onClick = {},
                ),
                SubMenu(
                    id = "subMenu1",
                    text = "SubMenu",
                    items = listOf(
                        Item(
                            id = "subMenu1_text1",
                            text = "Text only",
                            onClick = {},
                        )
                    ),
                    rightIcon = R.drawable.ic_arrow_right_black_24dp,
                    onClick = {},
                ),
            )
        )
    }
}
