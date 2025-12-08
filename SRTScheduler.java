import java.util.*;

public class SRTScheduler extends Scheduler {

    private LinkedList<Process> processes;
    private List<Process> finishedProcesses;
    private int currentTime = 0;
    private int busyTime = 0;
    private int idleTime = 0;
    private final int ctxSwitchTime = 2;   // ثابت
    private int contextSwitches = 0;

    public SRTScheduler(LinkedList<Process> processes) {
        this.processes = new LinkedList<>(processes);
        this.finishedProcesses = new ArrayList<>();
    }

    @Override
    public void run() {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║     Shortest Remaining Time (SRT) Scheduler    ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        LinkedList<Process> readyQueue = new LinkedList<>();
        Process currentProcess = null;

        while (finishedProcesses.size() < processes.size()) {

            // Add arrived processes
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime &&
                    p.getFinishedAt() == -1 &&
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

            // Pick shortest remaining time
            Process nextProcess = readyQueue.stream()
                    .min(Comparator.comparingInt(Process::getRemainingTime))
                    .get();

            // Context switch
            if (currentProcess != null &&
                currentProcess.getProcessId() != nextProcess.getProcessId()) {

                contextSwitches++;
                currentTime += ctxSwitchTime;
            }

            currentProcess = nextProcess;

            // First time execution
            if (nextProcess.getStartedAt() == -1) {
                nextProcess.setStartedAt(currentTime);
                nextProcess.setResponseTime(currentTime - nextProcess.getArrivalTime());
            }

            // Execute 1 unit
            nextProcess.decrement();
            busyTime++;
            currentTime++;

            if (nextProcess.end()) {
                nextProcess.setFinishedAt(currentTime);
                nextProcess.calculateAllTimes();
                finishedProcesses.add(nextProcess);
                readyQueue.remove(nextProcess);
            }

            printProcessStatuses(readyQueue);
        }

        printStats();
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

    private void printStats() {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║     Shortest Remaining Time (SRT) Scheduler    ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
        super.printStatsDetials(finishedProcesses, busyTime, idleTime, ctxSwitchTime * contextSwitches);
    }
}
