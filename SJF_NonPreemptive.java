import java.util.LinkedList;
import java.util.List;

public class SJF_NonPreemptive extends  Scheduler {
    LinkedList<Process> processes;
    private int busyTime;
    private int idleTime;
    private int ctxSwitchTime;
    private int currentTime = 0;
    private final int contextSwitch = 2;
    private List<Process> finishedProcesses;

    public SJF_NonPreemptive(LinkedList<Process> processes) {
        this.processes = processes;
        this.finishedProcesses = new LinkedList<>(); // FIX
    }

    @Override
    public void run() {
        processes.sort((a, b) -> a.getArrivalTime() - b.getArrivalTime());
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║            SJF Non-Preemptive Scheduling trace           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        while (!processes.isEmpty()) {
            LinkedList<Process> available = new LinkedList<>();
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime) {
                    available.add(p);
                }
            }

            if (available.isEmpty()) {
                idleTime += processes.getFirst().getArrivalTime() - currentTime;
                currentTime = processes.getFirst().getArrivalTime();
                continue;
            }

            Process current = available.stream()
                    .min((x, y) -> x.getBurstTime() - y.getBurstTime())
                    .get();

            current.setStartedAt(currentTime);
            current.setResponseTime(current.getStartedAt() - current.getArrivalTime()); // FIX

            currentTime += current.getBurstTime();
            current.setFinishedAt(currentTime);
            current.calculateAllTimes();
            busyTime += current.getBurstTime();

            // if (!processes.isEmpty() && processes.size() > 1) {
            //     // printContextSwitch();
            //     ctxSwitchTime += contextSwitch;
            //     currentTime += contextSwitch;
            // }

            finishedProcesses.add(current);
            processes.remove(current);

            printProcess(current);

            if (!processes.isEmpty()) {
                printContextSwitch();
                ctxSwitchTime += contextSwitch;
                currentTime += contextSwitch;
            }
            // printProcessStatuses();
        }

        printStats();
    }

    private void printProcess(Process currentProcess) {
        String batch = String.format(
            "%-15s %s",
            String.format("time %d-%d:", currentProcess.getStartedAt(), currentProcess.getFinishedAt()),   
            currentProcess.trace()        
        );
        System.out.println(batch + "\n===");
        try {
            Thread.sleep(500); // pause 500 milliseconds (0.5 seconds)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore interrupt
        }
    }

    private void printContextSwitch() {
        String ctxSwitch = String.format(
            "%-15s %s",
            String.format("time %d-%d:", currentTime, currentTime + contextSwitch),   // time column
            "Context Switching"
        );
        System.out.println(ctxSwitch + "\n===");
        try {
            Thread.sleep(500); // pause 500 milliseconds (0.5 seconds)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore interrupt
        }
    }

    // private void printProcessStatuses() {
    //     for (Process p : processes) {
    //         String status = p.getFinishedAt() != -1 ? "terminated" : "ready";
    //         System.out.println(String.format("  %s : %s", p.getName(), status));
    //     }
    //     System.out.println();
    // }
    
    private void printStats(){
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           SJF Non-Preemptive Scheduling stats            ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
        super.printStatsDetials(finishedProcesses, busyTime, idleTime, ctxSwitchTime);
    }
}
