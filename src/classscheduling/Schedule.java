/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Course.ART;
import static classscheduling.Course.ENGLISH;
import static classscheduling.Course.FRENCH;
import static classscheduling.Course.GEOGRAPHY;
import static classscheduling.Course.MATH;
import static classscheduling.Course.MUSIC;
import static classscheduling.Day.FRIDAY;
import static classscheduling.Day.MONDAY;
import static classscheduling.Day.THURSDAY;
import static classscheduling.Day.TUESDAY;
import static classscheduling.Day.WEDNESDAY;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author krzys
 */
// TODO: review throws clauses
public class Schedule {

    public static final int MILLION = 1000000;

    int movesTried;
    int freeSlots = 60;

    Day monday;
    Day tuesday;
    Day wednesday;
    Day thursday;
    Day friday;

    final Course courses[];

    final ValidationErrors errors;

    final Stack<Slot> history;

    private final Day days[];

    public Schedule() {
        monday = MONDAY;
        tuesday = TUESDAY;
        wednesday = WEDNESDAY;
        thursday = THURSDAY;
        friday = FRIDAY;

        days = new Day[5];
        days[0] = monday;
        days[1] = tuesday;
        days[2] = wednesday;
        days[3] = thursday;
        days[4] = friday;

        courses = new Course[6];
        courses[0] = FRENCH;
        courses[1] = GEOGRAPHY;
        courses[2] = MATH;
        courses[3] = ENGLISH;
        courses[4] = ART;
        courses[5] = MUSIC;

        errors = new ValidationErrors();

        history = new Stack<>();
    }

    // TODO make smart decisions based on which constraint failed
    boolean fillSchedule(Slot lastFilledSlot) throws SanityCheckException {
        movesTried++;
        // print a status update every once in a while
        if ((movesTried % MILLION) == 1) {
            System.out.println(freeSlots + " free slots left after "
                    + movesTried / MILLION + " million moves:");
            print();
        }
        // check if we are done
        if (freeSlots == 0) {
            errors.clear();
            // TODO: should we validate the whole schedule?
            validate(lastFilledSlot);
            if (errors.isEmpty()) {
                return true;
            }
        }
        errors.clear();
        // check for correctness of current schedule
        validateCorrectnessConstraints(lastFilledSlot);
        if (errors.hasErrors()) {
            // adding more slots won't help us
            return false;
        }
//        // TODO: remove this once we know it works
//        errors.clear();
//        validateCompletenessConstraints(lastFilledSlot);
//        if (errors.isEmpty()) {
//            throw new SanityCheckException("this should never happen");
//        }
        // schedule correct but not complete
        for (Slot slot : getEmptySlots()) {
            Course course = null;
            Course actualCourse = null;
            if (lastFilledSlot == null) {
                fillSlotWithNextAvailableCourse(slot);
            } else {
                char c = lastFilledSlot.gradeDay.get(lastFilledSlot.period);
                course = Course.forCode(c);
                actualCourse = fillSlotWithCourse(slot, course);
                if (!actualCourse.equals(course)) {
                    // nop
                    boolean nop = true;
                }
            }
            boolean success = fillSchedule(slot);
            // if the recursive call succeeded, we are done!
            if (success) {
                return true;
            }
            // no solution from this move, roll back
            clear(slot);
        }
        // no empty slot yields a winner
        return false;
    }

    void set(Day day, Grade grade, Period period, Course course) throws SanityCheckException {
        GradeDay gd = day.getGradeDay(grade);
        Slot s = new Slot();
        s.day = day;
        s.gradeDay = gd;
        s.period = period;
        set(s, course);
    }
    
    private void set(Slot slot, Course course) throws SanityCheckException {
        char c = slot.gradeDay.get(slot.period);
        if (c != 0) {
            throw new SanityCheckException(slot + " already has a course: " + c);
        }
        slot.gradeDay.set(slot.period, course.code);
        freeSlots--;
        history.push(slot);        
    }
    
    void clear(Slot slot) throws SanityCheckException {
        slot.gradeDay.clear(slot.period);
        freeSlots++;
        history.pop();
    }
    
    List<Slot> getEmptySlots() {
        ArrayList<Slot> result = new ArrayList<>();

        for (Day day : days) {
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

    void validate(Slot lastFilledSlot) {
        validateCorrectnessConstraints(lastFilledSlot);
        validateCompletenessConstraints(lastFilledSlot);
    }

    // once violated, cannot be un-violated by filling more slots
    void validateCorrectnessConstraints(Slot slot) {
        try {
            noCourseTwicePerDay(slot);
            teacherHasAtMostThreePeriodsPerDay(slot);
            conflictsWithConference(slot);
            notTooManyPeriodsPerWeek(slot);
            teacherDaysOff(slot);
            teacherOneSlotAtATime(slot);
        } catch (ValidationException ve) {
            // nop
            boolean nop = true;
        }
    }

    // once violated, can be un-violated by filling more slots
    void validateCompletenessConstraints(Slot slot) {
        try {
            frenchConferenceClass(slot);
            enoughPeriodsPerWeek(slot);
        } catch (ValidationException ve) {
            // nop
        }
    }

    Course todo() {
        Course result = null;
        errors.clear();
        for (Course course : courses) {
            try {
                enoughPeriodsPerWeek(course);
            } catch (ValidationException ve) {
                // nop
            }
            if (errors.hasErrors()) {
                // not enough periods per week
                return course;
            }
        }
        return result;
    }

    Course fillSlotWithNextAvailableCourse(Slot slot) throws SanityCheckException {
        Course c = todo();
        set(slot, c);
        return c;
    }

    Course fillSlotWithCourse(Slot slot, Course c) throws SanityCheckException {
        Course result = c;
        errors.clear();
        try {
            enoughPeriodsPerWeek(c);
        } catch (ValidationException ve) {
            // nop
        }
        if (errors.isEmpty()) {
            // already enough periods
            result = fillSlotWithNextAvailableCourse(slot);
        } else {
            // not enough periods
            set(slot, c);
        }
        return result;
    }

    void print() {
        for (Grade g : Grade.values()) {
            System.out.printf("%10s", g.name() + " |");
            for (Day day : days) {
                for (Period period : Period.values()) {
                    System.out.print(day.getGradeDay(g).get(period));
                }
                System.out.print("|");
            }
            System.out.println();
        }
    }

    private void enoughPeriodsPerWeek(Course course) throws ValidationException {
        for (Grade g : Grade.values()) {
            int gradeCount = 0;
            for (Day day : days) {
                GradeDay gd = day.getGradeDay(g);
                gradeCount += gd.count(course.code);
            }
            if (gradeCount < course.periods) {
                errors.add(g + ": not enough periods of " + course.name);
            }
        }
    }

    private void enoughPeriodsPerWeek(Slot slot) throws ValidationException {
        Course slotCourse = null;
        if (slot != null) {
            char c = slot.gradeDay.get(slot.period);
            slotCourse = Course.forCode(c);
        }
        for (Course course : courses) {
            if ((slotCourse != null) && (!course.equals(slotCourse))) {
                // irrelevant
                continue;
            }
            enoughPeriodsPerWeek(course);
        }
    }

    private void notTooManyPeriodsPerWeek(Slot slot) throws ValidationException {
        Course slotCourse = null;
        Grade slotGrade = null;
        if (slot != null) {
            char c = slot.gradeDay.get(slot.period);
            slotCourse = Course.forCode(c);
            slotGrade = slot.gradeDay.grade;
        }
        for (Course course : courses) {
            if ((slotCourse != null) && !slotCourse.equals(course)) {
                // irrelevant
                continue;
            }
            for (Grade g : Grade.values()) {
                if ((slotGrade != null) && (!slotGrade.equals(g))) {
                    // irrelevant
                    continue;
                }
                int gradeCount = 0;
                for (Day day : days) {
                    GradeDay gd = day.getGradeDay(g);
                    gradeCount += gd.count(course.code);
                }
                if (gradeCount > course.periods) {
                    errors.add(g + ": too many periods of " + course.name);
                }
            }
        }
    }

    private void noCourseTwicePerDay(Slot slot) throws ValidationException {
        Course slotCourse = null;
        if (slot != null) {
            char c = slot.gradeDay.get(slot.period);
            slotCourse = Course.forCode(c);
        }
        for (Course c : courses) {
            if ((slotCourse != null) && (!c.equals(slotCourse))) {
                // irrelevant
                continue;
            }
            for (Day day : days) {
                for (Grade g : Grade.values()) {
                    GradeDay gd = day.getGradeDay(g);
                    if (gd.count(c.code) > 1) {
                        errors.add(gd.grade.name() + ": too many " + c.name + " classes"
                                + " on " + day.name);
                    }
                }
            }
        }
    }

    private void teacherHasAtMostThreePeriodsPerDay(Slot slot) throws ValidationException {
        if (slot != null) {
            char c = slot.gradeDay.get(slot.period);
            if (slot.day.count(c) > 3) {
                Course course = Course.forCode(c);
                errors.add(course.name + " teacher has too many periods on "
                        + slot.day.name);
            }
            return;
        }
        for (Day day : days) {
            for (Course c : courses) {
                if ((c.code != 'F') && (c.code != 'G')) {
                    if (day.count(c.code) > 3) {
                        errors.add(c.name + " teacher has too many periods on "
                                + day.name);
                    }
                }
            }
            int frenchAndGeoPeriods = day.count('F') + day.count('G');
            if (frenchAndGeoPeriods > 3) {
                errors.add("French/Geography teacher has too many periods on "
                        + day.name);
            }
        }
    }

    // completeness (implies correctness?)
    private void frenchConferenceClass(Slot slot) throws ValidationException {
        Day slotDay = null;
        Period slotPeriod = null;
        if (slot != null) {
            slotDay = slot.day;
            slotPeriod = slot.period;
        }
        for (Day day : days) {
            if ((slotDay != null) && !day.equals(slotDay)) {
                // irrelevant
                continue;
            }
            for (Period p : Period.values()) {
                if ((slotPeriod != null) && (!p.equals(slotPeriod))) {
                    // irrelevant
                    continue;
                }
                // TODO: optimize?
                if (!allGradesHaveFrench(day, p)
                        && !noGradesHaveFrench(day, p)) {
                    errors.add("French class in " + p + " on " + day.name
                            + " must be shared by all grades");
                }
            }
        }
    }

    private boolean allGradesHaveFrench(Day day, Period period) {
        return day.grade7.hasCourse('F', period)
                && day.grade8.hasCourse('F', period)
                && day.grade9.hasCourse('F', period);
    }

    private boolean noGradesHaveFrench(Day day, Period period) {
        return !(day.grade7.hasCourse('F', period)
                || day.grade8.hasCourse('F', period)
                || day.grade9.hasCourse('F', period));
    }

    private void teacherDaysOff(Slot slot) throws ValidationException {
        int daysOn = 0;
        Course slotCourse = null;
        if (slot != null) {
            char c = slot.gradeDay.get(slot.period);
            slotCourse = Course.forCode(c);
        }
        for (Course course : courses) {
            if ((slotCourse != null) && (!slotCourse.equals(course))) {
                // not relevant
                continue;
            }
            for (Day day : days) {
                if (day.count(course.code) > 0) {
                    daysOn++;
                }
            }
            boolean enoughDaysOff = ((5 - daysOn) >= course.daysOff);
            if (!enoughDaysOff) {
                errors.add("Teacher of " + course + " has only "
                        + (5 - daysOn) + " days off instead of " + course.daysOff);
            }
        }
    }

    private void teacherOneSlotAtATime(Slot slot) throws ValidationException {
        Course slotCourse = null;
        if (slot != null) {
            char c = slot.gradeDay.get(slot.period);
            slotCourse = Course.forCode(c);
        }
        for (Course course : courses) {
            if ((slotCourse != null) && (!slotCourse.equals(course))) {
                // not relevant
                continue;
            }
            // French is a conference class
            if (course.code == 'F') {
                return;
            }
            for (Day day : days) {
                for (Period p : Period.values()) {
                    int count = 0;
                    for (Grade g : Grade.values()) {
                        GradeDay gd = day.getGradeDay(g);
                        if (gd.hasCourse(course.code, p)) {
                            count++;
                        }
                    }
                    if (count > 1) {
                        errors.add(course.name + " teacher in two slots at once on "
                                + day.name + ", " + p);
                    }
                }
            }
        }
    }

    // correctness check only, not completeness
    private void conflictsWithConference(Slot slot) throws ValidationException {
        if (slot == null) {
            // this validation is redundant when applied to the whole schedule
            return;
        }
        GradeDay slotGradeDay = slot.gradeDay;
        Period slotPeriod = slot.period;
        char slotCourse = slotGradeDay.get(slotPeriod);
        Day slotDay = slot.day;
        for (Grade g : Grade.values()) {
            if (g.equals(slotGradeDay.grade)) {
                // cannot conflict with itself
                continue;
            }
            GradeDay gd = slotDay.getGradeDay(g);
            char c = gd.get(slotPeriod);
            if (slotCourse == 'F') {
                if (c == 'F') {
                    continue;
                }
                if (c == 0) {
                    continue;
                }
                // slot has 'F', and here we see some other course
                errors.add("French conference conflict in " + slotPeriod
                        + " on " + slotDay);
            } else if (slotCourse != 0) {
                if (c == 'F') {
                    // slot has some course other than 'F', and here we see 'F'
                    errors.add("French conference conflict in " + slotPeriod
                            + " on " + slotDay);
                }
            }
        }
    }
}
