package test.validation.Coord04;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "resetX resetY;"
                   +"resetY resetX;")
class Coord {
    private int x, y;

    public Coord()
    {
        x=y=0;
    }

    @Atomic
    public void swap() {
        int oldX = x;
        x = y; // swap X
        y = oldX; // swap Y
    }

    @Atomic
    public void resetX(){
        x = 0;
    }

    @Atomic
    public void resetY(){
        y = 0;
    }
}
