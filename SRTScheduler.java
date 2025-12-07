import java.util.LinkedList;

public class SRTScheduler implements Scheduler {

    private LinkedList<Process> processes;
    private LinkedList<Process> readyQueue;
    private LinkedList<String> ganttChart;
    private LinkedList<Integer> ganttTimes;
    private int totalTime;
    private int contextSwitches;
    private int contextSwitchTime = 1;

    public SRTScheduler(LinkedList<Process> processes) {
        this.processes = new LinkedList<>(processes);
        this.readyQueue = new LinkedList<>();
        this.ganttChart = new LinkedList<>();
        this.ganttTimes = new LinkedList<>();
        this.totalTime = 0;
        this.contextSwitches = 0;
    }

    private String getProcessName(Process p) {
        return "P" + (p.getProcessId() + 1);
    }

    private void addArrivalsUpTo(int time) {
        for (Process p : processes) {
            if (p.getArrivalTime() <= time && !readyQueue.contains(p) && p.getRemainingTime() > 0) {
                readyQueue.add(p);
                System.out.println("Time " + p.getArrivalTime() + ": " + getProcessName(p) + " arrived");
            }
        }
    }

    public void run() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║    Shortest Remaining Time (SRT) Scheduler    ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        int currentTime = 0;
        int completed = 0;
        int n = processes.size();
        String lastEntry = "";
        ganttTimes.add(0);

        addArrivalsUpTo(currentTime);

        while (completed < n) {

            Process shortest = null;
            int minTime = Integer.MAX_VALUE;
            for (Process p : readyQueue) {
                if (p.getRemainingTime() > 0 && p.getRemainingTime() < minTime) {
                    minTime = p.getRemainingTime();
                    shortest = p;
                }
            }

            if (shortest == null) {
                if (!lastEntry.equals("IDLE")) {
                    ganttChart.add("IDLE");
                    ganttTimes.add(currentTime);
                    lastEntry = "IDLE";
                    System.out.println("Time " + currentTime + ": CPU IDLE");
                }
                currentTime++;
                addArrivalsUpTo(currentTime);
                continue;
            }

            String processName = getProcessName(shortest);

            if (!processName.equals(lastEntry)) {
                if (!lastEntry.equals("") && !lastEntry.equals("CS")) {
                    // Context switch
                    ganttChart.add("CS");
                    ganttTimes.add(currentTime);
                    System.out.println("Time " + currentTime + ": Context switch starts");
                    contextSwitches++;
                    currentTime += contextSwitchTime;
                    addArrivalsUpTo(currentTime);
                    lastEntry = "CS";
                }
            }

            if (shortest.getResponseTime() == -1) {
                shortest.setResponseTime(currentTime - shortest.getArrivalTime());
                shortest.setStartedAt(currentTime);
            }

            if (!processName.equals(lastEntry)) {
                ganttChart.add(processName);
                ganttTimes.add(currentTime);
                System.out.println("Time " + currentTime + ": " + processName + " started (Remaining: "
                        + shortest.getRemainingTime() + ")");
                lastEntry = processName;
            }

            // Execute 1 unit
            shortest.decrement();
            currentTime++;
            addArrivalsUpTo(currentTime);

            if (shortest.end()) {
                completed++;
                shortest.setFinishedAt(currentTime);
                shortest.calculateAllTimes();
                readyQueue.remove(shortest);
                System.out.println("Time " + currentTime + ": " + processName + " finished");
            }
        }

        ganttTimes.add(currentTime);
        totalTime = currentTime;

        System.out.println("\nAll processes completed!\n");
    }

    public void printResults() {
        System.out
                .println("╔════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out
                .println("║                              Process Results                                           ║");
        System.out.println(
                "╚════════════════════════════════════════════════════════════════════════════════════════╝\n");

        System.out.printf("%-8s %-10s %-10s %-10s %-10s %-10s %-10s %-10s\n",
                "ID", "Arrival", "Burst", "Start", "Finish", "TAT", "Waiting", "Response");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────────");

        for (Process p : processes) {
            System.out.printf("%-8s %-10d %-10d %-10d %-10d %-10d %-10d %-10d\n",
                    getProcessName(p),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getStartedAt(),
                    p.getFinishedAt(),
                    p.getTurnaroundTime(),
                    p.getWaitingTime(),
                    p.getResponseTime());
        }
        System.out.println();
    }

    public void printAverages() {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║                  Average Stats                 ║");
        System.out.println("╚════════════════════════════════════════════════╝");

        double totalTAT = 0, totalWT = 0, totalRT = 0;
        int n = processes.size();

        for (Process p : processes) {
            totalTAT += p.getTurnaroundTime();
            totalWT += p.getWaitingTime();
            totalRT += p.getResponseTime();
        }

        System.out.printf("Average turnaround   = %.2f\n", totalTAT / n);
        System.out.printf("Average waiting      = %.2f\n", totalWT / n);
        System.out.printf("Average response     = %.2f\n", totalRT / n);
        System.out.println();
    }

    public void printCPUUtilization() {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║                CPU Utilization                 ║");
        System.out.println("╚════════════════════════════════════════════════╝");

        int idleTime = 0;
        for (int i = 0; i < ganttChart.size(); i++) {
            if (ganttChart.get(i).equals("IDLE") && (i + 1) < ganttTimes.size()) {
                idleTime += ganttTimes.get(i + 1) - ganttTimes.get(i);
            }
        }

        int csTime = contextSwitches * contextSwitchTime;
        int busyTime = totalTime - idleTime; // شامل CS
        int totalSimulatedTime = totalTime;

        // double utilizationWithCS = (busyTime / (double) totalSimulatedTime) * 100;
        double utilizationProcessesOnly = ((busyTime - csTime) / (double) totalSimulatedTime) * 100;

        System.out.printf("Total simulated time          = %d\n", totalSimulatedTime);
        System.out.printf("Idle time                     = %d\n", idleTime);
        System.out.printf("Context-switch time (CS)      = %d (count = %d, each = %d)\n", csTime, contextSwitches,
                contextSwitchTime);
        System.out.printf("CPU Utilization (process only)= %.2f%%\n", utilizationProcessesOnly);
        System.out.println();
    }

    public void printSummary() {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║                  Summary                       ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("Number of Processes      : " + processes.size());
        System.out.println("Total Execution Time     : " + totalTime);
        System.out.println("Context Switches         : " + contextSwitches);
        System.out.println();
    }

    public void displayAllResults() {
        printResults();
        printAverages();
        printCPUUtilization();
        printSummary();
    }

    public LinkedList<Process> getProcesses() {
        return processes;
    }

    public LinkedList<Process> getReadyQueue() {
        return readyQueue;
    }

    public LinkedList<String> getGanttChart() {
        return ganttChart;
    }
}
