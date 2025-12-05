import java.util.LinkedList;
import java.util.Queue;

class RoundRobinScheduler {
    private Queue<Process> readyQueue;
    private int timeQuantum;
    private LinkedList<Process> processes;

    public RoundRobinScheduler(int timeQuantum) {
        this.readyQueue = new LinkedList<>();
        this.timeQuantum = timeQuantum;
        this.processes = new LinkedList<>();
    }

    public void addProcess(Process process) {
        processes.add(process);
    }

    public void simulate() {
        int currentTime = 0;
        int completedProcesses = 0;

        processes.sort((p1, p2) -> Integer.compare(p1.getArrivalTime(), p2.getArrivalTime()));

        int index = 0;
        while (index < processes.size() && processes.get(index).getArrivalTime() <= currentTime) {
            readyQueue.add(processes.get(index));
            index++;
        }

        System.out.println("Simulation started with time quantum: " + timeQuantum);

        while (completedProcesses < processes.size()) {
            if (!readyQueue.isEmpty()) {
                Process currentProcess = readyQueue.poll();

                int execTime = Math.min(timeQuantum, currentProcess.getRemainingTime());
                currentProcess.setRemainingTime(currentProcess.getRemainingTime() - execTime);
                currentTime += execTime;

                System.out.printf("Time %d: Executing Process %d for %d units\n", 
                                  currentTime - execTime, currentProcess.getProcessId(), execTime);

                while (index < processes.size() && processes.get(index).getArrivalTime() <= currentTime) {
                    readyQueue.add(processes.get(index));
                    index++;
                }

                if (currentProcess.getRemainingTime() > 0) {
                    readyQueue.add(currentProcess);
                } else {
                    currentProcess.setFinishedAt(currentTime);
                    currentProcess.setTurnaroundTime(currentTime - currentProcess.getArrivalTime());
                    currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
                    completedProcesses++;
                    System.out.printf("Time %d: Process %d completed\n", currentTime, currentProcess.getProcessId());
                }
            } else {
                if (index < processes.size()) {
                    currentTime = processes.get(index).getArrivalTime();
                    readyQueue.add(processes.get(index));
                    index++;
                }
            }
        }

        System.out.println("Simulation completed.");
    }

    public void printStatistics() {
        System.out.println("\nProcess Statistics:");
        System.out.printf("%-10s %-15s %-15s %-15s %-15s %-15s\n", 
                          "Process ID", "Arrival Time", "Burst Time", "Completion Time", "Turnaround Time", "Waiting Time");

        double totalTurnaround = 0;
        double totalWaiting = 0;

        for (Process p : processes) {
            System.out.printf("%-10d %-15d %-15d %-15d %-15d %-15d\n", 
                              p.getProcessId(), p.getArrivalTime(), p.getBurstTime(), 
                              p.getFinishedAt(), p.getTurnaroundTime(), p.getWaitingTime());
            totalTurnaround += p.getTurnaroundTime();
            totalWaiting += p.getWaitingTime();
        }

        System.out.printf("\nAverage Turnaround Time: %.2f\n", totalTurnaround / processes.size());
        System.out.printf("Average Waiting Time: %.2f\n", totalWaiting / processes.size());
    }
}