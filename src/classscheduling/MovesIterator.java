/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Schedule.MILLION;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author krzys
 */
public class MovesIterator {

    static long BAD_MOVE_THRESHOLD = 100;
    private final Schedule schedule;

    long illegalMovesTried;
    
    long movesPruned;
    
    private final List<Slot> remainingSlots;

    private final List<Course> remainingCourses;

    private Slot nextSlotToTry;

    Course currentCourse;

    public MovesIterator(Schedule schedule, Course currentCourse) {
        this.schedule = schedule;
        remainingSlots = new ArrayList<>();
        for (Slot slot : schedule.freeSlotList) {
            remainingSlots.add(slot);
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
    }

    public MovesIterator(MovesIterator other) {
        schedule = other.schedule;
        remainingSlots = new ArrayList<>();
        for (Slot slot : schedule.freeSlotList) {
            remainingSlots.add(slot);
        }
        currentCourse = other.currentCourse;
        nextSlotToTry = other.nextSlotToTry;
        remainingCourses = new ArrayList<>();
        for (Course course : other.remainingCourses) {
            remainingCourses.add(course);
        }
        illegalMovesTried = other.illegalMovesTried;
    }

    boolean notDone() {
        return nextSlotToTry != null && currentCourse != null;
    }

    boolean takingTooLong() {
        return illegalMovesTried >= BAD_MOVE_THRESHOLD;
    }

    // returns a slot filled with a course that has not yet been tried in that slot
    Slot move() throws SanityCheckException {
        Slot result = nextSlotToTry;
        schedule.set(result, currentCourse);
        if (!remainingSlots.remove((Slot) result)) {
            throw new SanityCheckException("free slot list does not contain " + (Slot) result);
        }
        nextSlotToTry = prepareNextMove();
        return result;
    }

    void retreat(Slot slot) throws SanityCheckException {
        schedule.clear(slot);
    }

    void selectNextCourse() throws SanityCheckException {
        for (Slot slot : schedule.freeSlotList) {
            if (!remainingSlots.contains(slot)) {
                remainingSlots.add(slot);
            }
        }
        if (remainingCourses.isEmpty()) {
            currentCourse = null;
            return;
        }
        if (!remainingCourses.remove(currentCourse)) {
            throw new SanityCheckException(currentCourse + " not found in remaining courses list");
        }
        if (remainingCourses.isEmpty()) {
            currentCourse = null;
            return;
        }
        currentCourse = remainingCourses.get(0);
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

    static void increaseThreshold() {
        BAD_MOVE_THRESHOLD *= 2;
    }
}
