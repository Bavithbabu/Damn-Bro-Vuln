# Extending VulnLabMobile

The app supports a modular list of vulnerability demos via `assets/vulnerabilities.json`. Each entry maps an id/title to a fully-qualified Activity class name. `VulnerabilityListActivity` reads this file and launches activities dynamically.

## Steps to add a new module
1. Create a new Activity in `app/src/main/java/com/example/vulnlab/`, e.g., `BackupActivity.java`.
2. Add its layout to `app/src/main/res/layout/`.
3. Register Activity in `AndroidManifest.xml` with `<activity android:name=".BackupActivity"/>`.
4. Append an entry to `app/src/main/assets/vulnerabilities.json`:
```json
{
  "id": "insecure-backup",
  "title": "Insecure Backup",
  "activityClass": "com.example.vulnlab.BackupActivity"
}
```
5. Rebuild the app.

## Suggested modules to add later
- Insecure backup/export (world-readable files, improper storage)
- Broken object authorization (local only, mock data)

## Notes
- Keep all new vulnerabilities self-contained, with clear `// VULN: ...` markers.
- Do not add dangerous permissions unless absolutely necessary; prefer mock/local data.
- Maintain debug symbol availability for native demos.
