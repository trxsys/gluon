#! /bin/bash

cd ..

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
   --module java.util.Map \
   --contract "containsKey(X) (put(X,_) | get(X) | remove(X))" \
   org.apache.catalina.startup.Bootstrap > tomcat_map

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
   --module java.util.List \
   --contract "size (get | set | remove)" \
   org.apache.catalina.startup.Bootstrap > tomcat_list

./gluon.sh --timeout 45 -t -p -s -y -r --classpath ../tomcat/output/classes \
    --module java.util.Set \
    --contract "contains(X) (add(X) | remove(X))" \
    org.apache.catalina.startup.Bootstrap > tomcat_set
