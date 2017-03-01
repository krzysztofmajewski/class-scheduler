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

    // TODO optimize
    private final ArrayList<BoardState> boardStates;

    public HopelessPartialSchedules(Schedule schedule) {
        this.schedule = schedule;
        boardStates = new ArrayList<>();
    }

    // TODO remove sanity checks
    void addThisPartialSchedule(int depth) throws SanityCheckException {
        BoardState bs = new BoardState(schedule.freeSlotList, depth);
        BoardState bsOrSuperPatternThereof = findAndPurgeSuperPatternsIfAny(bs);
        if ((bsOrSuperPatternThereof == null) || (bsOrSuperPatternThereof.depth > bs.depth)) {
            add(bs);
        }
    }

    BoardState findAndPurgeSuperPatternsIfAny(BoardState bs) throws SanityCheckException {
        BoardState result = null;
        Iterator<BoardState> iterator = boardStates.iterator();
        while (iterator.hasNext()) {
            BoardState state = iterator.next();
            if (state.depth < bs.depth) {
                if (state.isSubPatternOf(bs)) {
                    state.hits++;
                    result = state;
                    break;
                }
            } else if (state.depth == bs.depth) {
                if (state.equals(bs)) {
                    // we already found this state once before
                    state.hits++;
                    result = bs;
                    break;
                }
                // keep going
            } else {
                // state.depth > depth
                // remove redundant entries
                if (bs.isSubPatternOf(state)) {
                    bs.hits++;
                    result = state;
                    iterator.remove();
                }
            }
        }
        return result;
    }

    boolean vettingFailed(int depth) throws SanityCheckException {
        BoardState bs = new BoardState(schedule.freeSlotList, depth);
        return (findAndPurgeSuperPatternsIfAny(bs) != null);
    }

    private void add(BoardState bs) {
        if (boardStates.size() >= MAX_ENTRIES) {
            return;
        }
        int index = 0;
        for (BoardState state : boardStates) {
            index++;
            if (state.depth >= bs.depth) {
                break;
            }
        }
        boardStates.add(index, bs);
    }
}
