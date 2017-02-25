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

/**
 *
 * @author krzys
 */
public class ClassScheduling {

    static final long MAX_ITERATIONS = 1;

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
        int iterations = 1;
        while (!result && iterator.takingTooLong() && (iterations < MAX_ITERATIONS)) {
            iterations++;
            System.out.println();
            example.print();
            System.out.println("\n**** Search did not complete in time. ****\n");
            MovesIterator.increaseThreshold();
            if (MovesIterator.BAD_MOVE_THRESHOLD > MAX_ITERATIONS) {
                break;
            }
            System.out.println("Increasing search volume threshold to "
            + MovesIterator.BAD_MOVE_THRESHOLD);
            example = exampleSchedule();
            iterator = new MovesIterator(example, Course.MATH);
            result = example.scheduleCourses(iterator);
        }
        if (result) {
            System.out.println("Success!");
        } else {
            System.out.println("Failed.");
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
