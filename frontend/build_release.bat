@echo off
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "GRADLE_HOME=C:\Users\sachin\.gradle\wrapper\dists\gradle-8.5-bin\5t9huq95ubn472n8rpzujfbqh\gradle-8.5"
set "PATH=%GRADLE_HOME%\bin;%JAVA_HOME%\bin;%PATH%"

echo Fixing Gradle Wrapper...
call gradle wrapper --gradle-version 8.5

echo Building Release APK...
call gradle assembleRelease -Dorg.gradle.jvmargs="-Xmx2048m --add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED" -Pkapt.use.worker.api=false
