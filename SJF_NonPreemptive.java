import java.util.LinkedList;

public class SJF_NonPreemptive implements Scheduler{
    LinkedList<Process> processes;

    public SJF_NonPreemptive(LinkedList<Process> processes) {
        this.processes = processes;
    }
    @Override
    public  void run() {

        processes.sort((a, b) -> a.getArrivalTime() - b.getArrivalTime());
        int currentTime = 0;

        System.out.println("=== SJF Non-Preemptive Scheduling ===");

        while (!processes.isEmpty()) {
            LinkedList<Process> available = new LinkedList<>();
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime) {
                    available.add(p);
                }
            }

            if (available.isEmpty()) {
                currentTime = processes.getFirst().getArrivalTime();
                continue;
            }

            Process current = available.stream()
                    .min((x, y) -> x.getBurstTime() - y.getBurstTime())
                    .get();

            current.setStartedAt(currentTime);

            currentTime += current.getBurstTime();

            current.setFinishedAt(currentTime);

            current.calculateAllTimes();

            System.out.println(current.toString());

            processes.remove(current);
        }

        System.out.println("=== End SJF Non-Preemptive ===");
    }
}
