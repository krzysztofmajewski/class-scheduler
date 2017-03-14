/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author krzys
 */
public class Solutions {

    static int MAX_ENTRIES = 10000;

    long numAdded;
    long numOverflowed;

    int numElements;

    private final List<State> boardStates;

    public Solutions() {
        boardStates = new ArrayList<>();
    }

    Boolean addIfNotDuplicate(State state) {
        // make a copy of this state, because this state will change!
        State bs = new State(state);
        if (numElements >= MAX_ENTRIES) {
            numOverflowed++;
            return null;
        }
        if (!boardStates.contains(bs)) {
            // maybe duplicates are temporally close together
            // then we don't have to search to the end of the list to find them
            boardStates.add(0, bs);
            numAdded++;
            numElements++;
            return true;
        }
        return false;
    }

}
