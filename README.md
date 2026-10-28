# Nova Client (Minecraft 26.3, Fabric)

## Jar ohne Installation bauen (GitHub Actions)
1. Kostenloses GitHub-Konto, neues Repository anlegen (privat ist ok).
2. Den gesamten Inhalt dieses Ordners hochladen (auch den versteckten Ordner `.github`).
3. Tab "Actions" -> Workflow "build" abwarten (~3 Min.).
4. Beim fertigen Lauf unter "Artifacts" `nova-client` herunterladen und die ZIP entpacken.
   Die Datei `nova-client-1.0.0.jar` (nicht `-sources`) ist die Mod.
5. Bei einem roten Haken: Lauf öffnen, Fehlermeldung kopieren und mir schicken.

## Installieren
- Fabric Loader 0.19.5+ für 26.3 installieren (fabricmc.net/use), Java 25 nötig.
- `nova-client-1.0.0.jar` + Fabric API 0.165.0+26.3 nach `%appdata%\.minecraft\mods`.
- Im Spiel: Rechte Shift öffnet das ClickGUI.
