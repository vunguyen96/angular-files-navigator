import * as path from "path";
import * as vscode from "vscode";
import {
  baseOf,
  candidates,
  commonPrefixLength,
  roles,
  RoleDefinition,
  TargetRole
} from "./resolver";

export function activate(context: vscode.ExtensionContext): void {
  context.subscriptions.push(
    vscode.commands.registerCommand("angularFilesNavigator.showRelatedFiles", showRelatedFiles),
    vscode.commands.registerCommand("angularFilesNavigator.goToRole", goToRole)
  );
}

async function showRelatedFiles(): Promise<void> {
  const editor = vscode.window.activeTextEditor;
  if (!editor) {
    return;
  }
  const entries = (await Promise.all(
    roles.map(async (role) => ({ role, uri: await resolve(editor.document.uri, role.role) }))
  )).filter((entry): entry is { role: RoleDefinition; uri: vscode.Uri } => entry.uri !== undefined);

  if (entries.length === 0) {
    await vscode.window.showInformationMessage("No related Angular files found.");
    return;
  }

  const items = entries.map((entry) => ({
    label: `${entry.role.mnemonic}  ${entry.role.displayName}`,
    description: vscode.workspace.asRelativePath(entry.uri, false),
    uri: entry.uri,
    mnemonic: entry.role.mnemonic.toLowerCase()
  }));
  const quickPick = vscode.window.createQuickPick<typeof items[number]>();
  quickPick.items = items;
  quickPick.placeholder = "Type T, U, S, ... or use the arrow keys";
  quickPick.matchOnDescription = true;

  const openItem = async (item: typeof items[number] | undefined): Promise<void> => {
    quickPick.hide();
    quickPick.dispose();
    if (item) {
      await vscode.window.showTextDocument(item.uri, { preview: false });
    }
  };

  quickPick.onDidChangeValue((value) => {
    if (value.length !== 1) {
      return;
    }
    const item = items.find((candidate) => candidate.mnemonic === value.toLowerCase());
    if (item) {
      quickPick.activeItems = [item];
    }
  });
  quickPick.onDidAccept(() => void openItem(quickPick.selectedItems[0] ?? quickPick.activeItems[0]));
  quickPick.onDidHide(() => quickPick.dispose());
  quickPick.show();
}

async function goToRole(role?: TargetRole): Promise<void> {
  const editor = vscode.window.activeTextEditor;
  if (!editor) {
    return;
  }
  const selectedRole = role ?? await pickRole();
  if (!selectedRole) {
    return;
  }
  const uri = await resolve(editor.document.uri, selectedRole);
  if (uri) {
    await vscode.window.showTextDocument(uri, { preview: false });
  } else {
    await vscode.window.showInformationMessage(`No ${selectedRole} file found.`);
  }
}

async function pickRole(): Promise<TargetRole | undefined> {
  const selected = await vscode.window.showQuickPick(
    roles.map((role) => ({ label: `${role.mnemonic}  ${role.displayName}`, role: role.role })),
    { placeHolder: "Select a related file type" }
  );
  return selected?.role;
}

async function resolve(current: vscode.Uri, role: TargetRole): Promise<vscode.Uri | undefined> {
  const fileName = path.basename(current.fsPath);
  const names = candidates(role, baseOf(fileName), fileName);
  const directory = path.dirname(current.fsPath);

  for (const name of names) {
    const sameDirectory = vscode.Uri.file(path.join(directory, name));
    if (sameDirectory.fsPath !== current.fsPath && await exists(sameDirectory)) {
      return sameDirectory;
    }
  }

  const folder = vscode.workspace.getWorkspaceFolder(current);
  if (!folder) {
    return undefined;
  }
  const matches = (await Promise.all(names.map((name) =>
    vscode.workspace.findFiles(new vscode.RelativePattern(folder, `**/${name}`))
  ))).flat().filter((uri) => uri.fsPath !== current.fsPath);
  return matches.sort((left, right) =>
    commonPrefixLength(current.fsPath, right.fsPath) - commonPrefixLength(current.fsPath, left.fsPath) ||
    left.fsPath.length - right.fsPath.length
  )[0];
}

async function exists(uri: vscode.Uri): Promise<boolean> {
  try {
    await vscode.workspace.fs.stat(uri);
    return true;
  } catch {
    return false;
  }
}

export function deactivate(): void {}
