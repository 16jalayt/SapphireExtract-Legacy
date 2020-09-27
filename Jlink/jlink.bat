@RD /S /Q "java runtime"
"F:\Program Files\Java\jdk-15.0.0.36-hotspot\bin\jlink" --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules ^
java.base,java.logging,jdk.unsupported,java.sql,java.desktop,java.xml,java.management ^
--output "java runtime"