#!/usr/bin/env bash

JAVACMD=${JAVACMD:=./graalvm/bin/java}

PROGRAM_ARGS=""
JAVA_ARGS=" -Dgraal.MaximumEscapeAnalysisArrayLength=128 -Dgraal.TruffleOSRCompilationThreshold=1000 -Dgraal.TruffleCompilationThreshold=2 -Dgraal.TruffleMinInvokeThreshold=2 "

for opt in "$@"
do
  case $opt in
    -trace)
      JAVA_ARGS="$JAVA_ARGS -Dgraal.TraceTruffleCompilation=true" ;;
    -debug)
      JAVA_ARGS="$JAVA_ARGS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=y" ;;
    -dump)
      JAVA_ARGS="$JAVA_ARGS -Dgraal.Dump= -Dgraal.MethodFilter=Truffle.* -Dgraal.TruffleBackgroundCompilation=false -Dgraal.TraceTruffleCompilation=true -Dgraal.TraceTruffleCompilationDetails=true" ;;
    -disassemble)
      JAVA_ARGS="$JAVA_ARGS -XX:CompileCommand=print,*OptimizedCallTarget.callRoot -XX:CompileCommand=exclude,*OptimizedCallTarget.callRoot -Dgraal.TruffleBackgroundCompilation=false -Dgraal.TraceTruffleCompilation=true -Dgraal.TraceTruffleCompilationDetails=true" ;;
    -J*)
      opt=${opt:2}
      JAVA_ARGS="$JAVA_ARGS $opt" ;;
    *)
      PROGRAM_ARGS="$PROGRAM_ARGS $opt" ;;
  esac
done

#echo "$JAVACMD $JAVA_ARGS -polyglot -cp ./target/classes com.oracle.truffle.bf.BFMain $PROGRAM_ARGS"
$JAVACMD $JAVA_ARGS -polyglot -cp ./target/classes com.oracle.truffle.bf.BFMain $PROGRAM_ARGS
