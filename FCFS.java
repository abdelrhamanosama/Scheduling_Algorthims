import java.util.Comparator;
import java.util.LinkedList;

public class FCFS implements Scheduler {
    LinkedList<Process> processes;

    public FCFS(LinkedList<Process> processes) {
        this.processes = processes;
    }
    @Override
    public void run() {
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        int currentTime = 0;
        System.out.println("=== FCFS Scheduling ===");
        for (Process p : processes) {

            if (currentTime < p.getArrivalTime()) {
                currentTime = p.getArrivalTime();
            }
            p.setStartedAt(currentTime);
            currentTime += p.getBurstTime();
            p.setFinishedAt(currentTime);
            p.calculateAllTimes();
            System.out.println(p.toString());
        }

        System.out.println("=== End FCFS ===");
    }
}
