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
        int index = find(bs);
        if (index >= 0) {
            boardStates.add(index, bs);
        }
    }

    int find(BoardState bs) throws SanityCheckException {
        boolean found = false;
        int index = 0;
        Iterator<BoardState> iterator = boardStates.iterator();
        while (iterator.hasNext()) {
            BoardState state = iterator.next();
            index++;
            if (state.depth < bs.depth) {
                if (state.isSubPatternOf(bs)) {
                    // existential question
                    throw new SanityCheckException("Why are we here?");
                }
            } else if (state.depth == bs.depth) {
                if (state.equals(bs)) {
                    // we already found this state once before
                    found = true;
                    state.hits++;
                    break;
                }
                // keep going
            } else {
                // state.depth > depth
                // remove up redundant entries
                if (bs.isSubPatternOf(state)) {
                    found = true;
                    iterator.remove();
                }
            }
        }
        if (found) {
            return index;
        } else {
            return -1;
        }
    }

    boolean vettingFailed(int depth) throws SanityCheckException {
        BoardState bs = new BoardState(schedule.freeSlotList, depth);
        return (find(bs) >= 0);
    }
}
