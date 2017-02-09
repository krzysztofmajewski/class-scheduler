/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author krzys
 */
// TODO: review throws clauses
public class Schedule {

    public static final String VERSION = "v3";

    public static final int MILLION = 1000000;

    long movesTried;
    int freeSlots = 60;

    final ScheduleValidator validator;

    public Schedule() {
        validator = new ScheduleValidator(this);
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

    boolean fillSchedule(Slot lastFilledSlot, List<Slot> freeSlotList, int coursesFilled) throws SanityCheckException {
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
        if (coursesFilled == Course.values().length) {
            validator.validate();
            return validator.hasNoErrors();
        }
        // schedule correct but not complete
        for (Slot slot : freeSlotList) {
            slot = tryNextFreeSlotForCurrentCourse(lastFilledSlot, slot, coursesFilled);
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
            int newCoursesFilled = coursesFilled;
            boolean sameCourse = lastFilledSlot.getCourse().equals(slot.getCourse());
            if (!sameCourse) {
                newCoursesFilled++;
            }
            boolean success = fillSchedule(slot, smallerFreeSlotList, newCoursesFilled);
            // if the recursive call succeeded, we are done!
            if (success) {
                return true;
            }
            // recursive call failed, try another slot
            clear(slot);
        }
        // no slot can be filled
//        System.out.println("no free slot can be filled");
//        System.out.println(coursesFilled + " courses filled");
//        System.out.println("last successfully filled slot: " + lastFilledSlot);
//        System.out.println("[" + VERSION + "] " + freeSlots + " free slots left after "
//                + movesTried + " legal moves:");
//        print();
//        validator.validate();
//        validator.printErrors();
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
//        history.push(slot);
        course.incrementPeriodsScheduled(slot);
        return slot;
    }

    private void clear(Slot slot) throws SanityCheckException {
        Course course = slot.getCourse();
        course.decrementPeriodsScheduled(slot);
        slot.gradeDay.clear(slot.period);
        freeSlots++;
//        Slot lastFilledSlot = history.pop();
//        if (!lastFilledSlot.equals(slot)) {
//            throw new SanityCheckException("last filled is " + lastFilledSlot
//                    + ", expected " + slot);
//        }
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

//    private List<Course> initFreeCourseList() {
//        ArrayList<Course> result = new ArrayList<>();
//        result.addAll(Arrays.asList(Course.values()));
//        return result;
//    }
    private Slot tryNextFreeSlotForCurrentCourse(Slot lastFilledSlot,
            Slot slotToFill,
            int coursesFilled) throws SanityCheckException {
        Course currentCourse = lastFilledSlot.getCourse();
        if (enoughPeriodsPerWeek(currentCourse)) {
            coursesFilled++;
            if (coursesFilled >= Course.values().length) {
                return null;
            }
            currentCourse = Course.values()[coursesFilled];
        }
        return set(slotToFill, currentCourse);
    }

//    private Course todo() {
//        Course result = null;
//        for (Course course : Course.values()) {
//            if (!enoughPeriodsPerWeek(course)) {
//                result = course;
//                break;
//            }
//        }
//        return result;
//    }
//    private Course fillSlotWithNextAvailableCourse(Slot slot) throws SanityCheckException {
//        Course c = todo();
//        set(slot, c);
//        return c;
//    }
//    private Course fillSlotWithCourse(Slot slot, Course course) throws SanityCheckException {
//        Course result = course;
//        if (enoughPeriodsPerWeek(course)) {
//            // already enough periods
//            result = fillSlotWithNextAvailableCourse(slot);
//        } else {
//            // not enough periods yet, fill the slot
//            set(slot, course);
//        }
//        return result;
//    }
    private boolean enoughPeriodsPerWeek(Course course) {
        for (Grade g : Grade.values()) {
            if (course.getPeriodsScheduled(g) < course.periods) {
                return false;
            }
        }
        return true;
    }

}
