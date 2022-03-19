echo off

REM Add the required jars to classpath
set CP=.\lib\derby.jar;.\lib\derbyclient.jar;.\lib\derbyshared.jar;.\dist\hla_path_builder.jar;

java -cp %CP% orbisoftware.hla_pathbuilder.HlaPathBuilder %*

