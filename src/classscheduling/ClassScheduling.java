/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Course.MATH;
import static classscheduling.Day.MONDAY;
import static classscheduling.Grade.EIGHT;
import static classscheduling.Grade.NINE;
import static classscheduling.Grade.SEVEN;
import static classscheduling.Period.FIRST;
import static classscheduling.Period.SECOND;
import static classscheduling.Period.FOURTH;

/**
 *
 * @author krzys
 */
public class ClassScheduling {

    private static Slot lastFilledSlot;

    /**
     * @param args the command line arguments
     * @throws Exception if something goes wrong
     */
    public static void main(String[] args) throws Exception {

        Schedule example = exampleSchedule();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println(example.movesTried + " moves tried");
                System.out.println(example.history.size() + " moves in history");
                example.print();
            }
        });
                
        if (example.fillSchedule(lastFilledSlot, null, 0)) {
            System.out.println("success!");
            example.print();
        } else {
            System.out.println("failed.");
        }
        example.errors.clear();
        example.validate();
        // this should not print anything in case of success
        example.errors.print();
    }

    private static Schedule exampleSchedule() throws Exception {
        Schedule schedule = new Schedule();

        schedule.set(MONDAY, SEVEN, FIRST, MATH);
        schedule.set(MONDAY, EIGHT, FOURTH, MATH);
        lastFilledSlot = schedule.set(MONDAY, NINE, SECOND, MATH);
        
        return schedule;
    }

}
