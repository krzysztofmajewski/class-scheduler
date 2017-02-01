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
public class Course {

    String name;
    char code;
    int periods;
    int daysOff = 0;
    
    private Course() {
        
    }

    public static Course Math() {
        Course m = new Course();
        m.name = "Math";
        m.code = 'M';
        m.periods = 4;
        m.daysOff = 1;
        return m;
    }

    public static Course English() {
        Course e = new Course();
        e.name = "English";
        e.code = 'E';
        e.periods = 4;
        e.daysOff = 1;
        return e;
    }

    public static Course French() {
        Course f = new Course();
        f.name = "French";
        f.code = 'F';
        f.periods = 5;
        return f;
    }

    public static Course Geography() {
        Course g = new Course();
        g.name = "Geography";
        g.code = 'G';
        g.periods = 2;
        return g;
    }

    public static Course Art() {
        Course a = new Course();
        a.name = "Art";
        a.code = 'A';
        a.periods = 2;
        a.daysOff = 2;
        return a;
    }
    
    public static Course Music() {
        Course m = new Course();
        m.name = "Music";
        m.code = 'U';
        m.periods = 3;
        m.daysOff = 1;
        return m;
    }    
    
}
