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
                System.out.println("Shutdown hook called.");
                example.print();
            }
        });

        example = exampleSchedule();
        MovesIterator iterator = new MovesIterator(example, Course.MATH);
        boolean result = example.scheduleCourses(iterator);
        if (result) {
            System.out.println("Success!");
        } else {
            System.out.println("Failed.");
        }
        example.validator.validate();
        // this should not print anything in case of success
        example.validator.printErrors();
        System.out.println(example.movesSeenInThisGame
                + " moves seen in this game");
        System.out.println(example.movesFailedVetting
                + " moves failed vetting in this game");
        System.out.println(example.hopelessPartialSchedules.numAdded
                + " partial schedules added to lookup table");
        System.out.println(example.hopelessPartialSchedules.numElements
                + " partial schedules remaining in lookup table");
    }

    private static Schedule exampleSchedule() throws Exception {
        Schedule schedule = new Schedule();

//        schedule.set(MONDAY, SEVEN, FIRST, MATH);
//        schedule.set(MONDAY, EIGHT, FOURTH, MATH);
        schedule.set(MONDAY, NINE, SECOND, MATH);

        return schedule;
    }

}
