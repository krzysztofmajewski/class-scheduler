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
public class MovesIterator {

    static long MAX_DEPTH = 60;

    final int depth;

    private final State state;

    private final List<Slot> remainingSlots;

    private Slot nextSlotToTry;

    private Course currentCourse;

    public MovesIterator(State state) throws SanityCheckException {
        this.state = state;
        remainingSlots = new ArrayList<>();
        initRemainingSlots();
        nextSlotToTry = remainingSlots.get(0);
        initCurrentCourse();
        this.depth = 60 - remainingSlots.size();
    }

    public MovesIterator(MovesIterator parent) throws SanityCheckException {
        state = parent.state;
        remainingSlots = new ArrayList<>();
        parent.remainingSlots.forEach((slot) -> {
            remainingSlots.add(slot);
        });
        nextSlotToTry = parent.nextSlotToTry;
        currentCourse = parent.currentCourse;
        this.depth = parent.depth + 1;
    }

    boolean notDone() throws SanityCheckException {
        if (currentCourse.enoughPeriodsPerWeek()) {
            initCurrentCourse();
            initRemainingSlots();
            prepareNextMove();
        }
        return nextSlotToTry != null && currentCourse != null;
    }

    // TODO: remove sanity check
    Move getNextMove() throws SanityCheckException {
        Move result = new Move(nextSlotToTry, currentCourse);
        if (!remainingSlots.remove((Slot) nextSlotToTry)) {
            throw new SanityCheckException("free slot list does not contain " + (Slot) nextSlotToTry);
        }
        prepareNextMove();
        return result;
    }

    // TODO: optimize?
    private void initRemainingSlots() {
        for (Grade grade : Grade.values()) {
            for (Day day : Day.values()) {
                for (Period period : Period.values()) {
                    // TODO: write State.getCourseCode()
                    char c = state.getCourse(grade, day, period);
                    Course course = Course.forCode(c);
                    if (course == null) {
                        Slot slot = new Slot(grade, day, period);
                        if (!remainingSlots.contains(slot)) {
                            remainingSlots.add(slot);
                        }
                    }
                }
            }
        }
    }

    private void prepareNextMove() throws SanityCheckException {
        if (remainingSlots.isEmpty()) {
            nextSlotToTry = null;
        } else {
            nextSlotToTry = remainingSlots.get(0);
        }
    }

    // TODO: optimize?
    private void initCurrentCourse() {
        currentCourse = null;
        for (Course course : Course.values()) {
            if (course.enoughPeriodsPerWeek()) {
                continue;
            }
            currentCourse = course;
            break;
        }
    }

}
