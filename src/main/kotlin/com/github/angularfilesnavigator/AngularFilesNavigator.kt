package com.github.angularfilesnavigator

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

/**
 * Resolves and opens the related file for the currently active file.
 *
 * Resolution strategy:
 *  1. Look in the same directory (works even while the project is indexing).
 *  2. Fall back to a project-wide file-name lookup, choosing the match whose
 *     path is closest to the current file.
 *  3. Otherwise show a non-intrusive balloon notification.
 */
object AngularFilesNavigator {

    private const val NOTIFICATION_GROUP = "Angular Files Navigator"

    fun navigate(project: Project, current: VirtualFile, role: TargetRole) {
        val target = resolve(project, current, role)
        if (target != null) {
            FileEditorManager.getInstance(project).openFile(target, true)
        } else {
            notifyNotFound(project, role, BaseNameResolver.baseOf(current.name))
        }
    }

    /**
     * Resolves the related [VirtualFile] for [role], or `null` when none exists.
     * Same-directory lookups run even while the project is indexing.
     */
    fun resolve(project: Project, current: VirtualFile, role: TargetRole): VirtualFile? {
        val base = BaseNameResolver.baseOf(current.name)
        val candidates = role.candidates(base, current.name)
        return findInSameDirectory(current, candidates)
            ?: findInProject(project, current, candidates)
    }

    /** Shows a balloon when the picker has nothing to offer. */
    fun notifyNoRelatedFiles(project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP)
            .createNotification(
                "Angular Files Navigator",
                "No related Angular files found next to the current file.",
                NotificationType.INFORMATION
            )
            .notify(project)
    }

    private fun findInSameDirectory(current: VirtualFile, candidates: List<String>): VirtualFile? {
        val dir = current.parent ?: return null
        return candidates
            .asSequence()
            .mapNotNull { dir.findChild(it) }
            .firstOrNull { it != current && !it.isDirectory && it.isValid }
    }

    private fun findInProject(
        project: Project,
        current: VirtualFile,
        candidates: List<String>
    ): VirtualFile? {
        if (DumbService.isDumb(project)) return null
        return ReadAction.compute<VirtualFile?, RuntimeException> {
            val scope = GlobalSearchScope.projectScope(project)
            candidates
                .asSequence()
                .flatMap { name -> FilenameIndex.getVirtualFilesByName(name, scope).asSequence() }
                .filter { it != current && !it.isDirectory && it.isValid }
                .sortedWith(
                    compareByDescending<VirtualFile> { current.path.commonPrefixWith(it.path).length }
                        .thenBy { it.path.length }
                )
                .firstOrNull()
        }
    }

    private fun notifyNotFound(project: Project, role: TargetRole, base: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP)
            .createNotification(
                "Angular Files Navigator",
                "No ${role.displayName} file found for \"$base\".",
                NotificationType.INFORMATION
            )
            .notify(project)
    }
}
