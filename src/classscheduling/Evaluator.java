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
public class Evaluator {

    private static final int PERIODS = Period.values().length;

    private static boolean[] math;
    private static boolean[] english;
    private static boolean[] frenchGeo;
    private static boolean[] art;
    private static boolean[] music;

    static int evaluate(State state) {
        int result = 0;
        if (state.depth != 60) {
            return -1;
        }
        for (Day day : Day.values()) {
            clearSpares();
            for (Period period : Period.values()) {
                for (Grade grade : Grade.values()) {
                    char c = state.getCourse(grade, day, period);
                    Course course = Course.forCode(c);
                    mark(course, period);
                }
            }
            result += tally();
        }
        return result;
    }

    private static void clearSpares() {
        math = new boolean[PERIODS];
        english = new boolean[PERIODS];
        frenchGeo = new boolean[PERIODS];
        art = new boolean[PERIODS];
        music = new boolean[PERIODS];
    }

    private static void mark(Course course, Period period) {
        boolean[] mask;
        mask = courseToMask(course);
        mask[period.ordinal()] = true;
    }

    private static int tally() {
        int result = 0;
        for (Course course : Course.values()) {
            // don't count frenchGeo twice
            if (course.equals(FRENCH)) {
                continue;
            }
            result += tally(course);
        }
        return result;
    }

    private static int tally(Course course) {
        boolean atSchool = false;
        int potentialSpares = 0;
        int actualSpares = 0;
        boolean[] mask = courseToMask(course);
        for (Period period : Period.values()) {
            boolean hasClassInThisPeriod = mask[period.ordinal()];
            if (!atSchool && hasClassInThisPeriod) {
                atSchool = true;
            }
            if (atSchool && !hasClassInThisPeriod) {
                potentialSpares++;
            }
            if (atSchool && hasClassInThisPeriod) {
                actualSpares = potentialSpares;
            }
        }
        return actualSpares;
    }

    private static boolean[] courseToMask(Course course) {
        boolean[] mask = null;
        switch (course) {
            case MATH:
                mask = math;
                break;
            case ENGLISH:
                mask = english;
                break;
            case FRENCH:
            case GEOGRAPHY:
                mask = frenchGeo;
                break;
            case ART:
                mask = art;
                break;
            case MUSIC:
                mask = music;
        }
        return mask;
    }

}
