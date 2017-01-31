/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

/**
 *
 * @author krzys
 */
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

    private final Day days[];
    
    private final Course courses[];

    private final ValidationErrors errors;

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
    }

    ValidationErrors validate() {
        for (Course c : courses) {
            validatePeriodsPerWeek(c);
        }
        noCourseTwicePerDay();
        noTeacherHasFourPeriodsPerDay();
        frenchConferenceClass();
        for (Course c : courses) {
            teacherDaysOff(c);
        }
        return errors;
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
                    errors.add("Grade 7: too many " + c.name + " classes"
                            + " on " + day.name);
                }

                if (day.grade8.count(c.code) > 1) {
                    errors.add("Grade 8: too many " + c.name + " classes"
                            + " on " + day.name);
                }

                if (day.grade9.count(c.code) > 1) {
                    errors.add("Grade 9: too many " + c.name + " classes"
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
            for (int period = 1; period <= Grade.PERIODS_PER_DAY; period++) {
                if (!allGradesHaveFrench(day, period)
                        && !noGradesHaveFrench(day, period))  {
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

    boolean scheduleCourse(Course c) {
        return false;
    }

}
