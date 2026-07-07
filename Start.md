1. Start Docker Desktop, then the backend stack

Docker Desktop isn't running right now. Start it (from the Start menu), wait for it to fully boot, then:
cd C:\Users\dibya\Documents\Boondi
docker compose up -d
This brings up PostgreSQL, Redis, MinIO, MailHog, the backend, and nginx.

2. Create an Android emulator (none exists yet)

The SDK has the system image (system-images;android-36.1;google_apis_playstore;x86_64) but no AVD has been created. Easiest path: open Android Studio → Device Manager → Create Device, pick a Pixel profile, and select that system image. (Or I can create one for you via avdmanager if you'd rather do it from the CLI — just say so.)

3. Build and install the app

Open android/ in Android Studio and let it sync first — that's the safest way to catch anything Gradle-CLI alone might miss, and it'll offer to fix the Gradle wrapper JDK association automatically. Then Run on the emulator/device.

If you prefer CLI:
cd C:\Users\dibya\Documents\Boondi\android
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
.\gradlew.bat :app:installDebug

4. Smoke-test the Sprint 8 flow

With the backend up and the app installed on the emulator: register → login → browse Home/Latest/Trending tabs → open a post → view a profile → edit profile → compose a post. The emulator reaches the backend at 10.0.2.2:8080 automatically (already wired into BuildConfig.BASE_URL).

Once you confirm that flow works end-to-end, we're clear to start Sprint 9.