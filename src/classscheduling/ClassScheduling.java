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

    static State state;
    
    static Solver solver;

    /**
     * @param args the command line arguments
     * @throws Exception if something goes wrong
     */
    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown hook called.");
                state.print();
                solver.printStats();
            }
        });

        initState();
        solver = new Solver(state);
        if (solver.solve()) {
            System.out.println("Success!");
        } else {
            System.out.println("Failed.");
        }
        solver.printStats();
    }

    private static void initState() throws Exception {
        state = new State();

        state.setCourse(SEVEN, MONDAY, FIRST, MATH);
        state.setCourse(EIGHT, MONDAY, FOURTH, MATH);
        state.setCourse(NINE, MONDAY, SECOND, MATH);
    }

}
