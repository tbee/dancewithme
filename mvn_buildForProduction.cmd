if exist javaHome.cmd (
    call javaHome.cmd
)
call mvnw versions:set
call mvnw clean package -Pproduction -DskipTests
pause

rem run with: java -jar target\dancewithme-*-boot.jar
