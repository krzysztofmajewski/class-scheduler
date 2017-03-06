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
import static classscheduling.Period.FOURTH;
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

        int iterations = 0;
        boolean result;
        MovesIterator iterator;
        do {
            iterations++;
            example = exampleSchedule();
            iterator = new MovesIterator(example, Course.MATH, 3);
            result = example.scheduleCourses(iterator);
            if (result) {
                break;
            }
            if (!iterator.takingTooLong()) {
                break;
            }
//            System.out.println();
//            example.print();
//            System.out.println("\n**** Search did not complete in time, trying endgame on best path found ****\n");
//            example = new Schedule(example.bestStateSeenSoFar);
//            iterator = new MovesIterator(example, Course.MATH);
//            result = example.scheduleCourses(iterator);
//            if (result) {
//                break;
//            }
//            if (!iterator.takingTooLong()) {
//                System.out.println("\n**** Endgame failed ****\n");
//                // this starting state did not lead to a win
//            } else {
//                System.out.println("\n**** Endgame did not complete in time ****\n");
//            }
//            MovesIterator.increaseThreshold();
//            System.out.println("Increasing search volume threshold to "
//                    + MovesIterator.BAD_MOVE_THRESHOLD);
        } while (iterations < MAX_ITERATIONS);

        if (result) {
            System.out.println("Success!");
        } else {
            System.out.println("Failed.");
        }
        example.validator.validate();
        // this should not print anything in case of success
        example.validator.printErrors();
        System.out.println(example.movesSeenInThisGame + " moves seen in this game");
        System.out.println(example.movesFailedVetting + " moves failed vetting in this game");
        System.out.println(example.hopelessPartialSchedules.numAdded + " partial schedules added to lookup table");
        System.out.println(example.hopelessPartialSchedules.numElements + " partial schedules remaining in lookup table");
    }

    private static Schedule exampleSchedule() throws Exception {
        Schedule schedule = new Schedule();

        schedule.set(MONDAY, SEVEN, FIRST, MATH);
        schedule.set(MONDAY, EIGHT, FOURTH, MATH);
        schedule.set(MONDAY, NINE, SECOND, MATH);

        return schedule;
    }

}
