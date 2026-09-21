package com.vna.angularfilesnavigator.action

import com.vna.angularfilesnavigator.AngularFilesNavigator
import com.vna.angularfilesnavigator.TargetRole
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile

/**
 * Shows a popup near the caret listing every related file that exists for the
 * current file. Each entry carries a single-letter mnemonic ([TargetRole.mnemonic]),
 * so the user can press `Ctrl+Alt+A` and then `T`, `U`, `S`, ... to jump, or use
 * the arrow keys and Enter to pick from the list.
 */
class ShowRelatedFilesPopupAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabled = e.project != null && file != null && !file.isDirectory
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val current = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val entries = collectEntries(project, current)
        if (entries.isEmpty()) {
            AngularFilesNavigator.notifyNoRelatedFiles(project)
            return
        }

        val group = DefaultActionGroup().apply {
            entries.forEach { (role, file) -> add(openFileAction(project, role, file)) }
        }

        JBPopupFactory.getInstance()
            .createActionGroupPopup(
                "Angular Files Navigator",
                group,
                e.dataContext,
                /* showNumbers = */ false,
                /* showDisabledActions = */ false,
                /* honorActionMnemonics = */ true,
                /* disposeCallback = */ null,
                /* maxRowCount = */ -1,
                /* preselectActionCondition = */ null
            )
            .showInBestPositionFor(e.dataContext)
    }

    /**
     * Resolves each role in declaration order, keeping the first role that maps
     * to a given file so the same file never appears twice.
     */
    private fun collectEntries(
        project: Project,
        current: VirtualFile
    ): List<Pair<TargetRole, VirtualFile>> {
        val seen = HashSet<String>()
        return TargetRole.values()
            .mapNotNull { role -> AngularFilesNavigator.resolve(project, current, role)?.let { role to it } }
            .filter { (_, file) -> seen.add(file.path) }
    }

    private fun openFileAction(project: Project, role: TargetRole, file: VirtualFile): AnAction {
        val action = object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {
                FileEditorManager.getInstance(project).openFile(file, true)
            }
        }
        // A leading "&<letter>" marks the mnemonic honoured by the popup.
        action.templatePresentation.setText("&${role.mnemonic}  ${role.displayName}  \u2014  ${file.name}", true)
        action.templatePresentation.icon = IconLoader.getIcon(role.iconPath, ShowRelatedFilesPopupAction::class.java)
        return action
    }
}
