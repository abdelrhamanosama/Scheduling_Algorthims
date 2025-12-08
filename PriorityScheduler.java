import java.util.*;

public class PriorityScheduler extends Scheduler {

    private LinkedList<Process> processes;
    private List<Process> finishedProcesses;
    private int currentTime = 0;
    private final int contextSwitch = 2;
    private int busyTime = 0;
    private int idleTime = 0;
    private int ctxSwitchCount = 0;
    private boolean isPreemptive;

    public PriorityScheduler(LinkedList<Process> processes, boolean isPreemptive) {

        this.processes = processes;
        this.finishedProcesses = new LinkedList<>();
        this.isPreemptive = isPreemptive;

        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
    }

    @Override
    public void run() {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        if (isPreemptive)
            System.out.println("║        Priority Scheduling (Preemptive)               ║");
        else
            System.out.println("║      Priority Scheduling (Non-Preemptive)             ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        LinkedList<Process> readyQueue = new LinkedList<>();
        Process currentProcess = null;

        while (finishedProcesses.size() < processes.size()) {

            // add newly arrived processes
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime &&
                        p.getRemainingTime() > 0 &&
                        !readyQueue.contains(p) &&
                        !finishedProcesses.contains(p)) {
                    readyQueue.add(p);
                }
            }

            if (readyQueue.isEmpty()) {
                idleTime++;
                currentTime++;
                continue;
            }

            // pick highest priority
            Process nextProcess = readyQueue.stream()
                    .min(Comparator.comparingInt(Process::getPriority))
                    .get();

            // Context switch
            if (currentProcess != nextProcess) {
                if(currentProcess != null){
                    
                if (!isPreemptive && currentProcess.getRemainingTime() > 0) {
                    nextProcess = currentProcess;  // ignore new higher priority
                } else {
                    ctxSwitchCount++;
                    currentTime += contextSwitch;
                    currentProcess = nextProcess;
                }
                }
                else{
                    currentProcess = nextProcess;
                }
            }

            // first time execution
            if (nextProcess.getStartedAt() == -1) {
                nextProcess.setStartedAt(currentTime);
                nextProcess.setResponseTime(currentTime - nextProcess.getArrivalTime());
            }

            if (isPreemptive) {

                nextProcess.setRemainingTime(nextProcess.getRemainingTime() - 1);
                busyTime++;
                currentTime++;

                if (nextProcess.getRemainingTime() == 0) {
                    nextProcess.setFinishedAt(currentTime);
                    nextProcess.calculateAllTimes();
                    finishedProcesses.add(nextProcess);
                    readyQueue.remove(nextProcess);
                }

            } else {

                int runTime = nextProcess.getRemainingTime();
                currentTime += runTime;
                busyTime += runTime;

                nextProcess.setRemainingTime(0);
                nextProcess.setFinishedAt(currentTime);
                nextProcess.calculateAllTimes();
                finishedProcesses.add(nextProcess);
                readyQueue.remove(nextProcess);
            }

            printProcessStatuses(readyQueue);
        }

        printStatsDetails();
    }

    private void printProcessStatuses(LinkedList<Process> readyQueue) {
        System.out.println("[Time " + currentTime + "] Status:");
        for (Process p : processes) {
            String status;
            if (finishedProcesses.contains(p)) status = "terminated";
            else if (readyQueue.contains(p)) status = "ready";
            else status = "waiting";

            System.out.println(String.format("  %s : %s", p.getName(), status));
        }
        System.out.println();
    }

    private void printStatsDetails() {


        System.out.println("╔════════════════════════════════════════════════════════╗");
        if (isPreemptive)
            System.out.println("║        Priority Scheduling (Preemptive)               ║");
        else
            System.out.println("║      Priority Scheduling (Non-Preemptive)             ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        super.printStatsDetials(finishedProcesses, busyTime, idleTime,ctxSwitchCount * contextSwitch);
    }
}
