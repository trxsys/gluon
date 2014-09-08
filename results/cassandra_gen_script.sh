#! /bin/bash

cd ..

rm -f tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../cassandra-2.0.9/build/test/classes:../cassandra-2.0.9/build/classes/main:../cassandra-2.0.9/build/classes/stress \
    --module java.util.Map \
    --contract "remove isEmpty" \
    org.apache.cassandra.stress.StressServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../cassandra-2.0.9/build/test/classes:../cassandra-2.0.9/build/classes/main:../cassandra-2.0.9/build/classes/stress \
    --module org.apache.cassandra.db.compaction.LeveledManifest \
    --contract "remove add" \
    org.apache.cassandra.stress.StressServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../cassandra-2.0.9/build/test/classes:../cassandra-2.0.9/build/classes/main:../cassandra-2.0.9/build/classes/stress \
    --module java.util.Map \
    --contract "get put" \
    org.apache.cassandra.stress.StressServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../cassandra-2.0.9/build/test/classes:../cassandra-2.0.9/build/classes/main:../cassandra-2.0.9/build/classes/stress \
    --module java.util.concurrent.ExecutorService \
    --contract "shutdown awaitTermination" \
    org.apache.cassandra.stress.StressServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../cassandra-2.0.9/build/test/classes:../cassandra-2.0.9/build/classes/main:../cassandra-2.0.9/build/classes/stress \
    --module java.util.Iterator \
    --contract "hasNext next" \
    org.apache.cassandra.stress.StressServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../cassandra-2.0.9/build/test/classes:../cassandra-2.0.9/build/classes/main:../cassandra-2.0.9/build/classes/stress \
    --module java.net.InetAddress \
    --contract "equals getHostAddress" \
    org.apache.cassandra.stress.StressServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../cassandra-2.0.9/build/test/classes:../cassandra-2.0.9/build/classes/main:../cassandra-2.0.9/build/classes/stress \
    --module java.util.concurrent.atomic.AtomicReference \
    --contract "get set" \
    org.apache.cassandra.stress.StressServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../cassandra-2.0.9/build/test/classes:../cassandra-2.0.9/build/classes/main:../cassandra-2.0.9/build/classes/stress \
    --module java.util.Map \
    --contract "get get" \
    org.apache.cassandra.stress.StressServer >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../cassandra-2.0.9/build/test/classes:../cassandra-2.0.9/build/classes/main:../cassandra-2.0.9/build/classes/stress \
    --module org.apache.cassandra.net.MessagingService \
    --contract "isListening listen" \
    org.apache.cassandra.stress.StressServer >> tests_out
