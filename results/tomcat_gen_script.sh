#! /bin/bash

cd ..

rm -f tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module org.apache.catalina.deploy.ErrorPage \
    --contract "getErrorCode getErrorCode" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module java.util.HashMap \
    --contract "size values" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module org.apache.juli.logging.Log \
    --contract "isDebugEnabled debug" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module org.apache.catalina.util.LifecycleSupport \
    --contract "fireLifecycleEvent fireLifecycleEvent" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module java.util.Hashtable \
    --contract "get(X) put(X,_)" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module org.apache.catalina.core.StandardContext \
    --contract "getAnnotationProcessor getAnnotationProcessor" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module java.util.HashSet \
    --contract "size toArray" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module org.apache.catalina.deploy.ResourceBase \
    --contract "setNamingResources getName" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module java.util.HashMap \
    --contract "get(X) put(X,_)" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module java.util.ArrayList \
    --contract "size get" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module org.xml.sax.InputSource \
    --contract "setByteStream getSystemId" \
    org.apache.catalina.startup.Bootstrap >> tests_out

# ./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
#     --module java.util.HashMap \
#     --contract "keySet get size clear get put" \
#     org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module java.util.Enumeration \
    --contract "hasMoreElements nextElement" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module javax.management.MBeanServer \
    --contract "isRegistered(X) invoke(X,_,_,_)" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module java.util.ArrayList \
    --contract "size toArray" \
    org.apache.catalina.startup.Bootstrap >> tests_out

# ./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
#     --module java.util.TreeMap \
#     --contract "get put lastKey get remove" \
#     org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module org.apache.tomcat.util.digester.Digester \
    --contract "parse reset" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module org.apache.tomcat.util.descriptor.XmlErrorHandler \
    --contract "(getWarnings | getErrors) logFindings" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module java.util.HashSet \
    --contract "size contains" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module java.util.LinkedList \
    --contract "add poll" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module org.apache.juli.logging.Log \
    --contract "isInfoEnabled info" \
    org.apache.catalina.startup.Bootstrap >> tests_out

# ./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
#     --module java.lang.String \
#     --contract "endsWith length substring startsWith substring equals" \
#     org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module java.util.HashMap \
    --contract "size keySet" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module org.apache.catalina.Lifecycle \
    --contract "stop start" \
    org.apache.catalina.startup.Bootstrap >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module org.apache.catalina.core.ApplicationRequest \
    --contract "isSpecial getRequest" \
    org.apache.catalina.startup.Bootstrap >> tests_out

