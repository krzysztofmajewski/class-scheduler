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

    Day monday;
    Day tuesday;
    Day wednesday;
    Day thursday;
    Day friday;

    Course math;
    Course english;
    Course french;
    Course geography;
    Course art;
    Course music;

    final Course courses[];

    final ValidationErrors errors;

    private final Day days[];

    private final Stack<Slot> history;

    public Schedule() {
        monday = new Day("Monday");
        tuesday = new Day("Tuesday");
        wednesday = new Day("Wednesday");
        thursday = new Day("Thursday");
        friday = new Day("Friday");

        math = Course.Math();
        english = Course.English();
        french = Course.French();
        geography = Course.Geography();
        art = Course.Art();
        music = Course.Music();

        days = new Day[5];
        days[0] = monday;
        days[1] = tuesday;
        days[2] = wednesday;
        days[3] = thursday;
        days[4] = friday;

        courses = new Course[6];
        courses[0] = math;
        courses[1] = english;
        courses[2] = french;
        courses[3] = geography;
        courses[4] = art;
        courses[5] = music;

        errors = new ValidationErrors();

        history = new Stack<>();
    }

//    Slot peek() {
//        return history.peek();
//    }
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

    void validate() {
        validateMonotoneConstraints();
        validateNonMonotoneConstraints();
    }

    // once violated, cannot be un-violated by filling more slots
    void validateMonotoneConstraints() {
        noCourseTwicePerDay();
        teacherHasAtMostThreePeriodsPerDay();
        for (Course c : courses) {
            notTooManyPeriodsPerWeek(c);
            teacherDaysOff(c);
            teacherOneSlotAtATime(c);
        }
    }

    // once violated, can be un-violated by filling more slots
    void validateNonMonotoneConstraints() {
        frenchConferenceClass();
        for (Course c : courses) {
            enoughPeriodsPerWeek(c);
        }
    }

//    // TODO: randomize?
//    Course todo() {
//        Course result = null;
//        errors.clear();
//
//        for (Course c : courses) {
//            validatePeriodsPerWeek(c);
//            if (!errors.isEmpty()) {
//                return c;
//            }
//        }
//        return result;
//    }
    
    void fillSlot(Slot slot, Course c) {
        GradeDay gd = slot.gradeDay;
        Period p = slot.period;
        gd.set(p, c.code);
        history.push(slot);
    }

    void undo(Slot slot) throws Exception {
        GradeDay gd = slot.gradeDay;
        Period p = slot.period;
        gd.clear(p);
        history.pop();
    }

//    // assume the current configuration has no validation errors
//    // this implies that c is not partially scheduled
//    // TODO: randomize?
//    boolean scheduleOneSlot(Course c) throws Exception {
//        errors.clear();
//
//        for (Grade g : Grade.values()) {
//            if (!gradeComplete(c, g)) {
//                return scheduleOnePeriod(c, g);
//            }
//        }
//        // TODO: is this ever reached?
//        return false;
//    }
//
//    // undo the previously scheduled slot
//    void backTrack() throws Exception {
//        Slot slot = history.pop();
//        slot.gradeDay.clear(slot.period);
//    }
    int freeSlots() {
        return getEmptySlots().size();
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

    private void enoughPeriodsPerWeek(Course course) {
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

    private void notTooManyPeriodsPerWeek(Course course) {
        for (Grade g : Grade.values()) {
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

    private void noCourseTwicePerDay() {
        for (Course c : courses) {
            for (Day day : days) {
                if (day.grade7.count(c.code) > 1) {
                    errors.add(day.grade7.name + ": too many " + c.name + " classes"
                            + " on " + day.name);
                }

                if (day.grade8.count(c.code) > 1) {
                    errors.add(day.grade8.name + ": too many " + c.name + " classes"
                            + " on " + day.name);
                }

                if (day.grade9.count(c.code) > 1) {
                    errors.add(day.grade9.name + ": too many " + c.name + " classes"
                            + " on " + day.name);
                }
            }
        }
    }

    private void teacherOneSlotAtATime(Course c) {
        // French is a conference class
        if (c.code == 'F') {
            return;
        }
        for (Day day : days) {
            for (Period p : Period.values()) {
                int count = 0;
                for (Grade g : Grade.values()) {
                    GradeDay gd = day.getGradeDay(g);
                    if (gd.hasCourse(c.code, p)) {
                        count++;
                    }
                }
                if (count > 1) {
                    errors.add(c.name + " teacher in two slots at once on "
                            + day.name + ", " + p);
                }
            }
        }
    }

    private void teacherHasAtMostThreePeriodsPerDay() {
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

    private void frenchConferenceClass() {
        for (Day day : days) {
            for (Period p : Period.values()) {
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

    private void teacherDaysOff(Course course) {
        int daysOn = 0;
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

//    // schedules one period of c, if possible
//    private boolean scheduleOnePeriod(Course c, Grade g) throws Exception {
//        for (Day day : days) {
//            for (Period period : Period.values()) {
//                GradeDay gd = day.getGradeDay(g);
//                if (gd.get(period) == 0) {
//                    gd.set(period, c.code);
//                    errors.clear();
//                    validateMonotoneConstraints();
//                    if (errors.isEmpty()) {
//                        Slot slot = new Slot();
//                        slot.day = day;
//                        slot.gradeDay = gd;
//                        slot.period = period;
//                        history.push(slot);
//                        return true;
//                    } else {
//                        gd.clear(period);
//                    }
//                }
//            }
//        }
//        return false;
//    }
//    // whether c has been fully scheduled for grade
//    private boolean gradeComplete(Course c, Grade g) {
//        int periodsFound = 0;
//        for (Day day : days) {
//            GradeDay gd = day.getGradeDay(g);
//            if (gd.count(c.code) > 0) {
//                periodsFound++;
//            }
//        }
//        return (periodsFound >= c.periods);
//    }
}
