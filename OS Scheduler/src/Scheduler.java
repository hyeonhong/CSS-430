/**
 *
 * @author      Hyeon Hong
 * @date        07/19/2016
 * @assignment  Lab 2 - Part 2
 *
 *
 * Scheduler class implements a MFQS (Multilevel Feedback Queue Scheduling) algorithm.
 *
 * The scheduler operates according to the following specification:
 * 1. It has 3 queues with a time slice of 500, 1000, 2000 ms respectively.
 * 2. Each queue in itself is scheduled via round robin.
 * 3. New threads are added to the highest priority queue, which is queue0 (500ms = timeSlice/2).
 * 4. If the thread in queue0 doesn't complete the execution within the time quantum,
 *    it gets suspended and moved to the lower level queue, which is queue1 (1000ms = timeSlice).
 * 5. If queue0 is empty, the scheduler executes the first thread in queue1.
 * 6. After one execution, the scheduler first checks queue0 to see if it's not empty or not.
 * 7. If queue0 is not empty, then threads in it are processed first.
 * 8. If the thread in queue1 doesn't complete the execution within the time quantum,
 *    it gets suspended and moved to the lower level queue, which is queue2 (2000ms = 2 * timeSlice).
 * 9. If both queue0 and queue1 are empty, the scheduler executes the first thread in queue2.
 * 10. After one execution, the scheduler first checks queue0 and queue1 to see if they're empty or not.
 * 11. If they are not, then queue0 is processed first, and then queue1 is processed.
 * 12. If the thread in queue2 doesn't complete the execution within the time quantum,
 *     it gets suspended and moved to the tail of queue2.
 *
 *
 * Assumptions:
 * - There are two additional threads (Loader, Test2) scheduled in the queues by ThreadOS.
 *   However, they are not explicitly discussed in this algorithm since they don't affect
 *   the overall comparison aspect of two algorithms (RR and MFQS).
 *
 *
 */


import java.util.*;

@SuppressWarnings("deprecation")

public class Scheduler extends Thread {

    private Vector queue0;  // time quantum: 500ms ( = timeSlice / 2 )
    private Vector queue1;  // time quantum: 1000ms ( = timeSlice )
    private Vector queue2;  // time quantum: 2000ms ( = timeSlice * 2 )

    private int timeSlice;
    private static final int DEFAULT_TIME_SLICE = 1000;

    // New data added to p161
    private boolean[] tids; // Indicate which ids have been used
    private static final int DEFAULT_MAX_THREADS = 10000;

    // A new feature added to p161
    // Allocate an ID array, each element indicating if that id has been used
    private int nextId = 0;

    private void initTid(int maxThreads) {
        tids = new boolean[maxThreads];
        for (int i = 0; i < maxThreads; i++)
            tids[i] = false;
    }

    // A new feature added to p161
    // Search an available thread ID and provide a new thread with this ID
    private int getNewTid() {
        for (int i = 0; i < tids.length; i++) {
            int tentative = (nextId + i) % tids.length;
            if (tids[tentative] == false) {
                tids[tentative] = true;
                nextId = (tentative + 1) % tids.length;
                return tentative;
            }
        }
        return -1;
    }

    // A new feature added to p161
    // Return the thread ID and set the corresponding tids element to be unused
    private boolean returnTid(int tid) {
        if (tid >= 0 && tid < tids.length && tids[tid] == true) {
            tids[tid] = false;
            return true;
        }
        return false;
    }

    // A new feature added to p161
    // Retrieve the current thread's TCB from the queues
    // Search them in the order of queue0, queue1, queue2
    public TCB getMyTcb() {
        Thread myThread = Thread.currentThread(); // Get my thread object

        // traverse in queue0
        synchronized (queue0) {
            for (int i = 0; i < queue0.size(); i++) {
                TCB tcb = (TCB) queue0.elementAt(i);
                Thread thread = tcb.getThread();
                if (thread == myThread) // if this is my TCB, return it
                    return tcb;
            }
        }

        // traverse in queue1
        synchronized (queue1) {
            for (int i = 0; i < queue1.size(); i++) {
                TCB tcb = (TCB) queue1.elementAt(i);
                Thread thread = tcb.getThread();
                if (thread == myThread) // if this is my TCB, return it
                    return tcb;
            }
        }

        // traverse in queue2
        synchronized (queue2) {
            for (int i = 0; i < queue2.size(); i++) {
                TCB tcb = (TCB) queue2.elementAt(i);
                Thread thread = tcb.getThread();
                if (thread == myThread) // if this is my TCB, return it
                    return tcb;
            }
        }

        return null;
    }

    // A new feature added to p161
    // Return the maximal number of threads to be spawned in the system
    public int getMaxThreads() {
        return tids.length;
    }

    public Scheduler() {
        timeSlice = DEFAULT_TIME_SLICE;
        queue0 = new Vector();
        queue1 = new Vector();
        queue2 = new Vector();
        initTid(DEFAULT_MAX_THREADS);
    }

    public Scheduler(int quantum) {
        timeSlice = quantum;
        queue0 = new Vector();
        queue1 = new Vector();
        queue2 = new Vector();
        initTid(DEFAULT_MAX_THREADS);
    }

    // A new feature added to p161
    // A constructor to receive the max number of threads to be spawned
    public Scheduler(int quantum, int maxThreads) {
        timeSlice = quantum;
        queue0 = new Vector();
        queue1 = new Vector();
        queue2 = new Vector();
        initTid(maxThreads);
    }

    private void schedulerSleep() {
        try {
            if (queue0.size() != 0) {           // if queue0 is not empty
                Thread.sleep(timeSlice / 2);

            } else if (queue1.size() != 0) {    // if queue0 is empty & queue1 is not empty
                Thread.sleep(timeSlice);

            } else {                            // if queue0 and queue1 is empty
                Thread.sleep(timeSlice * 2);
            }

        } catch (InterruptedException e) {
        }
    }

    // A modified addThread of p161 example
    public TCB addThread(Thread t) {
        // t.setPriority( 2 );
        TCB parentTcb = getMyTcb(); // get my TCB and find my TID
        int pid = (parentTcb != null) ? parentTcb.getTid() : -1;
        int tid = getNewTid(); // get a new TID
        if (tid == -1)
            return null;
        TCB tcb = new TCB(t, tid, pid); // create a new TCB
        queue0.add(tcb); // New thread should be always added to queue0.
        return tcb;
    }

    // A new feature added to p161
    // Removing the TCB of a terminating thread
    public boolean deleteThread() {
        TCB tcb = getMyTcb();
        if (tcb != null)
            return tcb.setTerminated();
        else
            return false;
    }

    public void sleepThread(int milliseconds) {
        try {
            sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }

    // A modified run of p161
    public void run() {
        Thread current = null;

        // this.setPriority( 6 );

        while (true) {
            try {
                // get the next TCB and its thread
                // if queues are empty, keep waiting
                if (queue0.size() == 0 && queue1.size() == 0 && queue2.size() == 0)
                    continue;


                /************** process the queue0 **************/
                if (queue0.size() != 0) {
                    TCB currentTCB = (TCB) queue0.firstElement();

                    if (currentTCB.getTerminated() == true) {
                        queue0.remove(currentTCB);
                        returnTid(currentTCB.getTid());
                        continue;
                    }

                    current = currentTCB.getThread();
                    if (current != null) {
                        if (current.isAlive()) {
                            // current.setPriority( 4 );
                            current.resume();
                        } else {
                            // Spawn must be controlled by Scheduler
                            // Scheduler must start a new thread
                            current.start();

                            // current.setPriority( 4 );
                        }
                    }

                    schedulerSleep();
                    // System.out.println("* * * Context Switch * * * ");

                    synchronized (queue0) {
                        if (current != null && current.isAlive()) {
                            // current.setPriority( 2 );
                            current.suspend();
                        }
                        queue0.remove(currentTCB);  // remove this TCB from queue0
                        queue1.add(currentTCB);     // append this TCB to queue1
                    }
                }


                /************** process the queue1 **************/
                else if (queue1.size() != 0) {
                    TCB currentTCB = (TCB) queue1.firstElement();

                    if (currentTCB.getTerminated() == true) {
                        queue1.remove(currentTCB);
                        returnTid(currentTCB.getTid());
                        continue;
                    }

                    current = currentTCB.getThread();
                    if (current != null) {
                        if (current.isAlive()) {
                            // current.setPriority( 4 );
                            current.resume();
                        } else {
                            // Spawn must be controlled by Scheduler
                            // Scheduler must start a new thread
                            current.start();

                            // current.setPriority( 4 );
                        }
                    }

                    schedulerSleep();
                    // System.out.println("* * * Context Switch * * * ");

                    synchronized (queue1) {
                        if (current != null && current.isAlive()) {
                            // current.setPriority( 2 );
                            current.suspend();
                        }
                        queue1.remove(currentTCB);  // remove this TCB from queue1
                        queue2.add(currentTCB);     // append this TCB to queue2
                    }
                }


                /************** process the queue2 **************/
                else {
                    TCB currentTCB = (TCB) queue2.firstElement();

                    if (currentTCB.getTerminated() == true) {
                        queue2.remove(currentTCB);
                        returnTid(currentTCB.getTid());
                        continue;
                    }

                    current = currentTCB.getThread();
                    if (current != null) {
                        if (current.isAlive()) {
                            // current.setPriority( 4 );
                            current.resume();
                        } else {
                            // Spawn must be controlled by Scheduler
                            // Scheduler must start a new thread
                            current.start();

                            // current.setPriority( 4 );
                        }
                    }

                    schedulerSleep();
                    // System.out.println("* * * Context Switch * * * ");

                    synchronized (queue2) {
                        if (current != null && current.isAlive()) {
                            // current.setPriority( 2 );
                            current.suspend();
                        }
                        queue2.remove(currentTCB);  // remove this TCB from queue2
                        queue2.add(currentTCB);     // append this TCB to queue2
                    }
                }

            } catch (NullPointerException e3) {
            }
        }
    }
}