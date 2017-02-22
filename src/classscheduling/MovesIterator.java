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

    private final Schedule schedule;

    private final HashMap<Slot, List<Course>> badMoves;

    long repeatedBadMoves;
    long totalMoves;
    long totalMovesForThisCourse;

    private final List<Slot> freeSlotList;

    private Slot nextSlotToTry;

    private Course currentCourse;

    private final Random randomGenerator;

    public MovesIterator(Schedule schedule, Course currentCourse) {
        this.schedule = schedule;
        badMoves = new HashMap<>();
        freeSlotList = schedule.initFreeSlotList();
        for (Slot slot : freeSlotList) {
            List<Course> badMovesForThisSlot = new ArrayList<>();
            badMoves.put(slot, badMovesForThisSlot);
        }
        this.currentCourse = currentCourse;
        nextSlotToTry = freeSlotList.get(0);
        this.randomGenerator = new Random();
    }

    public MovesIterator(MovesIterator other) {
        this.schedule = other.schedule;
        badMoves = new HashMap<>();
        freeSlotList = schedule.initFreeSlotList();
        for (Slot slot : freeSlotList) {
            if (other.badMoves.containsKey(slot)) {
                badMoves.put(slot, new ArrayList<>(other.badMoves.get(slot)));
            }
        }
        currentCourse = other.currentCourse;
        nextSlotToTry = other.nextSlotToTry;
        this.randomGenerator = new Random();
    }

    public boolean notDone() {
        return nextSlotToTry != null;
    }

    // returns a slot filled with a course that has not yet been tried in that slot
    Slot move() throws SanityCheckException {
        totalMoves++;
        Slot result = nextSlotToTry;
        schedule.set(result, currentCourse);
        if (!freeSlotList.remove((Slot) result)) {
            throw new SanityCheckException("free slot list does not contain " + (Slot) result);
        }
        nextSlotToTry = prepareNextMove();
        return result;
    }

    void retreat(Slot slot) throws SanityCheckException {
        List<Course> badMovesForThisSlot = badMoves.get(slot);
        Course course = slot.getCourse();
//        if (badMovesForThisSlot.contains(course)) {
//            throw new SanityCheckException("Course " + course + " already tried for slot " + slot);
//        }
        badMovesForThisSlot.add(course);
        if (badMovesForThisSlot.size() == Course.values().length) {
            freeSlotList.remove(slot);
        }
        schedule.clear(slot);
    }

    boolean takingTooLong() {
        return (totalMovesForThisCourse > 10 * MILLION);
    }
    
    private void selectNextCourse() {
        ArrayList<Course> courses = new ArrayList<>(Arrays.asList(Course.values()));
        courses.remove(currentCourse);
        if (courses.isEmpty()) {
            currentCourse = null;
            return;
        }
        int index = randomGenerator.nextInt(courses.size());
        currentCourse = courses.get(index);
        totalMovesForThisCourse = 0;
//        System.out.println("switched to course: " + currentCourse);
    }
        
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

    // TODO just pick one at random
    private Slot prepareNextMove() {
        Slot result = null;
        if (schedule.enoughPeriodsPerWeek(currentCourse)) {
            selectNextCourse();
        } else {
            totalMovesForThisCourse++;
        }
//        else {
//            totalMovesForThisCourse++;
//            if (totalMovesForThisCourse > 30) {
//                selectNextCourse();
//            }
//        }
        if (currentCourse == null) {
            return result;
        }
        if (freeSlotList.isEmpty()) {
            return null;
        }
        int index = randomGenerator.nextInt(freeSlotList.size());
        result = freeSlotList.get(index);
        if (isKnownBadMove(result, currentCourse)) {
            repeatedBadMoves++;
        }
        return freeSlotList.get(index);
    }
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

    private boolean isKnownBadMove(Slot slot, Course course) {
        List<Course> badMovesForThisSlot = badMoves.get(slot);
        return badMovesForThisSlot.contains(course);
    }

}
