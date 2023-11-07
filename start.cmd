echo off

REM Add the required jars to classpath
set CP=.\lib\commons-lang3-3.0.jar;.\commons-text-1.10.0.jar;.\lib\derby.jar;.\lib\derbyclient.jar;.\lib\derbyshared.jar;.\dist\hla_path_builder.jar;

java -cp %CP% orbisoftware.hla_pathbuilder.HlaPathBuilder %*

