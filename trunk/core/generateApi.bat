@echo on

rem ----- Save Environment Variables ------------------------------------------

set ANT_HOME=%RCS_TERMINAL_VIEW_PATH_2%\..\tools\ant
set NCSS_HOME=%RCS_TERMINAL_VIEW_PATH_2%\..\tools\javancss
set XERCES_HOME=%ANT_HOME%\lib\xercesImpl.jar;

rem ----- Verify and Set Required Environment Variables -----------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
:gotJavaHome

rem ----- Set Up The Runtime Classpath ----------------------------------------

set CLASSPATH=%ANT_HOME%\lib\ant.jar;%ANT_HOME%\lib\ant-launcher.jar;%JAVA_HOME%\lib\tools.jar;%NCSS_HOME%\lib\javancss.jar;%NCSS_HOME%\lib\jhbasic.jar;%NCSS_HOME%\lib\ccl.jar


rem ----- Execute The Requested Build -----------------------------------------

cd %RCS_TERMINAL_VIEW_PATH_2%\core
"%JAVA_HOME%\bin\java" org.apache.tools.ant.Main -Dant.home="%ANT_HOME%" -Djava.home="%JAVA_HOME%" -Dxerces.home="%XERCES_HOME%" api


rem ----- Restore Environment Variables ---------------------------------------


:finish
pause