import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MLFQScheduler extends  Scheduler {
    private Queue<Process>[] queues;
    private LinkedList<Process> allProcesses;
    private int currentTime;
    private final int contextSwitches;
    private int lastBoostTime;
    private Process currentProcess;

    private List<Process> finishedProcesses;

    private int busyTime;
    private int idleTime;
    private int ctxSwitchTime;

    private static final int BOOST_INTERVAL = 20;

    @SuppressWarnings("unchecked")
    public MLFQScheduler(List<Process> processes) {

        this.queues = (Queue<Process>[]) new Queue[3];
        this.queues[0] = new LinkedList<>();
        this.queues[1] = new LinkedList<>();
        this.queues[2] = new LinkedList<>();

        this.allProcesses = new LinkedList<>(processes);

        this.currentTime = 0;
        this.contextSwitches = 2;
        this.lastBoostTime = 0;
        this.currentProcess = null;

        this.finishedProcesses = new LinkedList<>();
        this.busyTime = 0;
        this.idleTime = 0;
        this.ctxSwitchTime = 0;
    }

    public void run() {
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║       Multi-Level Feedback Queue Scheduler Trace        ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝\n");

        while (!isComplete()) {

            if (currentTime - lastBoostTime >= BOOST_INTERVAL && currentTime > 0) {
                performPriorityBoost();
                lastBoostTime = currentTime;
            }

            handleArrivals();

            if (currentProcess == null) {
                currentProcess = selectNextProcess();
                if (currentProcess != null) {
                    ctxSwitchTime += contextSwitches;
                    currentTime += contextSwitches;
                }
            }

            if (currentProcess != null) {
                busyTime++;
                executeProcess(currentProcess);
                printProcessStatuses();
            } else {
                idleTime++;
                currentTime++;
            }

            updateWaitingTimes();
            
        }
        printStats();    
        calculateMetrics();
    }

    private void handleArrivals() {
        for (Process p : allProcesses) {
            if (p.getArrivalTime() == currentTime) {
                queues[0].offer(p);
                p.currentQueue = 0;
            }
        }
    }

    private Process selectNextProcess() {
        for (int i = 0; i < 3; i++) {
            if (!queues[i].isEmpty()) {
                return queues[i].poll();
            }
        }
        return null;
    }

    private void executeProcess(Process p) {
        boolean completed = p.execute(currentTime);
        currentTime++;

        if (completed) {
            p.setFinishedAt(currentTime);
            finishedProcesses.add(p);
            currentProcess = null;
        } else if (p.isQuantumExhausted()) {
            p.demote();
            queues[p.currentQueue].offer(p);
            currentProcess = null;
        }
    }

    private void performPriorityBoost() {
        LinkedList<Process> toBoost = new LinkedList<>();

        for (int i = 1; i < 3; i++) {
            toBoost.addAll(queues[i]);
            queues[i].clear();
        }

        if (currentProcess != null && currentProcess.currentQueue > 0) {
            toBoost.add(currentProcess);
            currentProcess = null;
        }

        for (Process p : toBoost) {
            p.boost();
            queues[0].offer(p);
        }

        if (!toBoost.isEmpty()) {
            System.out.println("[Time " + currentTime + "] Priority boost: " +
                    toBoost.size() + " processes moved to Q0");
        }
    }

    private void updateWaitingTimes() {
        for (Process p : allProcesses) {
            if (p.getArrivalTime() <= currentTime &&
                p.getRemainingTime() > 0 &&
                p != currentProcess &&
                !p.isQuantumExhausted()) {

                p.setWaitingTime(p.getWaitingTime() + 1);
            }
        }
    }

    private boolean isComplete() {
        for (Process p : allProcesses) {
            if (p.getRemainingTime() > 0)
                return false;
        }
        return true;
    }

    private void calculateMetrics() {
        for (Process p : allProcesses) {
            p.setTurnaroundTime(p.getFinishedAt() - p.getArrivalTime());
        }
    }

    private void printProcessStatuses() {
        for (Process p : allProcesses) {
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
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║      Multi-Level Feedback Queue Scheduler Stats   ║");
        System.out.println("╚═══════════════════════════════════════════════════╝\n");
        super.printStatsDetials(finishedProcesses, busyTime, idleTime, ctxSwitchTime);
    }
}
