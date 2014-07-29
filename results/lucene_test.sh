#! /bin/bash

cd ..

# this is just to test performance
./gluon.sh --timeout 5 -t -p -s -y -r --classpath \
    ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module java.util.Map \
    --contract "containsKey(X) put(X,_)" \
     org.apache.lucene.demo.SearchFiles > lucene_map


#./gluon.sh --timeout 45 -t -p -s -y -r --classpath \
#    ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
#    --module java.util.Map \
#    --contract "containsKey(X) (put(X,_) | get(X) | remove(X))" \
#     org.apache.lucene.demo.SearchFiles > lucene_map

#./gluon.sh --timeout 45 -t -p -s -y -r --classpath \
#    ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
#    --module java.util.ArrayList \
#    --contract "size (get | set | remove)" \
#    org.apache.lucene.demo.SearchFiles > lucene_list

#./gluon.sh --timeout 45 -t -p -s -y -r --classpath \
#    ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
#    --module java.util.Set \
#    --contract "contains(X) (add(X) | remove(X))" \
#    org.apache.lucene.demo.SearchFiles > lucene_set

exit

./gluon.sh --timeout 5 -t -p -s -y -r --classpath \
    ../openjms/bin \
    --module java.util.Map \
    --contract "containsKey put" \
    org.exolab.jms.server.JmsServer
