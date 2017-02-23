/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Schedule.MILLION;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *
 * @author krzys
 */
public class MovesIterator {

    final static long MAX_ATTEMPTED_MOVES = 100;
    private final Schedule schedule;

    private final HashMap<Slot, List<Course>> badMoves;

    long millionsOfMovesTried;
    long movesTriedInThisMillion;

    private final List<Slot> remainingSlots;

    private final List<Course> remainingCourses;

    private Slot nextSlotToTry;

    Course currentCourse;

    private final Random randomGenerator;

    public MovesIterator(Schedule schedule, Course currentCourse) {
        this.schedule = schedule;
        badMoves = new HashMap<>();
        remainingSlots = new ArrayList<>();
        for (Slot slot : schedule.freeSlotList) {
            remainingSlots.add(slot);
        }
        for (Slot slot : remainingSlots) {
            List<Course> badMovesForThisSlot = new ArrayList<>();
            badMoves.put(slot, badMovesForThisSlot);
        }
        this.currentCourse = currentCourse;
        nextSlotToTry = remainingSlots.get(0);
        randomGenerator = new Random();
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
        badMoves = new HashMap<>();
        remainingSlots = new ArrayList<>();
        for (Slot slot : schedule.freeSlotList) {
            remainingSlots.add(slot);
        }
        for (Slot slot : remainingSlots) {
            if (other.badMoves.containsKey(slot)) {
                badMoves.put(slot, new ArrayList<>(other.badMoves.get(slot)));
            }
        }
        currentCourse = other.currentCourse;
        nextSlotToTry = other.nextSlotToTry;
        randomGenerator = new Random();
//        totalMovesForThisCourse = other.totalMovesForThisCourse;
//        millionsOfMovesTried = other.millionsOfMovesTried;
//        movesTriedInThisMillion = other.movesTriedInThisMillion;
        remainingCourses = new ArrayList<>();
        for (Course course : other.remainingCourses) {
            remainingCourses.add(course);
        }
    }

    public boolean notDone() {
        return nextSlotToTry != null && currentCourse != null;
    }

    // returns a slot filled with a course that has not yet been tried in that slot
    Slot move() throws SanityCheckException {
        if (movesTriedInThisMillion < MILLION) {
            movesTriedInThisMillion++;
        } else {
            movesTriedInThisMillion = 0;
            millionsOfMovesTried++;
        }
        Slot result = nextSlotToTry;
        schedule.set(result, currentCourse);
        if (!remainingSlots.remove((Slot) result)) {
            throw new SanityCheckException("free slot list does not contain " + (Slot) result);
        }
        nextSlotToTry = prepareNextMove();
        return result;
    }

    void retreat(Slot slot) throws SanityCheckException {
//        List<Course> badMovesForThisSlot = badMoves.get(slot);
//        Course course = slot.getCourse();
//        if (badMovesForThisSlot.contains(course)) {
//            throw new SanityCheckException("Course " + course + " already tried for slot " + slot);
//        }
//        badMovesForThisSlot.add(course);
//        if (badMovesForThisSlot.size() == Course.values().length) {
//            freeSlotList.remove(slot);
//        }
        schedule.clear(slot);
    }

    boolean takingTooLong() {
//                return (millionsOfMovesTried > 186);
//        return (millionsOfMovesTried > 1);
        return (millionsOfMovesTried > 0) && (movesTriedInThisMillion > 1000);
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
    //        int index = randomGenerator.nextInt(courses.size());
    //        currentCourse = courses.get(index);
    //        totalMovesForThisCourse = 0;
    //        System.out.println("switched to course: " + currentCourse);
    //    }
    //
    //        for (Course course : Course.values()) {
    //            if (course.equals(currentCourse)) {
    //                continue;
    //            }
    //            if (schedule.enoughPeriodsPerWeek(course)) {
    //                continue;
    //            }
    //            currentCourse = course;
    //            totalMovesForThisCourse = 0;
    //            return;
    //        }
    //        currentCourse = null;
    //    }

    private Slot prepareNextMove() throws SanityCheckException {
//        else {
//            totalMovesForThisCourse++;
//            if (totalMovesForThisCourse > 30) {
//                selectNextCourse();
//            }
//        }
        if (currentCourse == null) {
            return null;
        }
        if (remainingSlots.isEmpty()) {
            return null;
        }
        return remainingSlots.get(0);
    }
//        int index = randomGenerator.nextInt(freeSlotList.size());
//        result = freeSlotList.get(index);
//        if (isKnownBadMove(result, currentCourse)) {
//            repeatedBadMoves++;
//        }
//        return freeSlotList.get(index);
//    }
//
//        ArrayList<Slot> remainingSlots = new ArrayList<>(freeSlotList);
//        while (!remainingSlots.isEmpty()) {
//            int index = randomGenerator.nextInt(remainingSlots.size());
//            Slot slot = remainingSlots.get(index);
//            List<Course> movesTriedForThisSlot = badMoves.get(slot);
//            if (movesTriedForThisSlot.contains(currentCourse)) {
//                remainingSlots.remove(slot);
//                continue;
//            }
//            result = slot;
//            break;
//        }
//        for (Slot slot : freeSlotList) {
//            List<Course> movesTriedForThisSlot = badMoves.get(slot);
//            if (movesTriedForThisSlot.contains(currentCourse)) {
//                continue;
//            }
//            result = slot;
//            break;
//        }
//        return result;
//    }

//    private boolean isKnownBadMove(Slot slot, Course course) {
//        List<Course> badMovesForThisSlot = badMoves.get(slot);
//        return badMovesForThisSlot.contains(course);
//    }
}
