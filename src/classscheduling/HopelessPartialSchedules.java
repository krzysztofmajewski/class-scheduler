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

    // TODO optimize
    private final ArrayList<BoardState> boardStates;

    public HopelessPartialSchedules(Schedule schedule) {
        this.schedule = schedule;
        boardStates = new ArrayList<>();
    }

    // TODO remove sanity checks
    void addThisPartialSchedule(int depth) throws SanityCheckException {
        BoardState bs = new BoardState(schedule.freeSlotList, depth);
        BoardState found = find(bs);
        if (found != null) {
            throw new SanityCheckException("Check this out.");
        }
        purgeSuperPatterns(bs);
        add(bs);
    }

    BoardState find(BoardState bs) throws SanityCheckException {
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
                    result = state;
                    break;
                }
            } else {
                // state.depth > bs.depth
                // we don't care if a superpattern is bad
                break;
            }
        }
        return result;
    }

    // Returns false if any one of these is in boardStates:
    //   this exact board state
    //   a board state that is a subpattern of this board state
    boolean vetThisMove(int depth) throws SanityCheckException {
        BoardState bs = new BoardState(schedule.freeSlotList, depth);
        return (find(bs) == null);
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
        numAdded++;
    }

    private void purgeSuperPatterns(BoardState bs) {
        Iterator<BoardState> iterator = boardStates.iterator();
        while (iterator.hasNext()) {
            BoardState state = iterator.next();
            if (state.depth > bs.depth) {
                if (bs.isSubPatternOf(state)) {
                    numPurged++;
                    iterator.remove();
                }
            }
        }
    }
}
