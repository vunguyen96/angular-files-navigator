package com.github.angularfilesnavigator

/**
 * A related file "role" the user can navigate to.
 *
 * Most roles build their candidate file names from the shared [base] name
 * (`app.component.html` and `app.component.ts` share the base `app`). The
 * [SPEC] role is special: a unit test mirrors the *current* file, so
 * `abc.selectors.ts` maps to `abc.selectors.spec.ts`, not `abc.spec.ts`.
 *
 * The first existing candidate wins.
 */
enum class TargetRole(
    val displayName: String,
    /** Single-letter mnemonic shown in the picker and used as its shortcut key. */
    val mnemonic: Char,
    /** Classpath location of the badge icon shown in menus and the picker. */
    val iconPath: String,
    private val templates: List<String>
) {

    // --- Standard Angular component files ---------------------------------
    COMPONENT_TS("component (.ts)", 'T', "/icons/ts.svg", listOf("{b}.component.ts", "{b}.ts")) {
        override fun candidates(base: String, currentName: String): List<String> {
            // From a spec, the implementation source sits next to it:
            // `abc.selectors.spec.ts` -> `abc.selectors.ts`.
            val fromCurrent = currentName.takeIf { it.lowercase().endsWith(SPEC_SUFFIX) }
                ?.let { it.dropLast(SPEC_SUFFIX.length) + TS_SUFFIX }
            // Fallbacks cover navigating from a template/stylesheet.
            return listOfNotNull(
                fromCurrent,
                "$base.component.ts",
                "$base.ts"
            ).distinct()
        }
    },
    TEMPLATE("template (.html)", 'H', "/icons/html.svg", listOf("{b}.component.html", "{b}.html")),
    STYLES(
        "styles (.scss/.sass/.less/.css)",
        'S',
        "/icons/styles.svg",
        listOf(
            "{b}.component.scss", "{b}.scss",
            "{b}.component.sass", "{b}.sass",
            "{b}.component.less", "{b}.less",
            "{b}.component.css", "{b}.css"
        )
    ),
    SPEC("unit test (.spec.ts)", 'U', "/icons/spec.svg", emptyList()) {
        override fun candidates(base: String, currentName: String): List<String> {
            val lower = currentName.lowercase()
            val fromCurrent = when {
                // Already in a spec: toggle back to the implementation file.
                lower.endsWith(SPEC_SUFFIX) ->
                    currentName.dropLast(SPEC_SUFFIX.length) + TS_SUFFIX
                // Any TypeScript file: its spec sits right next to it.
                lower.endsWith(TS_SUFFIX) ->
                    currentName.dropLast(TS_SUFFIX.length) + SPEC_SUFFIX
                else -> null
            }
            // Fallbacks cover navigating from a template/stylesheet.
            return listOfNotNull(
                fromCurrent,
                "$base.component.spec.ts",
                "$base.spec.ts"
            ).distinct()
        }
    },

    // --- NgRx files -------------------------------------------------------
    REDUCER("reducer", 'R', "/icons/reducer.svg", listOf("{b}.reducer.ts", "{b}.reducers.ts")),
    EFFECTS("effects", 'E', "/icons/effects.svg", listOf("{b}.effects.ts", "{b}.effect.ts")),
    SELECTORS("selectors", 'L', "/icons/selectors.svg", listOf("{b}.selectors.ts", "{b}.selector.ts")),
    ACTIONS("actions", 'A', "/icons/actions.svg", listOf("{b}.actions.ts", "{b}.action.ts")),
    FACADE("facade", 'F', "/icons/facade.svg", listOf("{b}.facade.ts")),
    MODULE("module", 'M', "/icons/module.svg", listOf("{b}.module.ts"));

    /**
     * Builds the ordered list of candidate file names.
     *
     * @param base        the shared base name (e.g. `app` or `user`)
     * @param currentName the name of the file the caret is currently in
     */
    open fun candidates(base: String, currentName: String): List<String> =
        templates.map { it.replace("{b}", base) }

    companion object {
        private const val TS_SUFFIX = ".ts"
        private const val SPEC_SUFFIX = ".spec.ts"
    }
}
