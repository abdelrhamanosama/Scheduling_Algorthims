import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

class RoundRobinScheduler extends  Scheduler{
    private Queue<Process> readyQueue;
    private int timeQuantum = -1;
    private LinkedList<Process> processes;
    private List<Process> finishedProcesses;
    private Process currentProcess;
    private Process previousProcess;
    // context switch and CPU accounting
    private int contextSwitch = 2;
    private int ctxSwitchTime = 0;
    private int busyTime = 0; // time CPU spent executing processes
    private int idleTime = 0; // time CPU was idle
    
    public RoundRobinScheduler(LinkedList<Process> rawProcesses, int timeQuantum) {
        this.readyQueue = new LinkedList<>();
        this.timeQuantum = timeQuantum;
        this.processes = rawProcesses;
        this.finishedProcesses = new LinkedList<>();
    }
    
    public void run() {
        
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║      Round Robin Scheduler Simulation          ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        // Use a priority queue for future arrivals (ordered by arrival time then id)
        PriorityQueue<Process> arrivals = new PriorityQueue<>(
            Comparator.comparingInt(Process::getArrivalTime)
                      .thenComparingInt(Process::getProcessId)
        );
        arrivals.addAll(processes);

        int currentTime = 0;
        int completedProcesses = 0;

        // move initially available processes (arrival time <= 0)
        while (!arrivals.isEmpty() && arrivals.peek().getArrivalTime() <= currentTime) {
            readyQueue.add(arrivals.poll());
        }
        
        while (completedProcesses < processes.size()) {
            if (!readyQueue.isEmpty()) {
                Process proc = readyQueue.poll();
                previousProcess = currentProcess;
                currentProcess = proc;

                // context switch if switching between processes
                if (previousProcess != null && currentProcess != null && previousProcess.getProcessId() != currentProcess.getProcessId()) {
                    printContextSwitch(currentTime);
                    currentTime += contextSwitch;
                    ctxSwitchTime += contextSwitch;
                }

                if (proc.getResponseTime() == -1) {
                    proc.setStartedAt(currentTime);
                    proc.setResponseTime(currentTime - proc.getArrivalTime());
                }

                int execTime = Math.min(timeQuantum, proc.getRemainingTime());
                int start = currentTime;
                proc.setRemainingTime(proc.getRemainingTime() - execTime);
                currentTime += execTime;
                busyTime += execTime;

                System.out.printf("Time %d-%d: Executing Process %d for %d units\n", start, currentTime, proc.getProcessId(), execTime);
                System.out.println("===");
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                // move newly arrived processes into ready queue
                while (!arrivals.isEmpty() && arrivals.peek().getArrivalTime() <= currentTime) {
                    readyQueue.add(arrivals.poll());
                }

                if (proc.getRemainingTime() > 0) {
                    readyQueue.add(proc);
                } else {
                    proc.setFinishedAt(currentTime);
                    proc.calculateAllTimes();
                    finishedProcesses.add(proc);
                    completedProcesses++;
                    System.out.printf("Time %d: Process %d completed\n", currentTime, proc.getProcessId());
                    System.out.println("===");
                    try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
                printProcessStatuses();
            } else {
                // no ready processes: advance to next arrival
                if (!arrivals.isEmpty()) {
                    int nextArrival = arrivals.peek().getArrivalTime();
                    int old = currentTime;
                    currentTime = Math.max(currentTime + 1, nextArrival);
                    idleTime += Math.max(0, currentTime - old);

                    while (!arrivals.isEmpty() && arrivals.peek().getArrivalTime() <= currentTime) {
                        readyQueue.add(arrivals.poll());
                    }
                } else {
                    break; // nothing left
                }
            }

            try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
        }

        System.out.println("\nSimulation completed.");
        printStatistics();
    }


    private void printContextSwitch(int currentTime) {
        String ctxSwitch = String.format(
            "%-15s %s",
            String.format("time %d-%d:", currentTime, currentTime + contextSwitch),
            "Context Switching"
        );
        System.out.println(ctxSwitch + "\n===");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void printProcessStatuses() {
        for (Process p : processes) {
            String status;
            if (p.getFinishedAt() != -1) {
                status = "terminated";
            } else {
                status = "ready";
            }
            System.out.println(String.format("  %s : %s", p.getName(), status));
        }
        System.out.println();
    }

    public void printStatistics() {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║      Round Robin Scheduler Statistics          ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
        super.printStatsDetials(finishedProcesses, busyTime, idleTime, ctxSwitchTime);
    }
}