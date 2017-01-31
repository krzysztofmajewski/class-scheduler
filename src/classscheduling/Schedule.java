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

    private final Day days[];

    private final ValidationErrors errors;

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

    Slot peek() {
        return history.peek();
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
    
    ValidationErrors validate() {
        errors.clear();
        for (Course c : courses) {
            validatePeriodsPerWeek(c);
        }
        return validateConstraints();
    }

    ValidationErrors validateConstraints() {
        errors.clear();
        noCourseTwicePerDay();
        noTeacherHasFourPeriodsPerDay();
        frenchConferenceClass();
        for (Course c : courses) {
            teacherDaysOff(c);
        }
        return errors;
    }

    // TODO: randomize?
    Course todo() {
        Course result = null;
        errors.clear();

        for (Course c : courses) {
            validatePeriodsPerWeek(c);
            if (!errors.isEmpty()) {
                return c;
            }
        }
        return result;
    }

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
    
    // assume the current configuration has no validation errors
    // this implies that c is not partially scheduled
    // TODO: randomize?
    boolean scheduleOneSlot(Course c) throws Exception {
        errors.clear();

        for (Grade g : Grade.values()) {
            if (!gradeComplete(c, g)) {
                return scheduleOnePeriod(c, g);
            }
        }
        // TODO: is this ever reached?
        return false;
    }

    // undo the previously scheduled slot
    void backTrack() throws Exception {
        Slot slot = history.pop();
        slot.gradeDay.clear(slot.period);
    }
    
    private void validatePeriodsPerWeek(Course course) {

        int grade7Count = 0;
        int grade8Count = 0;
        int grade9Count = 0;

        for (Day day : days) {
            grade7Count += day.grade7.count(course.code);
            grade8Count += day.grade8.count(course.code);
            grade9Count += day.grade9.count(course.code);
        }

        if (grade7Count != course.periods) {
            errors.add("Grade 7: expected " + course.periods + " periods of "
                    + course.name + ", found " + grade7Count);
        }

        if (grade8Count != course.periods) {
            errors.add("Grade 8: expected " + course.periods + " periods of "
                    + course.name + ", found " + grade8Count);
        }

        if (grade9Count != course.periods) {
            errors.add("Grade 9: expected " + course.periods + " periods of "
                    + course.name + ", found " + grade9Count);
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

    private void noTeacherHasFourPeriodsPerDay() {
        for (Day day : days) {
            if (day.count('M') > 3) {
                errors.add("Math teacher has too many periods on "
                        + day.name);
            }
            if (day.count('E') > 3) {
                errors.add("English teacher has too many periods on "
                        + day.name);
            }
            int frenchAndGeoPeriods = day.count('F') + day.count('G');
            if (frenchAndGeoPeriods > 3) {
                errors.add("French/Geography teacher has too many periods on "
                        + day.name);
            }
            if (day.count('A') > 3) {
                errors.add("Art teacher has too many periods on "
                        + day.name);
            }
            if (day.count('U') > 3) {
                errors.add("Music teacher has too many periods on "
                        + day.name);
            }
        }
    }

    private void frenchConferenceClass() {
        for (Day day : days) {
            for (int period = 1; period <= GradeDay.PERIODS_PER_DAY; period++) {
                if (!allGradesHaveFrench(day, period)
                        && !noGradesHaveFrench(day, period)) {
                    errors.add("French class in period " + period
                            + " on " + day.name + " must be shared by all grades");
                }
            }
        }
    }

    private boolean allGradesHaveFrench(Day day, int period) {
        return day.grade7.hasCourse('F', period)
                && day.grade8.hasCourse('F', period)
                && day.grade9.hasCourse('F', period);
    }

    private boolean noGradesHaveFrench(Day day, int period) {
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

    // schedules one period of c, if possible
    private boolean scheduleOnePeriod(Course c, Grade g) throws Exception {
        for (Day day : days) {
            for (Period period : Period.values()) {
                GradeDay gd = day.getGradeDay(g);
                if (gd.get(period) == 0) {
                    gd.set(period, c.code);
                    errors.clear();
                    validateConstraints();
                    if (errors.isEmpty()) {
                        Slot slot = new Slot();
                        slot.day = day;
                        slot.gradeDay = gd;
                        slot.period = period;
                        history.push(slot);
                        return true;
                    } else {
                        gd.clear(period);
                    }
                }
            }
        }
        return false;
    }

    // whether c has been fully scheduled for grade
    private boolean gradeComplete(Course c, Grade g) {
        int periodsFound = 0;
        for (Day day : days) {
            GradeDay gd = day.getGradeDay(g);
            if (gd.count(c.code) > 0) {
                periodsFound++;
            }
        }
        return (periodsFound >= c.periods);
    }

    int freeSlots() {
        return getEmptySlots().size();
    }

    void print() {
        for (Grade g : Grade.values()) {
            System.out.print(g.name() + " |");
            for (Day day : days) {
                for (Period period : Period.values()) {
                    System.out.print(day.getGradeDay(g).get(Period.SECOND));
                }
                System.out.print("|");
            }
            System.out.println();
        }
    }

}
