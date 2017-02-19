/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/**
 *
 * @author krzys
 */
// TODO: review throws clauses
public class Schedule {

    public static final String VERSION = "v4";

    public static final int MILLION = 1000000;

    long movesTried;
    int freeSlots = 60;

    final ScheduleValidator validator;

    private final Random randomGenerator;

    public Schedule() {
        validator = new ScheduleValidator(this);
        randomGenerator = new Random();
    }

    void print() {
        for (Grade g : Grade.values()) {
            System.out.printf("%10s", g.name() + " |");
            for (Day day : Day.values()) {
                for (Period period : Period.values()) {
                    System.out.print(day.getGradeDay(g).get(period));
                }
                System.out.print("|");
            }
            System.out.println();
        }
    }

    boolean fillSchedule(Slot lastFilledSlot, List<Slot> freeSlotList) throws SanityCheckException {
        if (freeSlotList == null) {
            freeSlotList = initFreeSlotList();
        }
        movesTried++;
        // print a status update every once in a while
        if ((movesTried % MILLION) == 1) {
            System.out.println("[" + VERSION + "] " + freeSlots + " free slots left after "
                    + movesTried / MILLION + " million legal moves:");
            print();
            validator.validate();
            validator.printErrors();
        }
        // check if we are done
        if (freeSlotList.isEmpty()) {
            validator.validate();
            return validator.hasNoErrors();
        }
        // schedule correct but not complete
        for (Slot slot : freeSlotList) {
            slot = tryNextFreeSlot(slot, lastFilledSlot.getCourse());
            if (slot == null) {
                // no more courses left, but still slots left
                // TODO: can this ever happen?
                return true;
            }
            // slot not null
            validator.reset();
            // check for correctness of current schedule
            validator.validateCorrectnessConstraints(slot);
            if (validator.hasErrors()) {
                // no solution from this move, try another slot
                clear(slot);
                continue;
            }
            // this move is good
            List<Slot> smallerFreeSlotList = new ArrayList<>(freeSlotList);
            boolean ok = smallerFreeSlotList.remove(slot);
            if (!ok) {
                throw new SanityCheckException(slot + " not found in free slot list");
            }
            boolean success = fillSchedule(slot, smallerFreeSlotList);
            // if the recursive call succeeded, we are done!
            if (success) {
                return true;
            }
            // recursive call failed, try another slot
            clear(slot);
        }
        // no slot can be filled
        return false;
    }

    Slot set(Day day, Grade grade, Period period, Course course) throws SanityCheckException {
        GradeDay gd = day.getGradeDay(grade);
        Slot s = new Slot();
        s.day = day;
        s.gradeDay = gd;
        s.period = period;
        return set(s, course);
    }

    private Slot set(Slot slot, Course course) throws SanityCheckException {
        char c = slot.gradeDay.get(slot.period);
        if (c != 0) {
            throw new SanityCheckException(slot + " already has a course: " + c);
        }
        slot.gradeDay.set(slot.period, course.code);
        freeSlots--;
        course.incrementPeriodsScheduled(slot);
        return slot;
    }

    private void clear(Slot slot) throws SanityCheckException {
        Course course = slot.getCourse();
        course.decrementPeriodsScheduled(slot);
        slot.gradeDay.clear(slot.period);
        freeSlots++;
    }

    private List<Slot> initFreeSlotList() {
        ArrayList<Slot> result = new ArrayList<>();
        for (Day day : Day.values()) {
            for (Grade g : Grade.values()) {
                for (Period p : Period.values()) {
                    if (day.getGradeDay(g).get(p) == 0) {
                        Slot slot = new Slot();
                        slot.day = day;
                        slot.gradeDay = day.getGradeDay(g);
                        slot.period = p;
                        result.add(slot);
                    }
                }
            }
        }
        return result;
    }

    private Slot tryNextFreeSlot(Slot slotToFill, Course lastUsedCourse) throws SanityCheckException {
        Course course = selectNextCourse(lastUsedCourse);
        if (course == null) {
            return null;
        }
        return set(slotToFill, course);
    }

    private Course selectNextCourse(Course lastUsedCourse) throws SanityCheckException {
        if (!enoughPeriodsPerWeek(lastUsedCourse)) {
            return lastUsedCourse;
        }
        ArrayList<Course> courses = new ArrayList<>(EnumSet.allOf(Course.class));
        for (Course course : Course.values()) {
            if (enoughPeriodsPerWeek(course)) {
                boolean ok = courses.remove(course);
                if (!ok) {
                    throw new SanityCheckException(course + " not found in courses list");
                }
            }
        }
        if (courses.isEmpty()) {
            return null;
        }
        int index = randomGenerator.nextInt(courses.size());
        return courses.get(index);
    }

    private boolean enoughPeriodsPerWeek(Course course) {
        for (Grade g : Grade.values()) {
            if (course.getPeriodsScheduled(g) < course.periods) {
                return false;
            }
        }
        return true;
    }

}
