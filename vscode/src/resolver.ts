import * as path from "path";

export type TargetRole =
  | "component"
  | "template"
  | "styles"
  | "spec"
  | "reducer"
  | "effects"
  | "selectors"
  | "actions"
  | "facade"
  | "module";

export interface RoleDefinition {
  readonly role: TargetRole;
  readonly displayName: string;
  readonly mnemonic: string;
}

export const roles: readonly RoleDefinition[] = [
  { role: "component", displayName: "component (.ts)", mnemonic: "T" },
  { role: "template", displayName: "template (.html)", mnemonic: "H" },
  { role: "styles", displayName: "styles (.scss/.sass/.less/.css)", mnemonic: "S" },
  { role: "spec", displayName: "unit test (.spec.ts)", mnemonic: "U" },
  { role: "reducer", displayName: "reducer", mnemonic: "R" },
  { role: "effects", displayName: "effects", mnemonic: "E" },
  { role: "selectors", displayName: "selectors", mnemonic: "L" },
  { role: "actions", displayName: "actions", mnemonic: "A" },
  { role: "facade", displayName: "facade", mnemonic: "F" },
  { role: "module", displayName: "module", mnemonic: "M" }
];

const suffixes = [
  ".component.ts", ".component.html", ".component.scss", ".component.sass",
  ".component.less", ".component.css", ".reducer.ts", ".reducers.ts",
  ".effects.ts", ".effect.ts", ".selectors.ts", ".selector.ts", ".actions.ts",
  ".action.ts", ".facade.ts", ".state.ts", ".service.ts", ".module.ts",
  ".directive.ts", ".pipe.ts", ".guard.ts", ".resolver.ts", ".model.ts",
  ".models.ts", ".scss", ".sass", ".less", ".css", ".html", ".ts"
].sort((a, b) => b.length - a.length);

export function baseOf(fileName: string): string {
  const normalized = fileName.toLowerCase().endsWith(".spec.ts")
    ? `${fileName.slice(0, -".spec.ts".length)}.ts`
    : fileName;
  const lower = normalized.toLowerCase();
  const suffix = suffixes.find((candidate) => lower.endsWith(candidate));
  if (suffix) {
    return normalized.slice(0, -suffix.length);
  }
  return normalized.includes(".")
    ? normalized.slice(0, normalized.lastIndexOf("."))
    : normalized;
}

export function candidates(role: TargetRole, base: string, currentName: string): string[] {
  const currentLower = currentName.toLowerCase();
  if (role === "component") {
    const fromCurrent = currentLower.endsWith(".spec.ts")
      ? `${currentName.slice(0, -".spec.ts".length)}.ts`
      : undefined;
    return unique([fromCurrent, `${base}.component.ts`, `${base}.ts`]);
  }
  if (role === "spec") {
    const fromCurrent = currentLower.endsWith(".spec.ts")
      ? `${currentName.slice(0, -".spec.ts".length)}.ts`
      : currentLower.endsWith(".ts")
        ? `${currentName.slice(0, -".ts".length)}.spec.ts`
        : undefined;
    return unique([fromCurrent, `${base}.component.spec.ts`, `${base}.spec.ts`]);
  }
  const templates: Partial<Record<TargetRole, string[]>> = {
    template: [`${base}.component.html`, `${base}.html`],
    styles: [".scss", ".sass", ".less", ".css"].flatMap((extension) => [
      `${base}.component${extension}`, `${base}${extension}`
    ]),
    reducer: [`${base}.reducer.ts`, `${base}.reducers.ts`],
    effects: [`${base}.effects.ts`, `${base}.effect.ts`],
    selectors: [`${base}.selectors.ts`, `${base}.selector.ts`],
    actions: [`${base}.actions.ts`, `${base}.action.ts`],
    facade: [`${base}.facade.ts`],
    module: [`${base}.module.ts`]
  };
  return templates[role] ?? [];
}

function unique(values: Array<string | undefined>): string[] {
  return [...new Set(values.filter((value): value is string => Boolean(value)))];
}

export function commonPrefixLength(left: string, right: string): number {
  const normalizedLeft = path.normalize(left);
  const normalizedRight = path.normalize(right);
  let index = 0;
  while (index < normalizedLeft.length &&
    index < normalizedRight.length &&
    normalizedLeft[index].toLowerCase() === normalizedRight[index].toLowerCase()) {
    index++;
  }
  return index;
}
