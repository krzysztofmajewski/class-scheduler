/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Period.FIRST;
import static classscheduling.Period.SECOND;
import static classscheduling.Period.FOURTH;

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
                System.out.println(example.movesTried + " moves tried");
                System.out.println(example.history.size() + " moves in history");
                example.print();
            }
        });

        if (example.fillSchedule(null)) {
            System.out.println("success!");
            example.print();
        } else {
            System.out.println("failed.");
        }
        example.errors.clear();
        example.errors.strict = false;
        example.validate();
        // this should not print anything in case of success
        example.errors.print();
    }

    private static Schedule exampleSchedule() throws Exception {
        Schedule schedule = new Schedule();

        // Monday
        schedule.monday.grade7.set(FIRST, 'M');
        schedule.monday.grade7.set(SECOND, 'E');
//        schedule.monday.grade7.set(THIRD, 'F');

        schedule.monday.grade8.set(FIRST, 'E');
//        schedule.monday.grade8.set(THIRD, 'F');
        schedule.monday.grade8.set(FOURTH, 'M');

        schedule.monday.grade9.set(SECOND, 'M');
//        schedule.monday.grade9.set(THIRD, 'F');
        schedule.monday.grade9.set(FOURTH, 'E');

        // Tuesday
        schedule.tuesday.grade7.set(FIRST, 'M');
        schedule.tuesday.grade7.set(SECOND, 'E');
//        schedule.tuesday.grade7.set(THIRD, 'F');

        schedule.tuesday.grade8.set(FIRST, 'E');
//        schedule.tuesday.grade8.set(THIRD, 'F');
        schedule.tuesday.grade8.set(FOURTH, 'M');

        schedule.tuesday.grade9.set(SECOND, 'M');
//        schedule.tuesday.grade9.set(THIRD, 'F');
        schedule.tuesday.grade9.set(FOURTH, 'E');

        // Wednesday
        schedule.wednesday.grade7.set(FIRST, 'M');
        schedule.wednesday.grade7.set(SECOND, 'E');
//        schedule.wednesday.grade7.set(THIRD, 'F');
        schedule.wednesday.grade7.set(FOURTH, 'U');

        schedule.wednesday.grade8.set(FIRST, 'E');
        schedule.wednesday.grade8.set(SECOND, 'U');
//        schedule.wednesday.grade8.set(THIRD, 'F');
        schedule.wednesday.grade8.set(FOURTH, 'M');

        schedule.wednesday.grade9.set(FIRST, 'U');
        schedule.wednesday.grade9.set(SECOND, 'M');
//        schedule.wednesday.grade9.set(THIRD, 'F');
        schedule.wednesday.grade9.set(FOURTH, 'E');
        
        // Thursday
        schedule.thursday.grade7.set(FIRST, 'M');
        schedule.thursday.grade7.set(SECOND, 'E');
//        schedule.thursday.grade7.set(THIRD, 'F');
        schedule.thursday.grade7.set(FOURTH, 'U');

        schedule.thursday.grade8.set(FIRST, 'E');
        schedule.thursday.grade8.set(SECOND, 'U');
//        schedule.thursday.grade8.set(THIRD, 'F');
        schedule.thursday.grade8.set(FOURTH, 'M');

        schedule.thursday.grade9.set(FIRST, 'U');
        schedule.thursday.grade9.set(SECOND, 'M');
//        schedule.thursday.grade9.set(THIRD, 'F');
        schedule.thursday.grade9.set(FOURTH, 'E');
        
        // Friday
        schedule.friday.grade7.set(FIRST, 'U');
//        schedule.friday.grade7.set(THIRD, 'F');

        schedule.friday.grade8.set(SECOND, 'U');
//        schedule.friday.grade8.set(THIRD, 'F');

//        schedule.friday.grade9.set(THIRD, 'F');
        schedule.friday.grade9.set(FOURTH, 'U');
        return schedule;
    }

}
