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
public class Util {

    static int computeIndex(Grade grade, Day day, Period period) {
        int index = grade.ordinal() * Day.values().length * Period.values().length
                + day.ordinal() * Period.values().length
                + period.ordinal();
        return index;
    }

}
