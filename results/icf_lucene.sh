#! /bin/bash

cd ..

rm -f lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.DocumentsWriter \
  --contract "setInfoStream setMaxFieldLength" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.store.MockRAMDirectory \
  --contract "failOn close" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.store.RAMFile \
  --contract "getLength setLength" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexReader \
  --contract "close deleteDocument" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "docCount maxDoc" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "docCount numDocs" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "maxDoc numDocs" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "maxDoc docCount" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "numDocs maxDoc" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "numDocs docCount" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.store.MockRAMDirectory \
  --contract "close failOn" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexReader \
  --contract "deleteDocument close" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexModifier \
  --contract "optimize close" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.store.RAMDirectory \
  --contract "createOutput openInput" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexReader \
  --contract "commit close" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexReader \
  --contract "close undeleteAll" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexReader \
  --contract "deleteDocument undeleteAll" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexReader \
  --contract "undeleteAll deleteDocument" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexReader \
  --contract "undeleteAll close" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexReader \
  --contract "reopen close" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "getNumBufferedDocuments getSegmentCount" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "getBufferedDeleteTermsSize getNumBufferedDeleteTerms" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.CompoundFileReader \
  --contract "openInput close" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.CompoundFileReader \
  --contract "close openInput" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.store.MockRAMDirectory \
  --contract "clearCrash crash" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "getSegmentCount docCount" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "getSegmentCount getDocCount" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "docCount getDocCount" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "getDocCount getSegmentCount" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "getDocCount docCount" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.IndexWriter \
  --contract "docCount getSegmentCount" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../lucene-4.6.1/build/demo/classes/java/:../lucene-4.6.1/build/core/classes/java/ \
  --module org.apache.lucene.index.SnapshotDeletionPolicy \
  --contract "release snapshot" \
  org.apache.lucene.demo.SearchFiles >> lucene_icf_comp
