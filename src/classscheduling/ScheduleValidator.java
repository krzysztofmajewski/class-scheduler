/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Course.FRENCH;
import static classscheduling.Course.GEOGRAPHY;
import static classscheduling.Grade.EIGHT;
import static classscheduling.Grade.NINE;
import static classscheduling.Grade.SEVEN;

/**
 *
 * @author krzys
 */
public class ScheduleValidator {

    static boolean trackErrors;

    // in case we decide to shorten the week for testing purposes
    private static final int FIVE = Day.values().length;

    private final ValidationErrors errors;

    private boolean validationFailed;

    private  final Schedule schedule;

    public ScheduleValidator(Schedule schedule) {
        this.schedule = schedule;
        errors = new ValidationErrors();
    }

    void reset() {
        errors.clear();
        validationFailed = false;
    }

    void printErrors() {
        errors.print();
    }

    boolean hasNoErrors() throws SanityCheckException {
        boolean result = !validationFailed;
        return result;
    }

    boolean hasErrors() throws SanityCheckException {
        boolean result = validationFailed;
        return result;
    }

    void validate() {
        reset();
        trackErrors = true;
        validateCorrectnessConstraints();
        validateCompletenessConstraints();
        trackErrors = false;
    }

    // once violated, cannot be un-violated by filling more slots
    boolean validateCorrectnessConstraints(Move move) throws SanityCheckException {
        if (move == null) {
            // this should only happen if we just started
            return true;
        }
        boolean result = noCourseTwicePerDay(move)
                && teacherHasAtMostThreePeriodsPerDay(move)
                && conflictsWithConference(move)
                && notTooManyPeriodsPerWeek(move)
                && teacherDaysOff(move)
                && teacherOneSlotAtATime(move);
        return result;
    }

    private void validateCorrectnessConstraints() {
        noCourseTwicePerDay();
        teacherHasAtMostThreePeriodsPerDay();
        conflictsWithConference();
        notTooManyPeriodsPerWeek();
        teacherDaysOff();
        teacherOneSlotAtATime();
    }

    private void validateCompletenessConstraints() {
        frenchConferenceClassComplete();
        enoughPeriodsPerWeek();
    }

    private boolean enoughPeriodsPerWeek() {
        boolean result = true;
        for (Course course : Course.values()) {
            boolean ok = enoughPeriodsPerWeek(course);
            if (!ok) {
                result = false;
            }
        }
        return result;
    }

    private boolean enoughPeriodsPerWeek(Course course) {
        boolean result = true;
        for (Grade grade : Grade.values()) {
            if (course.count(grade) < course.periods) {
//                if (trackErrors) {
//                    errors.add(g + ": not enough periods of " + course.name);
//                }
                validationFailed = true;
                result = false;
            }
        }
        return result;
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

    private boolean notTooManyPeriodsPerWeek(Move move) {
        return notTooManyPeriodsPerWeek(move.slot.grade, move.course);
    }

    private boolean notTooManyPeriodsPerWeek(Grade grade, Course course) {
        if (course.count(grade) > course.periods) {
//            if (trackErrors) {
//                errors.add(grade + ": too many periods of " + course.name);
//            }
            validationFailed = true;
            return false;
        }
        return true;
    }

    private boolean noCourseTwicePerDay() {
        for (Course course : Course.values()) {
            for (Day day : Day.values()) {
                for (Grade grade : Grade.values()) {
                    boolean ok = noCourseTwicePerDay(course, day, grade);
                    if (!ok) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean noCourseTwicePerDay(Move move) {
        return noCourseTwicePerDay(move.course, move.slot.day, move.slot.grade);
    }

    private boolean noCourseTwicePerDay(Course course, Day day, Grade grade) {
        if (course.count(grade, day) > 1) {
//            if (trackErrors) {
//                errors.add(gd.grade.name() + ": too many " + c.name + " classes"
//                        + " on " + day.name);
//            }
            validationFailed = true;
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

    private boolean teacherHasAtMostThreePeriodsPerDay(Move move) {
        return teacherHasAtMostThreePeriodsPerDay(move.slot.day, move.course);
    }

    private boolean teacherHasAtMostThreePeriodsPerDay(Day day, Course course) {
        if (!course.equals(FRENCH) && !course.equals(GEOGRAPHY)) {
            if (course.count(day) > 3) {
//                if (trackErrors) {
//                    errors.add(course.name + " teacher has too many periods on "
//                            + day.name);
//                }
                validationFailed = true;
                return false;
            }
        } else {
            int frenchAndGeoPeriods = FRENCH.count(day) + GEOGRAPHY.count(day);
            if (frenchAndGeoPeriods > 3) {
//                if (trackErrors) {
//                    errors.add("French/Geography teacher has too many periods on "
//                            + day.name);
//                }
                validationFailed = true;
                return false;
            }
        }
        return true;
    }

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

    private boolean frenchConferenceClassComplete(Day day, Period period) {
        int numGradesWithFrenchAtThisTime = 0;
        for (Grade grade : Grade.values()) {
            if (schedule.isScheduled(FRENCH, grade, day, period)) {
                numGradesWithFrenchAtThisTime++;
            }
        }
        // return true if 0 or 3 grades have French at this time
        if ((numGradesWithFrenchAtThisTime % Grade.values().length) == 0) {
            return true;
        }
//        if (trackErrors) {
//            errors.add("French class in " + period + " on " + day
//                    + " must be shared by all grades");
//        }
        validationFailed = true;
        return false;
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

    private boolean teacherDaysOff(Move move) {
        return teacherDaysOff(move.course);
    }

    // TODO: optimize by caching this?
    private boolean teacherDaysOff(Course course) {
        int daysOn = 0;
        for (Day day : Day.values()) {
            if (course.count(day) > 0) {
                daysOn++;
            }
        }
        boolean enoughDaysOff = ((FIVE - daysOn) >= course.daysOff);
        if (!enoughDaysOff) {
//            if (trackErrors) {
//                errors.add("Teacher of " + course + " has only "
//                        + (FIVE - daysOn) + " days off instead of " + course.daysOff);
//            }
            validationFailed = true;
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

    private boolean teacherOneSlotAtATime(Move move) {
        return teacherOneSlotAtATime(move.course, move.slot.day, move.slot.period);
    }

    private boolean teacherOneSlotAtATime(Course course, Day day, Period period) {
        // French is a conference class
        if (course.equals(FRENCH)) {
            return true;
        }
        int count = 0;
        for (Grade grade : Grade.values()) {
            if (schedule.isScheduled(course, grade, day, period)) {
                count++;
            }
        }
        if (count > 1) {
//            if (trackErrors) {
//                errors.add(course.name + " teacher in two slots at once on "
//                        + day.name + ", " + p);
//            }
            validationFailed = true;
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

    private boolean conflictsWithConference(Move move) {
        return conflictsWithConference(move.slot.day, move.slot.period);
    }

    // correctness check only, not completeness
    private boolean conflictsWithConference(Day day, Period period) {
        int frenchClasses = 0;
        int otherClasses = 0;

        for (Grade grade : Grade.values()) {
            Course course = schedule.getCourse(grade, day, period);
//            GradeDay gd = day.getGradeDay(grade);
//            Course course = gd.getCourse(period);
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
//        if (trackErrors) {
//            errors.add("conflict with conference class in " + period
//                    + " on " + day);
//        }
        validationFailed = true;
        return false;
    }

}
