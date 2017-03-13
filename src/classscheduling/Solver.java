/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Course.FRENCH;

/**
 *
 * @author krzys
 */
// TODO: review throws clauses
public class Solver {

    public static final String VERSION = "v4";

    private static final boolean DEBUG = false;

    final State state;

    int largestNumberOfSlotsFilled = 0;
    int smallestNumberOfSlotsFilledWhenBacktracking = 60;
    long movesSeenInThisGame = 0;
    long movesFailedVetting = 0;

    int[] samplesAtDepth;

    HopelessPartialSchedules hopelessPartialSchedules;

    final ScheduleValidator validator;

    public Solver(State state) {
        this.state = state;
        validator = new ScheduleValidator(state);
        samplesAtDepth = new int[60];
        hopelessPartialSchedules = new HopelessPartialSchedules();
    }

    boolean solve() throws SanityCheckException {
        validator.reset();
        validator.validateCorrectnessConstraints();
        if (validator.hasErrors()) {
            return false;
        }
        MovesIterator iterator = new MovesIterator(state);
        return scheduleCourses(iterator, null);
    }

    // returns true if it finds a solution
    boolean scheduleCourses(MovesIterator iterator, Move lastMove) throws SanityCheckException {
        // lastMove is only null the first time we call this method
        if ((lastMove != null) && !satisfiesCorrectnessConstraints(lastMove)) {
            lastMove.isIllegalMove = true;
            return false;
        }
//        if (!vetThisPartialSchedule()) {
//            movesFailedVetting++;
//            if (DEBUG) {
//                System.out.println("Partial schedule failed vetting:");
//                state.print();
//            }
//            return false;
//        }
        if (state.depth == 60) {
            return validator.validate();
        }
        // lastMove is only null the first time we call this method
        if ((lastMove != null) && (state.depth > largestNumberOfSlotsFilled)) {
            largestNumberOfSlotsFilled = state.depth;
            System.out.println("Best move so far: " + lastMove.slot);
            System.out.println((60 - state.depth) + " free slots left");
            state.print();
            System.out.println();
        }
        while (iterator.notDone()) {
            Move nextMove = move(iterator);
            movesSeenInThisGame++;
            if (DEBUG) {
                System.out.println("Moved: " + nextMove.slot + " : " + nextMove.course);
            }
            MovesIterator subproblemIterator = new MovesIterator(iterator);
            boolean hasSolution = scheduleCourses(subproblemIterator, nextMove);
            if (hasSolution) {
                return true;
            }
            // don't pollute hopelessPartialSchedules with illegal moves
//            if (!nextMove.isIllegalMove) {
//                markThisPartialScheduleAsHopeless();
//                if (DEBUG) {
//                    System.out.println("Marked partial schedule as hopeless:");
//                    state.print();
//                }
//            }
            retreat(nextMove);
        }
        // tried all possible moves, did not find solution
        printInfoIfNeeded();
        return false;
    }

    // TODO: remove sanity check
    Move move(MovesIterator iterator) throws SanityCheckException {
        Move move = iterator.getNextMove();
        state.setCourse(move);
        return move;
    }

    void retreat(Move move) throws SanityCheckException {
        clear(move);
        if (DEBUG) {
            System.out.println("Retreated from move: " + move.slot + " : " + move.course);
        }
    }

    void printInfoIfNeeded() throws SanityCheckException {
        if (state.depth < smallestNumberOfSlotsFilledWhenBacktracking) {
            smallestNumberOfSlotsFilledWhenBacktracking = state.depth;
            System.out.println("This is the most free slots we've seen to date while retreating, printing stats...");
            printStats();
            state.print();
        }
    }

    void printStats() {
        System.out.println(movesSeenInThisGame + " moves seen in this game");
        System.out.println(movesFailedVetting + " moves failed vetting in this game");
        System.out.println(hopelessPartialSchedules.numAdded
                + " partial schedules added to lookup table");
        System.out.println(hopelessPartialSchedules.numPurged
                + " partial schedules purged from lookup table");
        System.out.println(hopelessPartialSchedules.numOverflowed
                + " partial schedules exceeded lookup table capacity");
        System.out.println(hopelessPartialSchedules.numElements
                + " partial schedules remaining in lookup table");
        System.out.println(hopelessPartialSchedules.maxElements
                + " partial schedules in lookup table at its fullest");
    }

    // TODO: remove sanity checks
    void clear(Move move) throws SanityCheckException {
        char c = state.getCourse(move);
        if (c != move.course.code) {
            throw new SanityCheckException(move.slot + ": expected " + move.course + " but found " + c);
        }
        state.clearCourse(move.slot);
    }

    // Returns false if any one of these is in boardStates:
    //   this exact board state
    //   a board state that is a subpattern of this board state
    boolean vetThisPartialSchedule() {
        return (hopelessPartialSchedules.findThisPatternOrSubPatternThereof(state) == null);
    }

    void markThisPartialScheduleAsHopeless() {
        hopelessPartialSchedules.markThisPartialScheduleAsHopeless(state);
    }

    private boolean satisfiesCorrectnessConstraints(Move move) throws SanityCheckException {
        validator.reset();
        validator.validateCorrectnessConstraints(move);
        return !validator.hasErrors();
    }

}
