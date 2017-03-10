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

    private final Schedule schedule;

    private final List<Slot> remainingSlots;

    private final List<Course> remainingCourses;

    private Slot nextSlotToTry;

    Course currentCourse;

    // TODO: optimize?
    public MovesIterator(Schedule schedule, Course currentCourse) {
        this.schedule = schedule;
        remainingSlots = new ArrayList<>();
        for (Grade grade : Grade.values()) {
            for (Day day : Day.values()) {
                for (Period period : Period.values()) {
                    Course course = schedule.getCourse(grade, day, period);
                    if (course == null) {
                        Slot slot = new Slot(grade, day, period);
                        remainingSlots.add(slot);
                    }
                }
            }
        }
        this.currentCourse = currentCourse;
        nextSlotToTry = remainingSlots.get(0);
        remainingCourses = new ArrayList<>();
        for (Course course : Course.values()) {
            if (schedule.enoughPeriodsPerWeek(course)) {
                continue;
            }
            remainingCourses.add(course);
        }
        this.depth = 60 - remainingSlots.size();
    }

    // TODO: optimize?
    public MovesIterator(MovesIterator parent) throws SanityCheckException {
        schedule = parent.schedule;
        remainingSlots = new ArrayList<>();
        for (Slot slot : parent.remainingSlots) {
            Course course = schedule.getCourse(slot.grade, slot.day, slot.period);
            if (course != null) {
                throw new SanityCheckException(slot + " should not have a course");
            }
            remainingSlots.add(slot);
        }
        currentCourse = parent.currentCourse;
        nextSlotToTry = parent.nextSlotToTry;
        remainingCourses = new ArrayList<>();
        for (Course course : parent.remainingCourses) {
            remainingCourses.add(course);
        }
        this.depth = parent.depth + 1;
    }

    boolean notDone() {
        return nextSlotToTry != null && currentCourse != null;
    }

    // returns a slot filled with a course that has not yet been tried in that slot
    Move move() throws SanityCheckException {
        Move result = new Move(nextSlotToTry, currentCourse);
        schedule.set(result);
        if (!remainingSlots.remove((Slot) nextSlotToTry)) {
            throw new SanityCheckException("free slot list does not contain " + (Slot) nextSlotToTry);
        }
        nextSlotToTry = prepareNextMove();
        return result;
    }

    void retreat(Move move) throws SanityCheckException {
        schedule.clear(move);
    }

    void selectNextCourse() throws SanityCheckException {
        if (!remainingCourses.remove(currentCourse)) {
            throw new SanityCheckException(currentCourse + " not found in remaining courses list");
        }
        if (remainingCourses.isEmpty()) {
            currentCourse = null;
            return;
        }
        currentCourse = remainingCourses.get(0);
        // TODO: optimize?
        for (Grade grade : Grade.values()) {
            for (Day day : Day.values()) {
                for (Period period : Period.values()) {
                    Course course = schedule.getCourse(grade, day, period);
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

    private Slot prepareNextMove() throws SanityCheckException {
        if (currentCourse == null) {
            return null;
        }
        if (remainingSlots.isEmpty()) {
            return null;
        }
        return remainingSlots.get(0);
    }

    void markMoveAsIllegal() throws SanityCheckException {
        schedule.hopelessPartialSchedules.addThisPartialSchedule(depth);
    }

}
