import java.util.*;

public class SJF{
    LinkedList<Process> ourProcessList;
    Queue<Process> waitingQueue;
    Queue<Process> runningQueue;

    // Constructor
    public SJF(LinkedList<Process> ourProcesses) {
        this.ourProcessList = ourProcesses;
        this.waitingQueue = new LinkedList<>();
        this.runningQueue = new LinkedList<>();
    }

    public void runScheduler() {
        int time = 0;
        StringBuilder gantt = new StringBuilder();
        int n = ourProcessList.size();
        int completed = 0;
        boolean[] isCompleted = new boolean[n];

        while (completed < n) {
            // Add ourProcesses that have arrived to waiting queue
            for (int i = 0; i < n; i++) {
                if (!isCompleted[i] && ourProcessList.get(i).getArrivalTime() <= time) {
                    if (!waitingQueue.contains(ourProcessList.get(i))) {
                        waitingQueue.add(ourProcessList.get(i));
                    }
                }
            }

            if (!waitingQueue.isEmpty()) {
                // Pick ourProcess with shortest burst time
                Process current = Collections.min(waitingQueue, Comparator.comparingInt(p -> p.getBurstTime()));
                waitingQueue.remove(current);
                runningQueue.add(current);

                gantt.append("|").append(current.getName());
                time += current.getBurstTime();
                isCompleted[ourProcessList.indexOf(current)] = true;
                runningQueue.poll();
                completed++;
            } else {
                gantt.append("|IDLE");
                time++;
            }
        }

        gantt.append("|");
        System.out.println("Gantt Chart: " + gantt);
    }
}
