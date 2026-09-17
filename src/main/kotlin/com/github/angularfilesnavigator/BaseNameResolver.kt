package com.github.angularfilesnavigator

/**
 * Derives the "base" name shared by all related files of an Angular building
 * block by stripping a recognised role suffix from a file name.
 *
 * Examples:
 *  - `app.component.html`  -> `app`
 *  - `app.component.ts`    -> `app`
 *  - `app.ts`             -> `app`   (Angular v20+ flat naming)
 *  - `user.reducer.ts`     -> `user`
 *  - `user.effects.ts`     -> `user`
 */
object BaseNameResolver {

    /** Recognised suffixes, matched longest-first so `.component.ts` beats `.ts`. */
    private val suffixes: List<String> = listOf(
        // component
        ".component.ts",
        ".component.html",
        ".component.scss", ".component.sass", ".component.less", ".component.css",
        // NgRx
        ".reducer.ts", ".reducers.ts",
        ".effects.ts", ".effect.ts",
        ".selectors.ts", ".selector.ts",
        ".actions.ts", ".action.ts",
        ".facade.ts",
        ".state.ts",
        // other Angular artefacts
        ".service.ts",
        ".module.ts",
        ".directive.ts",
        ".pipe.ts",
        ".guard.ts",
        ".resolver.ts",
        ".model.ts", ".models.ts",
        // flat / fallback extensions (Angular v20+ and plain files)
        ".scss", ".sass", ".less", ".css",
        ".html",
        ".ts"
    ).distinct().sortedByDescending { it.length }

    /** Returns the base name for [fileName], preserving the original casing. */
    fun baseOf(fileName: String): String {
        // Normalise spec files first so `user.selectors.spec.ts` reduces like
        // `user.selectors.ts` (base `user`) rather than `user.selectors`.
        val normalized = if (fileName.lowercase().endsWith(".spec.ts")) {
            fileName.substring(0, fileName.length - ".spec.ts".length) + ".ts"
        } else {
            fileName
        }
        val lower = normalized.lowercase()
        val match = suffixes.firstOrNull { lower.endsWith(it) }
        return when {
            match != null -> normalized.substring(0, normalized.length - match.length)
            normalized.contains('.') -> normalized.substringBeforeLast('.')
            else -> normalized
        }
    }
}
