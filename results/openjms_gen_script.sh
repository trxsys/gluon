#! /bin/bash

cd ..

rm -f tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module java.io.DataOutputStream \
    --contract "(writeByte|writeInt) flush" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module java.sql.PreparedStatement \
    --contract "setString executeUpdate" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module java.util.Map \
    --contract "containsKey put" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module org.exolab.jms.common.util.OrderedQueue \
    --contract "firstElement removeFirstElement" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module java.util.HashMap \
    --contract "containsKey put" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module java.io.DataOutputStream \
    --contract "(writeLong|write) flush" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module java.util.HashMap \
    --contract "get remove" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module org.exolab.jms.persistence.Destinations \
    --contract "get getId" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module java.util.Map \
    --contract "put put" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module org.exolab.jms.messagemgr.MessageRef \
    --contract "getMessageId isPersistent" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module org.exolab.jms.messagemgr.ResourceManager \
    --contract "getTransactionRecords logTransactionState" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module org.exolab.jms.server.SessionConsumer \
    --contract "stop start" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module org.exolab.jms.client.JmsConnection \
    --contract "ensureOpen setModified" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module java.util.HashMap \
    --contract "values clear" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module java.lang.Object \
    --contract "wait wait" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module java.util.AbstractCollection \
    --contract "isEmpty removeFirst" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module org.apache.commons.logging.Log \
    --contract "isDebugEnabled debug" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module java.util.LinkedList \
    --contract "contains add" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module org.exolab.jms.persistence.DatabaseService \
    --contract "getAdapter getConnection" \
    org.exolab.jms.server.JmsServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../openjms/bin \
    --module org.exolab.jms.persistence.DatabaseService \
    --contract "begin commit" \
    org.exolab.jms.server.JmsServer >> tests_out
