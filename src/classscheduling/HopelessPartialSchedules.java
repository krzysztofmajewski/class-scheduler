/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author krzys
 */
public class HopelessPartialSchedules {

    private static final int MAX_ENTRIES = 1000;

    long numAdded;
    long numPurged;
    long numOverflowed;

    int numElements;
    int maxElements;

    private final Object boardStates[];

    public HopelessPartialSchedules() {
        // depth 1..60
        boardStates = new Object[61];
    }

    void markThisPartialScheduleAsHopeless(State state) {
        // make a copy of this state, because this state will change!
        State bs = new State(state);
        purgeSuperPatterns(bs);
        add(bs);
    }

    State findThisPatternOrSubPatternThereof(State bs) {
        State result = null;
        for (int depthIndex = 1; depthIndex <= bs.depth; depthIndex++) {
            if (result != null) {
                break;
            }
            ArrayList<State> depthList = (ArrayList<State>) boardStates[depthIndex];
            if ((depthList != null) && (!depthList.isEmpty())) {
                if (depthIndex < bs.depth) {
                    // check for subpatterns of bs
                    for (State state : depthList) {
                        if (state.isSubPatternOf(bs)) {
                            state.hits++;
                            result = state;
                            break;
                        }
                    }
                } else if (depthIndex == bs.depth) {
                    // check if bs already in here
                    for (State state : depthList) {
                        if (state.equals(bs)) {
                            // we already found this state once before
                            state.hits++;
                            result = state;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    void purgeSuperPatterns(State bs) {
        for (int depthIndex = bs.depth + 1; depthIndex <= 60; depthIndex++) {
            ArrayList<State> depthList = (ArrayList<State>) boardStates[depthIndex];
            if ((depthList == null) || (depthList.isEmpty())) {
                continue;
            }
            Iterator<State> iterator = depthList.iterator();
            while (iterator.hasNext()) {
                State state = iterator.next();
                if (bs.isSubPatternOf(state)) {
                    iterator.remove();
                    numPurged++;
                    numElements--;
                }
            }
        }
    }

    void add(State bs) {
        // TODO: does this ever happen? should we shake things up when it does?
        if (numElements >= MAX_ENTRIES) {
            numOverflowed++;
            return;
        }
        ArrayList<State> depthList = (ArrayList<State>) boardStates[bs.depth];
        if (depthList == null) {
            // TODO optimize initial capacity?
            depthList = new ArrayList<>();
            boardStates[bs.depth] = depthList;
        }
        depthList.add(bs);
        numAdded++;
        numElements++;
        if (numElements > maxElements) {
            maxElements = numElements;
        }
    }

}
