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
public enum Course {

    // order matters!   
    MATH("Math", 'M', 4, 1),
    FRENCH("French", 'F', 5, 0),
    GEOGRAPHY("Geography", 'G', 2, 0),
    ART("Art", 'A', 2, 2),
    ENGLISH("English", 'E', 4, 1),
    MUSIC("Music", 'U', 3, 1);

    final String name;

    final char code;
    final int periods;
    final int daysOff;

    private final int numPeriodsScheduledPerGrade[];
    private final int numPeriodsScheduledPerDay[];
    private final int numPeriodsScheduledPerGradePerDay[];

    private Course(String name, char code, int periods, int daysOff) {
        this.name = name;
        this.code = code;
        this.periods = periods;
        this.daysOff = daysOff;
        numPeriodsScheduledPerGrade = new int[Grade.values().length];
        numPeriodsScheduledPerDay = new int[Day.values().length];
        numPeriodsScheduledPerGradePerDay = new int[Grade.values().length * Day.values().length];
    }

    public static Course forCode(char c) {
        switch (c) {
            case 'M':
                return MATH;
            case 'E':
                return ENGLISH;
            case 'F':
                return FRENCH;
            case 'G':
                return GEOGRAPHY;
            case 'A':
                return ART;
            case 'U':
                return MUSIC;
            default:
                return null;
        }
    }

    void incrementPeriodsScheduled(Grade grade, Day day) {
        final int gradeIndex = grade.ordinal();
        numPeriodsScheduledPerGrade[gradeIndex] = numPeriodsScheduledPerGrade[gradeIndex] + 1;
        final int dayIndex = day.ordinal();
        numPeriodsScheduledPerDay[dayIndex] = numPeriodsScheduledPerDay[dayIndex] + 1;
        int gradeDayIndex = computeIndex(grade, day);
        numPeriodsScheduledPerGradePerDay[gradeDayIndex] = numPeriodsScheduledPerGradePerDay[gradeDayIndex] + 1;
    }

    void incrementFrenchPeriodsScheduled(Day day) {
        final int dayIndex = day.ordinal();
        numPeriodsScheduledPerDay[dayIndex] = numPeriodsScheduledPerDay[dayIndex] + 1;
    }

    void decrementFrenchPeriodsScheduled(Day day) {
        final int dayIndex = day.ordinal();
        numPeriodsScheduledPerDay[dayIndex] = numPeriodsScheduledPerDay[dayIndex] - 1;
    }

    void incrementFrenchPeriodsScheduled(Grade grade, Day day) {
        final int gradeIndex = grade.ordinal();
        numPeriodsScheduledPerGrade[gradeIndex] = numPeriodsScheduledPerGrade[gradeIndex] + 1;
        int gradeDayIndex = computeIndex(grade, day);
        numPeriodsScheduledPerGradePerDay[gradeDayIndex] = numPeriodsScheduledPerGradePerDay[gradeDayIndex] + 1;
    }

    void decrementFrenchPeriodsScheduled(Grade grade, Day day) {
        final int gradeIndex = grade.ordinal();
        numPeriodsScheduledPerGrade[gradeIndex] = numPeriodsScheduledPerGrade[gradeIndex] - 1;
        int gradeDayIndex = computeIndex(grade, day);
        numPeriodsScheduledPerGradePerDay[gradeDayIndex] = numPeriodsScheduledPerGradePerDay[gradeDayIndex] - 1;
    }

    void decrementPeriodsScheduled(Grade grade, Day day) {
        int gradeIndex = grade.ordinal();
        numPeriodsScheduledPerGrade[gradeIndex] = numPeriodsScheduledPerGrade[gradeIndex] - 1;
        final int dayIndex = day.ordinal();
        numPeriodsScheduledPerDay[dayIndex] = numPeriodsScheduledPerDay[dayIndex] - 1;
        int gradeDayIndex = computeIndex(grade, day);
        numPeriodsScheduledPerGradePerDay[gradeDayIndex] = numPeriodsScheduledPerGradePerDay[gradeDayIndex] - 1;
    }

    int count(Grade grade) {
        return numPeriodsScheduledPerGrade[grade.ordinal()];
    }

    int count(Day day) {
        return numPeriodsScheduledPerDay[day.ordinal()];
    }

    int count(Grade grade, Day day) {
        int index = computeIndex(grade, day);
        return numPeriodsScheduledPerGradePerDay[index];
    }

    boolean enoughPeriodsPerWeek() {
        for (Grade grade : Grade.values()) {
            if (count(grade) < periods) {
                return false;
            }
        }
        return true;
    }

    private int computeIndex(Grade grade, Day day) {
        return grade.ordinal() * Day.values().length + day.ordinal();
    }
    
}
