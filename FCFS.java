import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class FCFS extends  Scheduler {
    LinkedList<Process> processes;
    private int currentTime = 0;
    private final int contextSwitch = 2;
    private List<Process> finishedProcesses;
    private int busyTime;
    private int idleTime;
    private int ctxSwitchTime;

    public FCFS(LinkedList<Process> processes) {
        this.processes = processes;
        this.finishedProcesses = new LinkedList<>(); 
        this.busyTime = 0;
        this.idleTime = 0;
        this.ctxSwitchTime = 0;
    }

    @Override
    public void run() {
        System.out.println("╔═════════════════════════════════════════════════════╗");
        System.out.println("║       First come First Serve Scheduler Trace        ║");
        System.out.println("╚═════════════════════════════════════════════════════╝\n");
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        for (Process p : processes) {

            if (currentTime < p.getArrivalTime()) {
                idleTime += p.getArrivalTime() - currentTime;
                currentTime = p.getArrivalTime();
            }

            p.setStartedAt(currentTime);
            p.setResponseTime(p.getStartedAt() - p.getArrivalTime()); 
            currentTime += p.getBurstTime();
            p.setFinishedAt(currentTime);
            p.calculateAllTimes();
            busyTime += p.getBurstTime();

            if (!p.equals(processes.getLast())) {
                ctxSwitchTime += contextSwitch;
                currentTime += contextSwitch;
            }

            finishedProcesses.add(p);
            printProcessStatuses();
        }

        printStats();
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
    private void printStats(){
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║       First come First Serve Scheduler Stats   ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
        super.printStatsDetials(finishedProcesses, busyTime, idleTime, ctxSwitchTime);
    }
    
}
