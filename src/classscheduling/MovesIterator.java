/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import java.util.ArrayList;
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

    private final List<Slot> freeSlotList;

    private Slot nextMoveToTry;

    private Course currentCourse;

    private Random randomGenerator;

    public MovesIterator(Schedule schedule, Course currentCourse) {
        this.schedule = schedule;
        badMoves = new HashMap<>();
        freeSlotList = schedule.initFreeSlotList();
        for (Slot slot : freeSlotList) {
            List<Course> badMovesForThisSlot = new ArrayList<>();
            badMoves.put(slot, badMovesForThisSlot);
        }
        this.currentCourse = currentCourse;
        nextMoveToTry = freeSlotList.get(0);
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
        nextMoveToTry = other.nextMoveToTry;
        this.randomGenerator = new Random();
    }

    public boolean notDone() {
        return nextMoveToTry != null;
    }

    // returns a slot filled with a course that has not yet been tried in that slot
    Slot move() throws SanityCheckException {
        Slot result = nextMoveToTry;
        schedule.set(result, currentCourse);
        if (!freeSlotList.remove((Slot) result)) {
            throw new SanityCheckException("free slot list does not contain " + (Slot) result);
        }
        nextMoveToTry = selectNextMove();
        return result;
    }
    
    void retreat(Slot slot) throws SanityCheckException {
        List<Course> badMovesForThisSlot = badMoves.get(slot);
        Course course = slot.getCourse();
        if (badMovesForThisSlot.contains(course)) {
            throw new SanityCheckException("Course " + course + " already tried for slot " + slot);
        }
        badMovesForThisSlot.add(course);
        if (badMovesForThisSlot.size() == Course.values().length) {
            freeSlotList.remove(slot);
        }
        schedule.clear(slot);
    }

    private void selectNextCourse() {
        for (Course course : Course.values()) {
            if (course.equals(currentCourse)) {
                continue;
            }
            if (schedule.enoughPeriodsPerWeek(course)) {
                continue;
            }
            currentCourse = course;
            return;
        }
        currentCourse = null;
    }

    private Slot selectNextMove() {
        Slot result = null;
        if (schedule.enoughPeriodsPerWeek(currentCourse)) {
            selectNextCourse();
        }
        if (currentCourse == null) {
            return result;
        }
        ArrayList<Slot> remainingSlots = new ArrayList<>(freeSlotList);
        while (!remainingSlots.isEmpty()) {
            int index = randomGenerator.nextInt(remainingSlots.size());
            Slot slot = remainingSlots.get(index);
            List<Course> movesTriedForThisSlot = badMoves.get(slot);
            if (movesTriedForThisSlot.contains(currentCourse)) {
                remainingSlots.remove(slot);
                continue;
            }
            result = slot;
            break;
        }
//        for (Slot slot : freeSlotList) {
//            List<Course> movesTriedForThisSlot = badMoves.get(slot);
//            if (movesTriedForThisSlot.contains(currentCourse)) {
//                continue;
//            }
//            result = slot;
//            break;
//        }
        return result;
    }

}
