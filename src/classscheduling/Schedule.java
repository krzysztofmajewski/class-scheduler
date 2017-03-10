/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

import static classscheduling.Course.FRENCH;

/**
 *
 * @author krzys
 */
// TODO: review throws clauses
public class Schedule {

    public static final String VERSION = "v4";

    final Course board[];

    int freeSlots = 60;
    int smallestNumberOfFreeSlots = 60;
    int largestNumberOfFreeSlotsWhenBacktracking = 0;
    long movesSeenInThisGame = 0;
    long movesFailedVetting = 0;

    double[] avgIllegalMovesAtDepth;
    int[] samplesAtDepth;

    HopelessPartialSchedules hopelessPartialSchedules;

    final ScheduleValidator validator;

    public Schedule() {
        board = initBoard();
        validator = new ScheduleValidator(this);
        Course.reset();
        avgIllegalMovesAtDepth = new double[60];
        samplesAtDepth = new int[60];
        hopelessPartialSchedules = new HopelessPartialSchedules(this);
    }

    void print() {
        for (Grade grade : Grade.values()) {
            System.out.printf("%10s", grade.name() + " |");
            for (Day day : Day.values()) {
                for (Period period : Period.values()) {
                    Course course = getCourse(grade, day, period);
                    if (course == null) {
                        System.out.print('?');
                    } else {
                        System.out.print(course.code);
                    }
                }
                System.out.print("|");
            }
            System.out.println();
        }
    }

    // returns true if it finds a solution
    boolean scheduleCourses(MovesIterator iterator) throws SanityCheckException {
        movesSeenInThisGame++;
        boolean knownBadMove = !hopelessPartialSchedules.vetThisMove(iterator.depth);
        if (knownBadMove) {
            movesFailedVetting++;
            return false;
        }
        while (iterator.notDone()) {
            Move move = iterator.move();
//            System.out.println("Moved: " + move.slot + " : " + move.course);
            // TODO: should we vet first, then check correctness?
            // check for correctness of current schedule
            validator.reset();
            validator.validateCorrectnessConstraints(move);
            if (validator.hasErrors()) {
                // no solution from this move, try another slot
                movesSeenInThisGame++;
//                iterator.markMoveAsIllegal();
                iterator.retreat(move);
//                System.out.println("Retreated illegal move: " + move.slot + " : " + move.course);
                continue;
            }
            // valid move
            boolean vetted = hopelessPartialSchedules.vetThisMove(iterator.depth);
            if (!vetted) {
                movesFailedVetting++;
                iterator.retreat(move);
//                System.out.println("Retreated known bad move: " + move.slot + " : " + move.course);
                continue;
            }
            if (freeSlots < smallestNumberOfFreeSlots) {
                smallestNumberOfFreeSlots = freeSlots;
                System.out.println("Best move so far: " + move.slot);
                System.out.println(freeSlots + " free slots left");
                print();
                System.out.println();
                if (freeSlots == 0) {
                    validator.validate();
                    if (validator.hasNoErrors()) {
                        return true;
                    } else {
                        throw new SanityCheckException("If this move is no good, why am I here?");
                    }
                }
            }
            if (enoughPeriodsPerWeek(iterator.currentCourse)) {
                iterator.selectNextCourse();
            }
            MovesIterator subproblemIterator = new MovesIterator(iterator);
            boolean hasSolution = scheduleCourses(subproblemIterator);
            if (hasSolution) {
                return true;
            }
            retreatAndPrintInfoIfNeeded(iterator, subproblemIterator, move);
        }
        if (freeSlots > 0) {
            // tried all possible moves, did not find solution
            iterator.markMoveAsIllegal();
            return false;
        }
        // zero free slots left
        validator.validate();
        if (validator.hasErrors()) {
            throw new SanityCheckException("Is this situation possible?");
        }
        return true;
    }

    void retreatAndPrintInfoIfNeeded(MovesIterator iterator, MovesIterator subproblemIterator,
            Move move) throws SanityCheckException {
        boolean printInfo = false;
        if (freeSlots > largestNumberOfFreeSlotsWhenBacktracking) {
            largestNumberOfFreeSlotsWhenBacktracking = freeSlots;
            System.out.println("\nRetreating from: " + move.slot + " : " + move.course);
            printInfo = true;
        }
        iterator.retreat(move);
//        System.out.println("Retreated bad move: " + move.slot + " : " + move.course);
        if (printInfo) {
            System.out.print("Retreated from hopeless move, ");
            System.out.println(freeSlots + " free slots");
            printStats();
            print();
            if (iterator.currentCourse == null) {
                System.out.println("This iterator has attempted all of its moves");
            } else {
                System.out.println("Next move for this iterator will try to schedule course: " + iterator.currentCourse);
            }
        }
    }

    void printStats() {
        System.out.println(movesSeenInThisGame + " moves seen in this game");
        System.out.println(movesFailedVetting + " moves failed vetting in this game");
        System.out.println(hopelessPartialSchedules.numAdded
                + " partial schedules added to lookup table");
        System.out.println(hopelessPartialSchedules.numPurged
                + " partial schedules purged from lookup table");
        System.out.println(hopelessPartialSchedules.numOverflowed
                + " partial schedules exceeded lookup table capacity");
        System.out.println(hopelessPartialSchedules.numElements
                + " partial schedules remaining in lookup table");
        System.out.println(hopelessPartialSchedules.maxElements
                + " partial schedules in lookup table at its fullest");
    }

    boolean enoughPeriodsPerWeek(Course course) {
        for (Grade grade : Grade.values()) {
            if (course.count(grade) < course.periods) {
                return false;
            }
        }
        return true;
    }

    Slot set(Day day, Grade grade, Period period, Course course) throws SanityCheckException {
        Slot slot = new Slot(grade, day, period);
        Move move = new Move(slot, course);
        return set(move);
    }

    // TODO: remove sanity checks
    void clear(Move move) throws SanityCheckException {
        freeSlots++;
        int index = computeIndex(move.slot.grade, move.slot.day, move.slot.period);
        if (board[index] == null) {
            throw new SanityCheckException(move.slot + " already cleared");
        }
        if (board[index] != move.course) {
            throw new SanityCheckException(move.slot + ": expected " + move.course + " but found " + board[index]);
        }
        board[index] = null;
        if (!move.course.equals(FRENCH)) {
            move.course.decrementPeriodsScheduled(move.slot.grade, move.slot.day);
        } else {
            decrementFrenchPeriodsScheduled(move.slot.grade, move.slot.day, move.slot.period);
        }
    }

    // TODO: remove sanity checks
    Slot set(Move move) throws SanityCheckException {
        freeSlots--;
        if (!move.course.equals(FRENCH)) {
            move.course.incrementPeriodsScheduled(move.slot.grade, move.slot.day);
        } else {
            incrementFrenchPeriodsScheduled(move.slot.grade, move.slot.day, move.slot.period);
        }
        setCourse(move.slot.grade, move.slot.day, move.slot.period, move.course);
        return move.slot;
    }

    // TODO: remove sanity check
    void setCourse(Grade grade, Day day, Period period, Course course) throws SanityCheckException {
        int index = computeIndex(grade, day, period);
        if (board[index] != null) {
            Slot slot = new Slot(grade, day, period);
            throw new SanityCheckException(slot + " already has a course");
        }
        board[index] = course;
    }

    Course getCourse(Grade grade, Day day, Period period) {
        int index = computeIndex(grade, day, period);
        return board[index];
    }

    boolean isScheduled(Course course, Grade grade, Day day, Period period) {
        Course actualCourse = getCourse(grade, day, period);
        return (actualCourse != null) && (actualCourse.equals(course));
    }

    private int computeIndex(Grade grade, Day day, Period period) {
        int index = grade.ordinal() * Day.values().length * Period.values().length
                + day.ordinal() * Period.values().length
                + period.ordinal();
        return index;
    }

    private Course[] initBoard() {
        return new Course[60];
    }

    private void incrementFrenchPeriodsScheduled(Grade grade, Day day, Period period) throws SanityCheckException {
        boolean alreadyScheduledInThisPeriod = false;
        for (Grade g : Grade.values()) {
            Course course = getCourse(g, day, period);
            if ((course != null) && course.equals(FRENCH)) {
                alreadyScheduledInThisPeriod = true;
                break;
            }
        }
        if (!alreadyScheduledInThisPeriod) {
            FRENCH.incrementFrenchPeriodsScheduled(day);
        }
        FRENCH.incrementFrenchPeriodsScheduled(grade, day);
    }

    private void decrementFrenchPeriodsScheduled(Grade grade, Day day, Period period) throws SanityCheckException {
        boolean stillScheduledInThisPeriod = false;
        for (Grade g : Grade.values()) {
            Course course = getCourse(g, day, period);
            if ((course != null) && course.equals(FRENCH)) {
                stillScheduledInThisPeriod = true;
                break;
            }
        }
        if (!stillScheduledInThisPeriod) {
            FRENCH.decrementFrenchPeriodsScheduled(day);
        }
        FRENCH.decrementFrenchPeriodsScheduled(grade, day);
    }

}
