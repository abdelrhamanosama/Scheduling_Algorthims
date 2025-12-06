import java.util.*;

public class PriorityScheduler implements Scheduler{

    private LinkedList<Process> processes;
    private Queue<Process> readyQueue = new LinkedList<>();
    private Queue<Process> waitingQueue = new LinkedList<>();

    private int time = 0;
    private boolean isPreemptive;

    public PriorityScheduler(LinkedList<Process> processes, boolean isPreemptive) {
        this.processes = processes;
        this.isPreemptive = isPreemptive;

        processes.sort(Comparator.comparingInt(p -> p.getArrivalTime()));
    }

    public void run() {

        System.out.println(isPreemptive ?
                "=== Priority Scheduling (Preemptive) ===" :
                "=== Priority Scheduling (Non-Preemptive) ===");

        List<Process> finished = new ArrayList<>();

        while (finished.size() < processes.size()) {

            // 1) add arrived processes to readyQueue
            for (Process p : processes) {
                if (p.getArrivalTime() <= time && p.getRemainingTime() > 0 && !readyQueue.contains(p) && !finished.contains(p)) {
                    readyQueue.add(p);
                }
            }

            printStatus();

            // idle CPU
            if (readyQueue.isEmpty()) {
                time++;
                continue;
            }

            // 2) pick highest priority
            Process currProcess = getHighestPriority();

            System.out.println("Time " + time + ": Running " + currProcess.getName());

            if (isPreemptive) {
                // ========== PREEMPTIVE PHASE ==========
                currProcess.setRemainingTime((currProcess.getRemainingTime()-1));
                time++;

                if (currProcess.getRemainingTime() == 0) {
                    currProcess.setTurnaroundTime ( time - currProcess.getArrivalTime());
                    currProcess.setWaitingTime( currProcess.getTurnaroundTime() - currProcess.getBurstTime());
                    finished.add(currProcess);
                    readyQueue.remove(currProcess);
                }

            } else {
                // ========== NON-PREEMPTIVE PHASE ==========
                time += currProcess.getRemainingTime();
                currProcess.setRemainingTime(0);

                currProcess.setTurnaroundTime(time - currProcess.getArrivalTime());
                currProcess.setWaitingTime(currProcess.getTurnaroundTime() - currProcess.getBurstTime());

                finished.add(currProcess);
                readyQueue.remove(currProcess);
            }
        }

        printFinalTable();
    }

    private Process getHighestPriority() {
        // smaller priority value = higher priority
        return readyQueue.stream()
                .min(Comparator.comparingInt(p -> p.getPriority()))
                .get();
    }

    private void printStatus() {
        System.out.println("\nTime " + time + " Status:");

        System.out.print("Ready Queue: ");
        for (Process p : readyQueue)
            System.out.print(p.getName() + "(P=" + p.getPriority() + ") ");
        System.out.println();

        System.out.print("Waiting Queue: ");
        for (Process p : waitingQueue) System.out.print(p.getName() + " ");
        System.out.println();
    }

    private void printFinalTable() {
        System.out.println("\n====================================================");
        System.out.println("Name | Arr | Burst | Priority | Waiting | Turnaround");

        for (Process p : processes) {
            System.out.printf(
                    "%4s | %3d | %5d | %8d | %7d | %10d\n",
                    p.getName(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(), p.getWaitingTime(), p.getTurnaroundTime()
            );
        }
    }
}
