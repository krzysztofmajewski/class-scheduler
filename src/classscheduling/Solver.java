/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

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

    long movesSeenInThisGame = 0;

    final ScheduleValidator validator;

    public Solver(State state) {
        this.state = state;
        validator = new ScheduleValidator(state);
    }

    boolean solve() {
        validator.reset();
        validator.validateCorrectnessConstraints();
        if (validator.hasErrors()) {
            return false;
        }
        MovesIterator iterator = new MovesIterator(state);
        return scheduleCourses(iterator, null);
    }

    // returns true if it finds a solution
    boolean scheduleCourses(MovesIterator iterator, Move lastMove) {
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
                solutionsFound++;
                System.out.println("Found solution # " + solutionsFound + "!");
                state.print();
                printStats();
            }
            retreat(nextMove);
        }
        // tried all possible moves, did not find solution
        printInfoIfNeeded();
        return false;
    }

    Move move(MovesIterator iterator) {
        Move move = iterator.getNextMove();
        state.setCourse(move);
        return move;
    }

    void retreat(Move move) {
        clear(move);
        if (DEBUG) {
            System.out.println("Retreated from move: " + move.slot + " : " + move.course);
        }
    }

    void printInfoIfNeeded() {
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
        System.out.println();
    }

    void clear(Move move) {
        state.clearCourse(move.slot);
    }

    private boolean satisfiesCorrectnessConstraints(Move move) {
        validator.reset();
        validator.validateCorrectnessConstraints(move);
        return !validator.hasErrors();
    }

}
