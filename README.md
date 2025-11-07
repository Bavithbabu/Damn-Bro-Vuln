# VulnLabMobile

⚠️ Educational project for mobile forensics and reverse engineering. Use ONLY in isolated lab environments (emulator or sacrificial test device). Do NOT run on production devices or networks.

## Overview
Single-APK Android app showcasing two intentionally vulnerable modules for static/dynamic analysis:
- Insecure Crypto (AES-ECB, hardcoded key, predictable SecureRandom)
- Unsafe JNI (stack buffer overflow via `strcpy`)

Java app layer, C NDK native layer with symbols kept for easier reversing.

## Build & Run
- Build: `./gradlew assembleDebug`
- Install: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- Package: `app/build/outputs/apk/debug/app-debug.apk`

Target package: `com.example.vulnlab`

## App Navigation
- Main screen: two buttons
  - Insecure Crypto Demo → `CryptoActivity`
  - Unsafe JNI Demo → `NativeActivity`
- Modular list also included: `VulnerabilityListActivity` reads `assets/vulnerabilities.json` and launches activities (for easy extension later).

## Reproduce: Crypto PoC
1. Launch app → Insecure Crypto Demo.
2. Tap "Encrypt & Save" (default plaintext `TopSecret123`).
3. View stored ciphertext in SharedPreferences:
   ```bash
   adb shell run-as com.example.vulnlab cat /data/data/com.example.vulnlab/shared_prefs/lab_prefs.xml
   ```
4. Open APK in jadx (or similar) and inspect `com/example/vulnlab/CryptoUtils.java`.
   - Find `KEY_BYTES` (hardcoded 16-byte AES key) // VULN
   - See `Cipher.getInstance("AES/ECB/PKCS5Padding")` // VULN
   - Note `new SecureRandom(new byte[]{1,2,3})` // VULN

### Decrypt stored ciphertext (Demo)
- Java snippet:
```java
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

byte[] KEY_BYTES = new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
String b64 = "<paste from SharedPreferences>";
SecretKeySpec key = new SecretKeySpec(KEY_BYTES, "AES");
Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
c.init(Cipher.DECRYPT_MODE, key);
String pt = new String(c.doFinal(Base64.getDecoder().decode(b64)), "UTF-8");
System.out.println(pt);
```
- Python snippet:
```python
from base64 import b64decode
from Crypto.Cipher import AES

KEY = bytes([1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16])
ct = b64decode("<paste from SharedPreferences>")
print(AES.new(KEY, AES.MODE_ECB).decrypt(ct).decode('utf-8'))
```

## Reproduce: JNI PoC (Crash)
1. Launch app → Unsafe JNI Demo.
2. Enter a very long input (e.g., 1024 'A's) and tap Process.
3. Observe app crash due to native stack overflow.
4. Capture logs:
   ```bash
   adb logcat | grep vuln_native -n
   ```

### Reverse Engineering Native
- Extract `.so` from APK:
  ```bash
  unzip -l app/build/outputs/apk/debug/app-debug.apk | grep libvuln
  unzip -p app/build/outputs/apk/debug/app-debug.apk lib/<abi>/libvuln.so > libvuln.so
  ```
- Inspect symbols/strings:
  ```bash
  strings libvuln.so | head -n 50
  readelf -s libvuln.so | grep Java_com_example_vulnlab_NativeActivity_processInput
  ```

### Reverse Engineering Java
- Use jadx-gui to open APK. Navigate to:
  - `com/example/vulnlab/CryptoUtils.java`
  - `com/example/vulnlab/CryptoUtilsFixed.java` (secure reference, not used)
  - `com/example/vulnlab/CryptoActivity.java`
  - `app/src/main/cpp/vuln_native.c`

## Remediation Notes
- Crypto:
  - Replace AES/ECB with AES/GCM.
  - Use Android Keystore to protect keys; no hardcoded keys.
  - Use `SecureRandom()` without fixed seed; derive keys with PBKDF2.
  - See `CryptoUtilsFixed.java` for example.
- JNI:
  - Avoid `strcpy`. Use length-checked APIs (e.g., `snprintf`, `strlcpy`) or allocate exact buffers.
  - Validate inputs in Java before passing to JNI.

## Ethical Notice
This app is intentionally vulnerable and should only be used in isolated lab environments (emulator or test device). Do not connect to production services or networks while running the app.
