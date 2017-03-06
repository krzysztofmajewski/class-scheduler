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

    private final Schedule schedule;

    int numAdded;
    int numPurged;
    int numElements;

    private final Object boardStates[];

    public HopelessPartialSchedules(Schedule schedule) {
        this.schedule = schedule;
        // depth 1..60
        boardStates = new Object[61];
    }

    void addThisPartialSchedule(int depth) {
        BoardState bs = new BoardState(schedule.freeSlotList, depth);
        purgeSuperPatterns(bs);
        add(bs);
    }

    BoardState find(BoardState bs) {
        BoardState result = null;
        for (int depthIndex=1; depthIndex <= bs.depth; depthIndex++) {
            if (result != null) {
                break;
            }
            ArrayList<BoardState> depthList = (ArrayList<BoardState>) boardStates[depthIndex];
            if ((depthList != null) && (!depthList.isEmpty())) {
                if (depthIndex < bs.depth) {
                    // check for subpatterns of bs
                    for (BoardState state : depthList) {
                        if (state.isSubPatternOf(bs)) {
                            state.hits++;
                            result = state;
                            break;
                        }
                    }
                } else if (depthIndex == bs.depth) {
                    // check if bs already in here
                    for (BoardState state : depthList) {
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

    // Returns false if any one of these is in boardStates:
    //   this exact board state
    //   a board state that is a subpattern of this board state
    boolean vetThisMove(int depth) {
        BoardState bs = new BoardState(schedule.freeSlotList, depth);
        return (find(bs) == null);
    }

    private void add(BoardState bs) {
        // TODO: does this ever happen? should we shake things up when it does?
        if (numElements >= MAX_ENTRIES) {
            return;
        }
        ArrayList<BoardState> depthList = (ArrayList<BoardState>) boardStates[bs.depth];
        if (depthList == null) {
            // TODO optimize initial capacity?
            depthList = new ArrayList<>();
            boardStates[bs.depth] = depthList;
        }
        depthList.add(bs);
        numAdded++;
        numElements++;
    }

    private void purgeSuperPatterns(BoardState bs) {
        for (int depthIndex = bs.depth + 1; depthIndex <= 60; depthIndex++) {
            ArrayList<BoardState> depthList = (ArrayList<BoardState>) boardStates[depthIndex];
            if ((depthList == null) || (depthList.isEmpty())) {
                continue;
            }
            Iterator<BoardState> iterator = depthList.iterator();
            while (iterator.hasNext()) {
                BoardState state = iterator.next();
                if (bs.isSubPatternOf(state)) {
                    iterator.remove();
                    numPurged++;
                    numElements--;
                }
            }
        }
    }
}
