#!/bin/bash
SCRIPT_PATH="${BASH_SOURCE[0]}";
if([ -h "${SCRIPT_PATH}" ]) then
  while([ -h "${SCRIPT_PATH}" ]) do SCRIPT_PATH=`readlink "${SCRIPT_PATH}"`; done
fi
pushd . > /dev/null
cd "`dirname "${SCRIPT_PATH}"`" > /dev/null
SCRIPT_PATH=`pwd`;
popd  > /dev/null

script_dir=`cygpath -w "${SCRIPT_PATH}" || echo "${SCRIPT_PATH}"`

#stty -icanon min 1 -echo > /dev/null 2>&1
#java -Xmx512M -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -Dfile.encoding=utf-8 $SBT_OPTS -jar "$script_dir/sbt-launcher/xsbt-launch.jar" "$@"
#java -Xmx512M -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m $SBT_OPTS -jar "$script_dir/sbt-launcher/xsbt-launch.jar" "$@"
#java -Xmx512M -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -Dfile.encoding=utf-8 $SBT_OPTS -jar "$script_dir/sbt-launcher/xsbt-launch.jar" "$@"
#java -Xmx512M -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -Dfile.encoding=utf-8 -Dinput.encoding=cpCp2151 $SBT_OPTS -jar "$script_dir/sbt-launcher/xsbt-launch.jar" "$@"
java -Xmx512M -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -Dfile.encoding=utf-8 -Dinput.encoding=cp1252 $SBT_OPTS -jar "$script_dir/sbt-launcher/xsbt-launch.jar" "$@"
#java -Xmx512M -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -Dfile.encoding=utf-8 -Dinput.encoding=iso-8859-1 $SBT_OPTS -jar "$script_dir/sbt-launcher/xsbt-launch.jar" "$@"
#java -Xmx512M -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -Dfile.encoding=iso-8859-1 $SBT_OPTS -jar "$script_dir/sbt-launcher/xsbt-launch.jar" "$@"
#java -Xmx512M -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -Dfile.encoding=utf-8 -Djline.terminal=jline.UnixTerminal -Dsbt.cygwin=true $SBT_OPTS -jar "$script_dir/sbt-launcher/xsbt-launch.jar" "$@"
#stty icanon echo > /dev/null 2>&1