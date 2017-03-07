/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classscheduling;

/**
 *
 * A partial schedule is a schedule that may be only partially filled. For
 * partial schedule P, a partial schedule P' is a super-pattern of P iff for
 * each nonempty slot in P, P' has the same course in that slot. The partial
 * schedules are ordered in two dimensions, depth and insertion time. An ordered
 * traversal is guaranteed to visit partial schedules of non-decreasing depth.
 * At any given depth, the most recently inserted schedule will be visited
 * first.
 *
 * @author krzys
 */
public class HopelessPartialSchedules {

    private static final int MAX_ENTRIES = 1000;

    private final Schedule schedule;

    int numAdded;
    int numPurged;
    int numElements;

    // for each index d, the element at d is a pointer to the least recently added BoardState of depth d
    final BoardState depthHeads[];

    // for each index d, the element at d is a pointer to the most recently added BoardState of depth d
    final BoardState depthTails[];

    BoardState first;

    public HopelessPartialSchedules(Schedule schedule) {
        this.schedule = schedule;
        // depth 1..60
        depthHeads = new BoardState[61];
        depthTails = new BoardState[61];
    }

    BoardState addThisPartialSchedule(int depth) throws SanityCheckException {
        BoardState bs = new BoardState(schedule.freeSlotList, depth);
        purgeSuperPatterns(bs);
        add(bs);
        return bs;
    }

    BoardState findThisPatternOrSubpatternThereof(BoardState bs) {
        BoardState result = null;
        // TODO: if many BoardStates at one depth, might have been faster to stick with ArrayList<BoardState> iterator
        for (BoardState cursor = first; cursor != null; cursor = cursor.next) {
            if (cursor.depth < bs.depth) {
                // check for subpatterns of bs
                if (cursor.isSubPatternOf(bs)) {
                    cursor.hits++;
                    result = cursor;
                    break;
                }
            } else if (cursor.depth == bs.depth) {
                // check if bs already in here
                if (cursor.equals(bs)) {
                    // we already found this state once before
                    cursor.hits++;
                    result = cursor;
                    break;
                }
            } else {
                break;
            }
        }
        return result;
    }

// Returns false if any one of these is in boardStates:
//   this exact board state
//   a board state that is a subpattern of this board state
    boolean vetThisMove(int depth) {
        BoardState bs = new BoardState(schedule.freeSlotList, depth);
        return (findThisPatternOrSubpatternThereof(bs) == null);
    }

    // TODO: nuke sanity checks
    private void add(BoardState bs) throws SanityCheckException {
        // TODO: does this ever happen? should we shake things up when it does?
        if (numElements >= MAX_ENTRIES) {
            return;
        }
        // head is old
        // tail is new
        // find newest element at depth >= bs.depth, if any
        //   make it the second-newest element
        BoardState next = null;
        BoardState prev = null;
        for (int depth = bs.depth; depth <= 60; depth++) {
            next = depthTails[depth];
            if (next != null) {
                prev = next.prev;
                bs.next = next;
                bs.prev = prev;
                next.prev = bs;
                break;
            }
        }
        if (next == null) {
            // there is no next
            // therefore still need to compute prev
            // find oldest element at depth < bs.depth
            for (int depth = bs.depth - 1; depth >= 1; depth--) {
                if (depthHeads[depth] != null) {
                    prev = depthHeads[depth];
                    bs.prev = prev;
                    if (bs.next != prev.next) {
                        throw new SanityCheckException("bs.next should already be set, if it exists");
                    }
                    break;
                }
            }
        }
        if (prev != null) {
            prev.next = bs;
        }
        // adjust tail of doubly linked list to point to newest item
        if (depthTails[bs.depth] != null) {
            if (depthTails[bs.depth] != bs.next) {
                throw new SanityCheckException("if already something at this depth, it should be bs.next");
            }
        }
        depthTails[bs.depth] = bs;
        // adjust head of doubly linked list to point to oldest item
        if (depthHeads[bs.depth] == null) {
            depthHeads[bs.depth] = bs;
        }
        numAdded++;
        numElements++;
        if ((first == null) || (bs.depth <= first.depth)) {
            first = bs;
        }
    }

    private void purgeSuperPatterns(BoardState bs) {
        if (bs.depth == 60) {
            // there are no super-patterns
            return;
        }
        BoardState cursor = null;
        // start with the first BoardState we find that has depth > bs.depth
        for (int depth = bs.depth + 1; depth <= 60; depth++) {
            if (depthTails[depth] != null) {
                cursor = depthTails[depth];
                break;
            }
        }
        while (cursor != null) {
            if (bs.isSubPatternOf(cursor)) {
                BoardState removeMe = cursor;
                BoardState prev = removeMe.prev;
                BoardState next = removeMe.next;
                if (prev != null) {
                    prev.next = next;
                }
                if (next != null) {
                    next.prev = prev;
                }
                if (depthTails[removeMe.depth] == removeMe) {
                    depthTails[removeMe.depth] = null;
                    if ((next != null) && (next.depth == removeMe.depth)) {
                        depthTails[removeMe.depth] = next;
                    }
                }
                if (depthHeads[removeMe.depth] == removeMe) {
                    depthHeads[removeMe.depth] = null;
                    if ((prev != null) && (prev.depth == removeMe.depth)) {
                        depthHeads[removeMe.depth] = prev;
                    }
                }
                removeMe.prev = null;
                removeMe.next = null;
                numPurged++;
                numElements--;
            }
            cursor = cursor.next;
        }
    }
}
