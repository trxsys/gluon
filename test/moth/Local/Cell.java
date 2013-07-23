package test.moth.Local;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "getValue setValue;"
          +"setValue getValue;")
class Cell {
    private int n = 0;
    
    @Atomic
    int getValue() {
        return n;
    }
    @Atomic
    void setValue(int x) {
        n = x;
    }
}
