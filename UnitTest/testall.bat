@ECHO OFF
REM 
REM a batch file to run all the junit tests.  The folder
REM containing java.exe and javac.exe must be in your PATH.
REM 
REM  @author Andrew Nuxoll


SET CP=".;..;./junit;./junit/junit-4.10.jar;junit-4.10.jar;"

REM Compile the JUnit tests
javac -cp %CP% *.java
if ERRORLEVEL 0 GOTO tests
echo "COMPILE FAILED.  STOPPING"
GOTO end

REM Run the JUnit tests
:tests

SET TESTNAME=WMETest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=WMESetTest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=ElementalEpisodeTest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=ActionTest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=ActionWithSequenceEpisodesTest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=SequenceEpisodeTest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=SequenceTest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=SequenceWithSequenceEpisodesTest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=FlipSystemEnvironmentTest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=ReplacementTest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=RouteTest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=PlanTest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=ZiggBasicTest
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end

SET TESTNAME=ZiggSimpleRoombaTest 
echo BEGIN TEST: %TESTNAME%
java -cp %CP% org.junit.runner.JUnitCore UnitTest.%TESTNAME%
if NOT ERRORLEVEL 0 GOTO end



:end
SET CP=
SET TESTNAME=
