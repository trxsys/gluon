# Contract generation

The generation of contracts is done by a piece of code on branch `tmp-mk-contract`.  As the name suggests this branch
was supposed to be a quick-and-dirty way of generating contracts and in the future we would have a proper implementation
for that.  That never happened, so the process of generating contracts is very ad-hoc.

In branch `tmp-mk-contract` gluon only knows how to generate contracts.  To actually run the test you need to get
back to `master`.  The way that the contract generation works is as follows: in this branch I modified the
`MonitorAnalysis` to check for `synchronized` blocks that contain sequences of calls to some type.
If the same sequence appears two or more times in synchronized blocks then we consider that it might be something that
should always be done atomically (the entire sequence), and we output a contract for it.

The way the contracts are outputed is... well... not amazing.  Basically gluon will output a shell script to `STDERR`.
That shell script will call gluon to verify the contract.

To give a step-by-step example, I modified one of the validation tests to use `synchronized` blocks (the `AccountTest`
test).  You [can see](https://github.com/trxsys/gluon/blob/f2ab14db14b829c6afe6795a295e847b4354af86/test/validation/AccountTest/Update.java#L6)
that the test now has two `synchronized` blocks were it calls `account.getBalance()` and then `account.setBalance()`
(in `Update.update()` and `Update.update2()`).  

Lets run gluon and see that it actually generates the contract.  Make sure you are in branch `tmp-mk-contract`.  Also,
you need to have `sbt` (a build system) installed.  To generate the contract for this test do

```text
$ ./gluon.sh --synch --classpath test/target/classes --module test.validation.AccountTest.Account test.validation.AccountTest.Main 2> script.sh
[...]
$ cat script.sh
#! /bin/bash

cd ..

rm -f tests_out

./gluon.sh --timeout 5 -t -p -s -y -r --classpath ../cassandra-2.0.9/build/test/classes:../cassandra-2.0.9/build/classes/main:../cassandra-2.0.9/build/classes/stress \
    --module test.validation.AccountTest.Account \
    --contract "getBalance setBalance" \
    org.apache.cassandra.stress.StressServer >> tests_out
```

Note that in this case the script only contains one contract, but you can have multiple ones (see [here](https://github.com/trxsys/gluon/blob/f2ab14db14b829c6afe6795a295e847b4354af86/results/cassandra_gen_script.sh)).  You will also
notice that the script gets the module (`test.validation.AccountTest.Account`) and contract (`getBalance setBalance`)
right, but it has a the classpath and main class that refers to cassandra.  That's because this is hardcoded [here](https://github.com/trxsys/gluon/blob/f2ab14db14b829c6afe6795a295e847b4354af86/src/main/java/gluon/analysis/monitor/MonitorAnalysis.java#L206).
You basically have to change this for each project that you want to generate contracts for.  I know... less than ideal.

As for the other flags passed to gluon, here's a brief explanation:

* `-s` - Class scope analysis.  This means that gluon will not see sequences of calls that cross class boundaries.
    Doing the whole programme analysis is just too expensive.
* `--timeout <minutes>` - Even when doing class scope analysis it might be too costly to analyse the class.  For that
    reason there is this timeout for each class.
* `-t`/`-p` - Output profiling information.
* `-y` - Uses `synchronized` to identify atomic regions instead of the `@Atomic` annotation.
* `-r` - Uses a conservative notion of points-to analysis.  This is usually necessary when the program under analysis
    dynamically loads classes, and therefore the points-to information would be incomplete.

To see other flags just run `./gluon.sh -h`.
