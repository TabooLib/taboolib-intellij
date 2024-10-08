package org.tabooproject.development.component

import com.intellij.openapi.ui.Messages
import com.intellij.ui.AddDeleteListPanel
import org.tabooproject.development.util.Assets

class AddDeleteStringListPanel(
    title: String,
    initial: List<String>,
    private val dialogMessage: String,
    private val dialogTitle: String,
    defaultHeight: Int = 100,
) : AddDeleteListPanel<String>(title, initial) {

    init {
        preferredSize = preferredSize.apply {
            width += 200
            height += defaultHeight
        }
    }

    override fun findItemToAdd(): String? {
        return Messages.showInputDialog(
            dialogMessage,
            dialogTitle,
            Assets.TABOO_32x32,
            "",
            null
        )
    }

    fun export() = listItems.map { it as String }
}