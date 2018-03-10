@echo on

rem if already elevated, will execute command
rem if elevation denied will return 0
powershell /c Start-Process -WindowStyle Hidden -Verb RunAs java -ArgumentList %COMMA_SEPARATED_ARGS_LIST%
exit /B %ERRORLEVEL%
