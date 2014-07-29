#! /bin/bash

cd ..

rm -f tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.index.TermsEnum \
    --contract "seekExact docs" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.util.InfoStream \
    --contract "isEnabled message" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module java.util.Map \
    --contract "get put" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.index.ConcurrentMergeScheduler \
    --contract "updateMergeThreads notifyAll" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.store.Directory \
    --contract "deleteFile deleteFile" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module java.util.Iterator \
    --contract "hasNext next" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.index.IndexWriter \
    --contract "checkpoint changed" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.index.SegmentInfo \
    --contract "getDocCount getDocCount" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module java.util.List \
    --contract "size get" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module java.util.HashSet \
    --contract "contains remove" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.index.IndexWriter \
    --contract "ensureOpen checkpoint" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.index.IndexFileDeleter \
    --contract "deleteFile deleteNewFiles" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.index.BufferedUpdatesStream \
    --contract "applyDeletesAndUpdates prune" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.index.DocumentsWriterFlushControl \
    --contract "abortPendingFlushes waitForFlush" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.index.DocumentsWriter \
    --contract "flushAllThreads finishFullFlush" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module java.util.concurrent.locks.ReentrantLock \
    --contract "lock unlock" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.index.DocumentsWriterPerThreadPool \
    --contract "getMaxThreadStates getThreadState" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module org.apache.lucene.index.ReadersAndUpdates \
    --contract "initWritableLiveDocs delete getPendingDeleteCount" \
    org.apache.lucene.demo.SearchFiles >> tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
    --module java.util.concurrent.atomic.AtomicLong \
    --contract "addAndGet get" \
    org.apache.lucene.demo.SearchFiles >> tests_out
