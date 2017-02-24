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
import static classscheduling.YesNoMaybe.MAYBE;

/**
 *
 * @author krzys
 */
public class ClassScheduling {

    static Schedule example;

    /**
     * @param args the command line arguments
     * @throws Exception if something goes wrong
     */
    public static void main(String[] args) throws Exception {

        example = exampleSchedule();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
//                System.out.println(example.legalMovesTried + " legal moves tried");
                example.print();
            }
        });
        MovesIterator iterator = new MovesIterator(example, Course.MATH);
        boolean result = example.scheduleCourses(iterator);
        while (!result && iterator.takingTooLong()) {
            System.out.println();
            example.print();
            System.out.println("\n**** Search did not complete in time. ****\n");
            System.out.print("Increasing search volume threshold to ");
            example = exampleSchedule();
            iterator = new MovesIterator(example, Course.MATH);
            MovesIterator.increaseThreshold();
            System.out.println(MovesIterator.VOLUME_THRESHOLD);
            result = example.scheduleCourses(iterator);
        }
        if (result) {
            System.out.println("Success!");
        } else {
            System.out.println("Failed.");
        }
        example.print();
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
