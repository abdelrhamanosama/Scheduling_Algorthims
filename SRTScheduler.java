import java.util.LinkedList;
import java.util.Iterator;

public class SRTScheduler {

    private LinkedList<Process> processes;
    private LinkedList<Process> readyQueue;
    private LinkedList<String> ganttChart;
    private LinkedList<Integer> ganttTimes;
    private int totalTime;

    public SRTScheduler(LinkedList<Process> processes) {
        this.processes = new LinkedList<>(processes);
        this.readyQueue = new LinkedList<>();
        this.ganttChart = new LinkedList<>();
        this.ganttTimes = new LinkedList<>();
        this.totalTime = 0;
    }

    public void schedule() {
        int currentTime = 0;
        int completed = 0;
        int n = processes.size();
        String lastProcess = "";
        Process currentProcess = null;
        ganttTimes.addLast(0);

        while (completed < n) {
            addArrivedProcesses(currentTime);
            Process shortestProcess = findShortestRemainingTime();
            if (shortestProcess == null) {
                if (!lastProcess.equals("IDLE")) {
                    ganttChart.addLast("IDLE");
                    ganttTimes.addLast(currentTime);
                    lastProcess = "IDLE";
                }
                currentTime++;
                continue;
            }
            if (shortestProcess.getResponseTime() == -1) {
                shortestProcess.setResponseTime(currentTime - shortestProcess.getArrivalTime());
                shortestProcess.setStartedAt(currentTime);
            }
            if (!shortestProcess.getName().equals(lastProcess)) {
                ganttChart.addLast(shortestProcess.getName());
                ganttTimes.addLast(currentTime);
                lastProcess = shortestProcess.getName();
                currentProcess = shortestProcess;
            }
            shortestProcess.decrement();
            currentTime++;
            if (shortestProcess.end()) {
                completed++;
                shortestProcess.setFinishedAt(currentTime);
                shortestProcess.calculateAllTimes();
                readyQueue.remove(shortestProcess);
            }
        }
        ganttTimes.addLast(currentTime);
        totalTime = currentTime;
    }

    private void addArrivedProcesses(int currentTime) {
        Iterator<Process> iterator = processes.iterator();
        while (iterator.hasNext()) {
            Process p = iterator.next();
            if (p.getArrivalTime() == currentTime && !readyQueue.contains(p)) {
                readyQueue.addLast(p);
            }
        }
    }

    private Process findShortestRemainingTime() {
        if (readyQueue.isEmpty())
            return null;
        Process shortest = null;
        int minRemainingTime = Integer.MAX_VALUE;
        Iterator<Process> iterator = readyQueue.iterator();
        while (iterator.hasNext()) {
            Process p = iterator.next();
            if (p.getRemainingTime() > 0 && p.getRemainingTime() < minRemainingTime) {
                minRemainingTime = p.getRemainingTime();
                shortest = p;
            }
        }
        return shortest;
    }

    public void printGanttChart() {
        if (ganttChart.isEmpty())
            return;
        System.out.print(" ");
        for (int i = 0; i < ganttChart.size(); i++)
            System.out.print("--------");
        System.out.println();
        System.out.print("|");
        Iterator<String> chartIterator = ganttChart.iterator();
        while (chartIterator.hasNext()) {
            String process = chartIterator.next();
            System.out.printf("  %-4s |", process);
        }
        System.out.println();
        System.out.print(" ");
        for (int i = 0; i < ganttChart.size(); i++)
            System.out.print("--------");
        System.out.println();
        Iterator<Integer> timeIterator = ganttTimes.iterator();
        while (timeIterator.hasNext()) {
            Integer time = timeIterator.next();
            System.out.printf("%-8d", time);
        }
        System.out.println("\n");
    }

    public void printResults() {
        System.out.printf("%-8s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s\n",
                "ID", "Name", "Arrival", "Burst", "Start", "Finish", "TAT", "Waiting", "Response");
        Iterator<Process> iterator = processes.iterator();
        while (iterator.hasNext()) {
            Process p = iterator.next();
            System.out.printf("%-8d %-10s %-10d %-10d %-10d %-10d %-10d %-10d %-10d\n",
                    p.getProcessId(),
                    p.getName(),
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
        double totalTAT = 0, totalWT = 0, totalRT = 0;
        int n = processes.size();
        Iterator<Process> iterator = processes.iterator();
        while (iterator.hasNext()) {
            Process p = iterator.next();
            totalTAT += p.getTurnaroundTime();
            totalWT += p.getWaitingTime();
            totalRT += p.getResponseTime();
        }
        System.out.printf("Average Turnaround Time : %.2f\n", totalTAT / n);
        System.out.printf("Average Waiting Time    : %.2f\n", totalWT / n);
        System.out.printf("Average Response Time   : %.2f\n", totalRT / n);
        System.out.printf("Total Execution Time    : %d\n", totalTime);
        System.out.printf("CPU Utilization         : %.2f%%\n", calculateCPUUtilization());
        System.out.printf("Context Switches        : %d\n", ganttChart.size() - 1);
        System.out.println();
    }

    private double calculateCPUUtilization() {
        int idleTime = 0;
        Iterator<String> chartIterator = ganttChart.iterator();
        int index = 0;
        while (chartIterator.hasNext()) {
            String process = chartIterator.next();
            if (process.equals("IDLE") && index < ganttTimes.size() - 1) {
                idleTime += ganttTimes.get(index + 1) - ganttTimes.get(index);
            }
            index++;
        }
        if (totalTime == 0)
            return 0;
        return ((totalTime - idleTime) / (double) totalTime) * 100;
    }

    public void printSummary() {
        System.out.println("Number of Processes   : " + processes.size());
        System.out.println("Total Execution Time : " + totalTime);
        System.out.println("Context Switches      : " + (ganttChart.size() - 1));
        System.out.println("Ready Queue (Final)   : " + readyQueue.size());
        System.out.println();
    }

    public void printReadyQueueStatus() {
        if (readyQueue.isEmpty()) {
            System.out.println("Ready Queue is empty.");
        } else {
            Iterator<Process> iterator = readyQueue.iterator();
            while (iterator.hasNext()) {
                Process p = iterator.next();
                System.out.println(p.getName() + " (Remaining: " + p.getRemainingTime() + ")");
            }
        }
        System.out.println();
    }

    public void displayAllResults() {
        printGanttChart();
        printResults();
        printAverages();
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
