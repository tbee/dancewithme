if exist javaHome.cmd (
    call javaHome.cmd
)

for %%f in (target\dancewithme*-boot.jar) do (
	call %JAVA_HOME%\bin\java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar %%f
)
pause
