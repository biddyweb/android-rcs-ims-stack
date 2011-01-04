@echo on

rem ----- Save Environment Variables ------------------------------------------

rem %JAVA_HOME%/bin should be in your path
rem %ANT_HOME%/bin should be in your path

rem ----- Verify and Set Required Environment Variables -----------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
:gotJavaHome

rem ----- Set Up The Runtime Classpath ----------------------------------------

set CLASSPATH=%ANT_HOME%\lib\ant.jar;%ANT_HOME%\lib\ant-launcher.jar;%JAVA_HOME%\lib\tools.jar


rem ----- Execute The Requested Build -----------------------------------------

cd .\core
"%JAVA_HOME%\bin\java" org.apache.tools.ant.Main -Dant.home="%ANT_HOME%" -Djava.home="%JAVA_HOME%" api


rem ----- Restore Environment Variables ---------------------------------------


:finish
pause