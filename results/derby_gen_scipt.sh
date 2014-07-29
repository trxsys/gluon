#! /bin/bash

cd ..

rm -f tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module org.apache.derby.impl.jdbc.EmbedConnection \
    --contract "setupContextStack getLanguageConnection restoreContextStack" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module org.apache.derby.impl.jdbc.EmbedResultSet \
    --contract "setupContextStack restoreContextStack" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module org.apache.derby.impl.jdbc.LOBStreamControl \
    --contract "write getLength read truncate" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module org.apache.derby.impl.jdbc.ConnectionChild \
    --contract "setupContextStack getEmbedConnection restoreContextStack" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module org.apache.derby.iapi.sql.PreparedStatement \
    --contract "getActivation executeSubStatement" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module java.util.Vector \
    --contract "iterator clear" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module org.apache.derby.iapi.services.io.Storable \
    --contract "isNull hasStream" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module org.apache.derby.iapi.sql.ResultDescription \
    --contract "getColumnCount getColumnDescriptor" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module java.util.ArrayList \
    --contract "isEmpty remove" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module java.util.Vector \
    --contract "size get" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module org.apache.derby.iapi.sql.conn.StatementContext \
    --contract "getActivation setActivation" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module org.apache.derby.impl.services.monitor.BaseMonitor \
    --contract "report report" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module java.util.Iterator \
    --contract "hasNext next" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module org.apache.derby.impl.jdbc.ConnectionChild \
    --contract "getEmbedConnection setupContextStack" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module org.apache.derby.iapi.sql.conn.LanguageConnectionContext \
    --contract "getStatementContext pushStatementContext" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out


./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../db-derby-10.10.2.0-src/classes \
    --module org.apache.derby.iapi.sql.conn.LanguageConnectionContext \
    --contract "prepareInternalStatement popStatementContext" \
    org.apache.derbyTesting.functionTests.harness.RunTest >> tests_out
