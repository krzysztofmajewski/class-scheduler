/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Course.MATH;
import static classscheduling.Day.MONDAY;
import static classscheduling.Grade.NINE;
import static classscheduling.Period.SECOND;
import static classscheduling.Schedule.MILLION;

/**
 *
 * @author krzys
 */
public class ClassScheduling {

    /**
     * @param args the command line arguments
     * @throws Exception if something goes wrong
     */
    public static void main(String[] args) throws Exception {

        Schedule example = exampleSchedule();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println(example.movesTried / MILLION + " million legal moves tried");
                example.print();
            }
        });
        MovesIterator iterator = new MovesIterator(example, Course.MATH);
        if (example.scheduleCourses(iterator)) {
            System.out.println("success!");
        } else {
            System.out.println("failed.");
        }
        example.validator.validate();
        // this should not print anything in case of success
        example.validator.printErrors();
    }

    private static Schedule exampleSchedule() throws Exception {
        Schedule schedule = new Schedule();

//        schedule.set(MONDAY, SEVEN, FIRST, MATH);
//        schedule.set(MONDAY, EIGHT, FOURTH, MATH);
        schedule.set(MONDAY, NINE, SECOND, MATH);

        return schedule;
    }

}
