package com.github.angularfilesnavigator.action

import com.github.angularfilesnavigator.AngularFilesNavigator
import com.github.angularfilesnavigator.TargetRole
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.util.IconLoader

/**
 * Base class for every "jump to related file" action. Concrete subclasses only
 * bind a [TargetRole]; all behaviour lives here.
 */
abstract class NavigateToRoleAction(private val role: TargetRole) : AnAction() {

    init {
        templatePresentation.icon = IconLoader.getIcon(role.iconPath, NavigateToRoleAction::class.java)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabled = e.project != null && file != null && !file.isDirectory
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val current = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        AngularFilesNavigator.navigate(project, current, role)
    }
}

class GoToComponentTsAction : NavigateToRoleAction(TargetRole.COMPONENT_TS)
class GoToTemplateAction : NavigateToRoleAction(TargetRole.TEMPLATE)
class GoToStylesAction : NavigateToRoleAction(TargetRole.STYLES)
class GoToSpecAction : NavigateToRoleAction(TargetRole.SPEC)

class GoToReducerAction : NavigateToRoleAction(TargetRole.REDUCER)
class GoToEffectsAction : NavigateToRoleAction(TargetRole.EFFECTS)
class GoToSelectorsAction : NavigateToRoleAction(TargetRole.SELECTORS)
class GoToActionsAction : NavigateToRoleAction(TargetRole.ACTIONS)
class GoToFacadeAction : NavigateToRoleAction(TargetRole.FACADE)
class GoToModuleAction : NavigateToRoleAction(TargetRole.MODULE)
