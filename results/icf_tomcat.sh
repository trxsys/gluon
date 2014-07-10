#! /bin/bash

cd ..

rm -f tomcat_icf_use

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
  --module org.apache.tomcat.util.net.AprEndpoint$Poller \
  --contract "destroy destroy" \
  org.apache.catalina.startup.Bootstrap >> tomcat_icf_use

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
  --module org.apache.tomcat.util.net.NioEndpoint \
  --contract "recycleWorkerThread recycleWorkerThread" \
  org.apache.catalina.startup.Bootstrap >> tomcat_icf_use

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
  --module org.apache.catalina.tribes.group.GroupChannel \
  --contract "stop stop" \
  org.apache.catalina.startup.Bootstrap >> tomcat_icf_use

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
  --module org.apache.catalina.tribes.membership.Membership \
  --contract "removeMember memberAlive" \
  org.apache.catalina.startup.Bootstrap >> tomcat_icf_use

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
  --module org.apache.catalina.users.MemoryUserDatabase \
  --contract "findRole createRole" \
  org.apache.catalina.startup.Bootstrap >> tomcat_icf_use

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
  --module org.apache.catalina.core.StandardContext \
  --contract "findParameters findParameter" \
  org.apache.catalina.startup.Bootstrap >> tomcat_icf_use

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
  --module org.apache.catalina.authenticator.SingleSignOnEntry \
  --contract "removeSession findSessions" \
  org.apache.catalina.startup.Bootstrap >> tomcat_icf_use

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
  --module java.beans.PropertyDescriptor \
  --contract "getWriteMethod getWriteMethod" \
  org.apache.catalina.startup.Bootstrap >> tomcat_icf_use

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
  --module org.apache.jasper.compiler.SmapGenerator \
  --contract "addStratum getString" \
  org.apache.catalina.startup.Bootstrap >> tomcat_icf_use

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
  --module org.apache.jasper.compiler.SmapGenerator \
  --contract "setOutputFileName getString" \
  org.apache.catalina.startup.Bootstrap >> tomcat_icf_use
