import java.util.*;

public class FCFS {
    LinkedList<Process> processList;
    Queue<Process> waitingQueue;
    Queue<Process> runningQueue;
    
    // Constructor
    public FCFS(LinkedList<Process> processes) {
        this.processList = processes;
        this.waitingQueue = new LinkedList<>();
        this.runningQueue = new LinkedList<>();
    }

    public void runScheduler() {
        // Sort processes by arrival time
        processList.sort(Comparator.comparingInt(p -> p.getArrivalTime()));

        int time = 0;
        StringBuilder gantt = new StringBuilder();
        int index = 0;
        int n = processList.size();
        // int 
        while (index < n || !waitingQueue.isEmpty() || !runningQueue.isEmpty()) {
            // Add newly arrived processes to waiting queue
            while (index < n && processList.get(index).getArrivalTime() <= time) {
                waitingQueue.add(processList.get(index));
                index++;
            }

            // Move process to running queue if CPU idle
            if (runningQueue.isEmpty() && !waitingQueue.isEmpty()) {
                runningQueue.add(waitingQueue.poll());
            }

            if (!runningQueue.isEmpty()) {
                Process current = runningQueue.poll();
                gantt.append("|").append(current.getName());
                time += current.getBurstTime();
            } else {
                gantt.append("|IDLE");
                time++;
            }
        }

        gantt.append("|");
        System.out.println("Gantt Chart: " + gantt);
    }

    
}
