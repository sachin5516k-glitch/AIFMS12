set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Ensure we are in the correct directory (frontend)
if not exist "app" (
    cd frontend
)

if exist "app\franchise-release-key.jks" (
    echo Keystore already exists. Skipping generation.
) else (
    "%JAVA_HOME%\bin\keytool.exe" -genkey -v -keystore app\franchise-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias franchisekey -storepass Franchise@123 -keypass Franchise@123 -dname "CN=AI Franchise, OU=Engineering, O=AIFranchise, L=City, S=State, C=US"
    echo Keystore generated at app\franchise-release-key.jks
)
