/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Course.FRENCH;
import java.text.DecimalFormat;
import java.text.NumberFormat;

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
    int solutionsFound = 0;
    int duplicateSolutionsFound = 0;

    long movesSeenInThisGame = 0;

    Solutions solutions;

    final ScheduleValidator validator;

    public Solver(State state) {
        this.state = state;
        validator = new ScheduleValidator(state);
        solutions = new Solutions();
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
                Boolean added = solutions.addIfNotDuplicate(state);
                if (added == null) {
                    System.out.println("Solutions table overflowed");
                    System.out.println("Potentially unique solution:");
                    state.print();
                    printStats();
                } else if (added) {
                    solutionsFound++;
                    System.out.println("Found solution # " + solutionsFound + "!");
                    state.print();
                    printStats();
                } else {
                    System.out.println("Found a duplicate solution");
                    state.print();
                    printStats();
                    duplicateSolutionsFound++;
                }
            }
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
            System.out.println((60 - state.depth) + " is the most free slots we've seen to date while retreating, printing stats...");
            printStats();
            state.print();
        }
    }

    void printStats() {
        NumberFormat formatter = new DecimalFormat("0.######E0");
        System.out.println(formatter.format(movesSeenInThisGame) + " moves tried");
        System.out.println(solutionsFound + " unique solutions found");
        System.out.println(duplicateSolutionsFound + " duplicate solutions found");
        System.out.println(solutions.numOverflowed
                + " potentially unique solutions exceeded lookup table capacity");
        System.out.println();
    }

    // TODO: remove sanity checks
    void clear(Move move) throws SanityCheckException {
        char c = state.getCourse(move);
        if (c != move.course.code) {
            throw new SanityCheckException(move.slot + ": expected " + move.course + " but found " + c);
        }
        state.clearCourse(move.slot);
    }

    private boolean satisfiesCorrectnessConstraints(Move move) throws SanityCheckException {
        validator.reset();
        validator.validateCorrectnessConstraints(move);
        return !validator.hasErrors();
    }

}
