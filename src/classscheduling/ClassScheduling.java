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
public class ClassScheduling {

    /**
     * @param args the command line arguments
     * @throws Exception if something goes wrong
     */
    public static void main(String[] args) throws Exception {
        Schedule example = exampleSchedule();
        Course c = example.todo();
        while (c != null) {
            if (!example.scheduleOneSlot(c)) {
                example.backTrack();
                System.out.println("pop");
            } else {
                Slot top = example.peek();
                System.out.println(top);
            }
            c = example.todo();
        }
//        ValidationErrors errors = example.validate();
//        errors.print();
    }

    private static Schedule exampleSchedule() throws Exception {
        Schedule schedule = new Schedule();

        // Monday
        schedule.monday.grade7.set(1, 'M');
        schedule.monday.grade7.set(2, 'E');
        schedule.monday.grade7.set(3, 'F');

        schedule.monday.grade8.set(1, 'E');
        schedule.monday.grade8.set(3, 'F');
        schedule.monday.grade8.set(4, 'M');

        schedule.monday.grade9.set(2, 'M');
        schedule.monday.grade9.set(3, 'F');
        schedule.monday.grade9.set(4, 'E');

        // Tuesday
        schedule.tuesday.grade7.set(1, 'M');
        schedule.tuesday.grade7.set(2, 'E');
        schedule.tuesday.grade7.set(3, 'F');

        schedule.tuesday.grade8.set(1, 'E');
        schedule.tuesday.grade8.set(3, 'F');
        schedule.tuesday.grade8.set(4, 'M');

        schedule.tuesday.grade9.set(2, 'M');
        schedule.tuesday.grade9.set(3, 'F');
        schedule.tuesday.grade9.set(4, 'E');

        // Wednesday
        schedule.wednesday.grade7.set(1, 'M');
        schedule.wednesday.grade7.set(2, 'E');
        schedule.wednesday.grade7.set(3, 'F');
        schedule.wednesday.grade7.set(4, 'U');

        schedule.wednesday.grade8.set(1, 'E');
        schedule.wednesday.grade8.set(2, 'U');
        schedule.wednesday.grade8.set(3, 'F');
        schedule.wednesday.grade8.set(4, 'M');

        schedule.wednesday.grade9.set(1, 'U');
        schedule.wednesday.grade9.set(2, 'M');
        schedule.wednesday.grade9.set(3, 'F');
        schedule.wednesday.grade9.set(4, 'E');

        // Thursday
        schedule.thursday.grade7.set(1, 'M');
        schedule.thursday.grade7.set(2, 'E');
        schedule.thursday.grade7.set(3, 'F');
        schedule.thursday.grade7.set(4, 'U');

        schedule.thursday.grade8.set(1, 'E');
        schedule.thursday.grade8.set(2, 'U');
        schedule.thursday.grade8.set(3, 'F');
        schedule.thursday.grade8.set(4, 'M');

        schedule.thursday.grade9.set(1, 'U');
        schedule.thursday.grade9.set(2, 'M');
        schedule.thursday.grade9.set(3, 'F');
        schedule.thursday.grade9.set(4, 'E');

        // Friday
        schedule.friday.grade7.set(1, 'U');
        schedule.friday.grade7.set(3, 'F');

        schedule.friday.grade8.set(2, 'U');
        schedule.friday.grade8.set(3, 'F');

        schedule.friday.grade9.set(3, 'F');
        schedule.friday.grade9.set(4, 'U');

        return schedule;
    }

}
