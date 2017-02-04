/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Course.FRENCH;
import static classscheduling.Course.GEOGRAPHY;

/**
 *
 * @author krzys
 */
public class ScheduleValidator {

    private final Schedule schedule;

    public ScheduleValidator(Schedule schedule) {
        this.schedule = schedule;
    }

    void validateCorrectnessConstraints() {
        noCourseTwicePerDay();
        teacherHasAtMostThreePeriodsPerDay();
        conflictsWithConference();
        notTooManyPeriodsPerWeek();
        teacherDaysOff();
        teacherOneSlotAtATime();
    }

    void validateCompletenessConstraints() {
        frenchConferenceClassComplete();
        enoughPeriodsPerWeek();
    }

    // once violated, cannot be un-violated by filling more slots
    boolean validateCorrectnessConstraints(Slot slot) throws SanityCheckException {
        if (slot == null) {
            // this should only happen if we just started
            if (schedule.movesTried != 1) {
                throw new SanityCheckException("slot should not be null");
            }
            return true;
        }
        boolean result = noCourseTwicePerDay(slot)
                && teacherHasAtMostThreePeriodsPerDay(slot)
                && conflictsWithConference(slot)
                && notTooManyPeriodsPerWeek(slot)
                && teacherDaysOff(slot)
                && teacherOneSlotAtATime(slot);
        return result;
    }

    // once violated, can be un-violated by filling more slots
    void validateCompletenessConstraints(Slot slot) {
        frenchConferenceClassComplete(slot);
        enoughPeriodsPerWeek(slot);
    }

    private boolean enoughPeriodsPerWeek() {
        for (Course course : Course.values()) {
            boolean ok = schedule.enoughPeriodsPerWeek(course);
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    private boolean enoughPeriodsPerWeek(Slot slot) {
        char c = slot.gradeDay.get(slot.period);
        Course course = Course.forCode(c);
        return schedule.enoughPeriodsPerWeek(course);
    }

    private boolean notTooManyPeriodsPerWeek() {
        for (Course course : Course.values()) {
            for (Grade g : Grade.values()) {
                boolean ok = notTooManyPeriodsPerWeek(g, course);
                if (!ok) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean notTooManyPeriodsPerWeek(Slot slot) {
        GradeDay gd = slot.gradeDay;
        char c = gd.get(slot.period);
        Course course = Course.forCode(c);
        return notTooManyPeriodsPerWeek(gd.grade, course);
    }

    private boolean notTooManyPeriodsPerWeek(Grade grade, Course course) {
        if (course.getPeriodsScheduled(grade) > course.periods) {
            schedule.errors.add(grade + ": too many periods of " + course.name);
            return false;
        }
        return true;
//        int gradeCount = 0;
//        for (Day day : Day.values()) {
//            GradeDay gd = day.getGradeDay(grade);
//            gradeCount += gd.count(course.code);
//        }
//        if (gradeCount > course.periods) {
//            schedule.errors.add(grade + ": too many periods of " + course.name);
//            return false;
//        }
//        return true;
    }

    private boolean noCourseTwicePerDay() {
        for (Course c : Course.values()) {
            for (Day day : Day.values()) {
                for (Grade g : Grade.values()) {
                    GradeDay gd = day.getGradeDay(g);
                    boolean ok = noCourseTwicePerDay(c, day, gd);
                    if (!ok) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean noCourseTwicePerDay(Slot slot) {
        GradeDay gd = slot.gradeDay;
        char c = gd.get(slot.period);
        Course slotCourse = Course.forCode(c);
        return noCourseTwicePerDay(slotCourse, slot.day, gd);
    }

    private boolean noCourseTwicePerDay(Course c, Day day, GradeDay gd) {
        if (gd.count(c.code) > 1) {
            schedule.errors.add(gd.grade.name() + ": too many " + c.name + " classes"
                    + " on " + day.name);
            return false;
        }
        return true;
    }

    private boolean teacherHasAtMostThreePeriodsPerDay() {
        for (Day day : Day.values()) {
            for (Course c : Course.values()) {
                boolean ok = teacherHasAtMostThreePeriodsPerDay(day, c);
                if (!ok) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean teacherHasAtMostThreePeriodsPerDay(Slot slot) {
        char c = slot.gradeDay.get(slot.period);
        Course course = Course.forCode(c);
        return teacherHasAtMostThreePeriodsPerDay(slot.day, course);
    }

    private boolean teacherHasAtMostThreePeriodsPerDay(Day day, Course course) {
        if (!course.equals(FRENCH) && !course.equals(GEOGRAPHY)) {
            if (day.count(course.code) > 3) {
                schedule.errors.add(course.name + " teacher has too many periods on "
                        + day.name);
                return false;
            }
        } else {
            int frenchAndGeoPeriods = day.count('F') + day.count('G');
            if (frenchAndGeoPeriods > 3) {
                schedule.errors.add("French/Geography teacher has too many periods on "
                        + day.name);
                return false;
            }
        }
        return true;
    }

    // possibly redundant?
    private boolean frenchConferenceClassComplete() {
        for (Day day : Day.values()) {
            for (Period period : Period.values()) {
                boolean ok = frenchConferenceClassComplete(day, period);
                if (!ok) {
                    return false;
                }
            }
        }
        return true;
    }

// completeness
    private boolean frenchConferenceClassComplete(Slot slot) {
        return frenchConferenceClassComplete(slot.day, slot.period);
    }

    private boolean frenchConferenceClassComplete(Day day, Period period) {
        if (allGradesHaveFrench(day, period) || noGradesHaveFrench(day, period)) {
            return true;
        }
        schedule.errors.add("French class in " + period + " on " + day
                + " must be shared by all grades");
        return false;
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

    private boolean teacherDaysOff() {
        for (Course course : Course.values()) {
            boolean ok = teacherDaysOff(course);
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    private boolean teacherDaysOff(Slot slot) {
        char c = slot.gradeDay.get(slot.period);
        Course course = Course.forCode(c);
        return teacherDaysOff(course);
    }

    // TODO: optimize by caching this?
    private boolean teacherDaysOff(Course course) {
        int daysOn = 0;
        for (Day day : Day.values()) {
            if (day.count(course.code) > 0) {
                daysOn++;
            }
        }
        boolean enoughDaysOff = ((5 - daysOn) >= course.daysOff);
        if (!enoughDaysOff) {
            schedule.errors.add("Teacher of " + course + " has only "
                    + (5 - daysOn) + " days off instead of " + course.daysOff);
            return false;
        }
        return true;
    }

    private boolean teacherOneSlotAtATime() {
        for (Day day : Day.values()) {
            for (Period period : Period.values()) {
                for (Course course : Course.values()) {
                    boolean ok = teacherOneSlotAtATime(course, day, period);
                    if (!ok) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean teacherOneSlotAtATime(Slot slot) {
        // TODO use this everywhere
        Course course = slot.getCourse();
        return teacherOneSlotAtATime(course, slot.day, slot.period);
    }

    private boolean teacherOneSlotAtATime(Course course, Day day, Period p) {
        // French is a conference class
        if (course.code == 'F') {
            return true;
        }
        int count = 0;
        for (Grade g : Grade.values()) {
            GradeDay gd = day.getGradeDay(g);
            if (gd.hasCourse(course.code, p)) {
                count++;
            }
        }
        if (count > 1) {
            schedule.errors.add(course.name + " teacher in two slots at once on "
                    + day.name + ", " + p);
            return false;
        }
        return true;
    }

    private boolean conflictsWithConference() {
        for (Day day : Day.values()) {
            for (Period period : Period.values()) {
                boolean ok = conflictsWithConference(day, period);
                if (!ok) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean conflictsWithConference(Slot slot) {
        return conflictsWithConference(slot.day, slot.period);
    }

    // correctness check only, not completeness
    private boolean conflictsWithConference(Day day, Period period) {
        int frenchClasses = 0;
        int otherClasses = 0;

        for (Grade g : Grade.values()) {
            GradeDay gd = day.getGradeDay(g);
            // TODO: use this everywhere
            Course course = gd.getCourse(period);
            if (course == null) {
                continue;
            }
            if (course.equals(FRENCH)) {
                frenchClasses++;
            } else {
                otherClasses++;
            }
        }

        if (frenchClasses == 0) {
            return true;
        }
        if (otherClasses == 0) {
            return true;
        }
        schedule.errors.add("conflict with conference class in " + period
                + " on " + day);
        return false;
    }

}
