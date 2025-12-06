
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/*
   make two threads one to keep adding processes to the scheudler and one to run the schdeuler to see the simulation
*/

@SuppressWarnings({"unused", "unchecked"})
public class MQScheduler implements Scheduler{
   private final int NUMBER_OF_QUEUES = 4;
   private final int QUANTUM = 4;
   private Queue<Process>[] queues;
   private Queue<Process>[] priorityQueues;
   private PriorityQueue<Process> processes;
   private Process currentProcess;
   private Process previousProcess;
   private int contextSwitch;
   private int currentTime;
   // CPU utilization tracking
   private int busyTime;        // time units CPU spent executing processes
   private int idleTime;        // time units CPU was idle (no ready processes)
   private int ctxSwitchTime;   // time units spent on context switches
   private List<Process> finishedProcesses;

   public MQScheduler() {
      processes = new PriorityQueue<>(
        Comparator.comparingInt(Process::getArrivalTime)
            .thenComparingInt(Process::getProcessId)
      );

      finishedProcesses = new LinkedList<>();
      contextSwitch = 2;
      currentProcess = null;
      currentTime = 0;
      busyTime = 0;
      idleTime = 0;
      ctxSwitchTime = 0;
   }

   public MQScheduler(LinkedList<Process> rawProcesses) {
      this();
      initiateProcesses(rawProcesses);
      initQueues();
      initPriorityQueues();
   }

   private void initiateProcesses(LinkedList<Process> rawProcesses) {
      for(var process : rawProcesses) {
         processes.add(process);    // sorting all the processes based on their arrival time
      }    
   }

   private void initQueues() {
      queues = (Queue<Process>[]) new Queue[NUMBER_OF_QUEUES];
      for(int i = 0; i < NUMBER_OF_QUEUES; i ++) {
         queues[i] = new LinkedList<>();
      }

      while(!processes.isEmpty()) {
         var process = processes.element();
         if(process.getArrivalTime() == 0) { // initially put the processes with arrival time = 0
            addProcesstoQueue(process);
            processes.poll();
         } else {
            break;
         }
      }
   }

   private void initPriorityQueues() {
      priorityQueues = (Queue<Process>[]) new Queue[NUMBER_OF_QUEUES];
      for(int i = 0; i < NUMBER_OF_QUEUES; i ++) {
         priorityQueues[i] = new PriorityQueue<>(
        Comparator.comparingInt(Process::getArrivalTime)
                  .thenComparingInt(Process::getProcessId)
);

      }

      while(!processes.isEmpty()) {
         var process = processes.poll();
         if(process.getArrivalTime() != 0)   // put the rest of the processes in the PQ
            addProcessToPriorityQueue(process);
      }
   }

   public void run() {
      System.out.println("╔════════════════════════════════════════════════╗");
      System.out.println("║       Multi-Level Queue Scheduler Trace        ║");
      System.out.println("╚════════════════════════════════════════════════╝\n");

      runScheduler();      
      printStats();
   }

   public void runScheduler() {
      // main loop
      while (hasPendingProcesses()) {
         previousProcess = currentProcess;

         // 1) move arrivals from future priority queues into ready queues
         fillQueues();

         // 2) select highest-priority non-empty ready queue
         int selected = selectHigherPriority();

         // all Qs are empty -- no ready processes -- arrival time not reached yet
         // advance time to next arrival (fast-forward)
         if (selected == -1) {
            Integer next = nextArrivalTimeAny();   // to run the first non-empty Q
            if (next == null) {               
               break;   // nothing left -- all processes finished
            }            
            int old = currentTime;
            int ny = Math.max(currentTime + 1, next);
            idleTime += (ny - old);
            currentTime = ny;  // fast-forward time to next arrival
            continue;
         }

         // we have a process to run
         var proc = queues[selected].poll();
         currentProcess = proc;

         if(previousProcess != null && currentProcess != null && previousProcess.getProcessId() != currentProcess.getProcessId()) {            
            printContextSwitch();
            currentTime += contextSwitch;
            // account for context-switch time
            ctxSwitchTime += contextSwitch;
         }

         // compute time until next arrival from a higher priority queue
         Integer nextHigher = nextArrivalTimeHigherPriority(selected);
         int timeUntilNextHigher = (nextHigher == null) ? Integer.MAX_VALUE : Math.max(0, nextHigher - currentTime);
         int runFor = allowedRunningTime(selected, timeUntilNextHigher, proc);

         // set response time if this is the first time the process runs        
         if (proc.getResponseTime() == -1) {
            proc.setStartedAt(currentTime);
            proc.setResponseTime(currentTime - proc.getArrivalTime());  // response time = start - arrive
         }

         // execute for runFor time units (decrement remaining) and log each time unit
         int before = currentTime;
         for (int t = 0; t < runFor; t++) {
            proc.decrement();
            currentTime++;
            // account as busy CPU time
            busyTime++;
            fillQueues();     // move arrivals that have become ready at this time instant
            // sleep one tick so the simulation is visible
         }

         // 4) update completion / requeue
         proc.setFinishedAt(currentTime);

         printProcess(before, selected);
         if (proc.end()) {
            updateProcess(proc);            
         } else {
            queues[selected].add(proc);      // no-feedback: stays in same queue
         }          
      }
   }

   private void updateProcess(Process proc) {
      proc.setTurnaroundTime(proc.getFinishedAt() - proc.getArrivalTime());
      proc.setWaitingTime(proc.getTurnaroundTime() - proc.getBurstTime());      
      finishedProcesses.add(proc);
   }

   private int allowedRunningTime(int selected, int timeUntilNextHigher, Process proc) {
      int runFor;
      if (selected == NUMBER_OF_QUEUES - 1) {
         // FCFS: run to completion or until a higher-priority arrival
         runFor = Math.min(proc.getRemainingTime(), timeUntilNextHigher);
      } else {
         // RR: run up to quantum, to completion, or until higher-priority arrival
         runFor = Math.min(timeUntilNextHigher, Math.min(QUANTUM, proc.getRemainingTime()));
      }
      return runFor;
   }

   private void fillQueues() {
      for (int i = 0; i < NUMBER_OF_QUEUES; i++) {
         var pq = priorityQueues[i];
         while (pq != null && !pq.isEmpty() && pq.element().getArrivalTime() <= currentTime) {
            queues[i].add(pq.poll());
         }
      }
   }

   private int selectHigherPriority() {
      for (int i = 0; i < NUMBER_OF_QUEUES; i++) 
         if (!queues[i].isEmpty()) 
            return i;               
      return -1;
   }

   private boolean hasPendingProcesses() {   // check if there are processes not finished yet
      // any ready queue non-empty?
      for (int i = 0; i < NUMBER_OF_QUEUES; i++) if (queues[i] != null && !queues[i].isEmpty()) return true;
      // any future arrival?
      for (int i = 0; i < NUMBER_OF_QUEUES; i++) if (priorityQueues[i] != null && !priorityQueues[i].isEmpty()) return true;
      return false;
   }

   private Integer nextArrivalTimeAny() {    // next arrival time for any Q to start early
      Integer min = null;
      for (int i = 0; i < NUMBER_OF_QUEUES; i++) {
         var pq = priorityQueues[i];
         if (pq != null && !pq.isEmpty()) {
            int a = pq.element().getArrivalTime();
            if (min == null || a < min) min = a;
         }
      }
      return min;
   }

   private Integer nextArrivalTimeHigherPriority(int currentQueue) {    // next arrival time for higher Qs
      Integer min = null;
      for (int i = 0; i < currentQueue; i++) {
         var pq = priorityQueues[i];
         if (pq != null && !pq.isEmpty()) {
            int a = pq.element().getArrivalTime();
            if (min == null || a < min) min = a;
         }
      }
      return min;
   }

   private void addProcesstoQueue(Process process) {
      int q = process.getType().getQueueIndex();   
      queues[q].add(process);                            
   }

   private void addProcessToPriorityQueue(Process process) {
      int q = process.getType().getQueueIndex();   
      priorityQueues[q].add(process);
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

   private void printProcess(int before, int selected) {
      String batch = String.format(
         "%-15s %s %-10s",
         String.format("time %d-%d:", before, currentTime),   
         currentProcess.trace(),
         String.format("%-6s queue", ProcessType.values()[selected])
      );
      System.out.println(batch + "\n===");
      try {
         Thread.sleep(500); // pause 500 milliseconds (0.5 seconds)
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt(); // restore interrupt
      }
   }

   public void printStats() {
      System.out.println("╔════════════════════════════════════════════════╗");
      System.out.println("║       Multi-Level Queue Scheduler Stats        ║");
      System.out.println("╚════════════════════════════════════════════════╝\n");

      if (finishedProcesses.isEmpty()) {
         System.out.println("No finished processes to report.");
         return;
      }
      double totalTurnaround = 0;
      double totalWaiting = 0;
      double totalResponse = 0;
      finishedProcesses.sort((a, b) -> a.getProcessId() - b.getProcessId());
      System.out.println(
         String.format(
            "%-12s %-13s %-13s %-11s %-12s %-13s %-13s %-16s %-13s",
            "Process_id", "Type", "Arrival_time", "Burst_time", "Started_at", "Finished_at", "Waiting_time", "Turnaround_time", "Response_time"
         )
      );
      System.out.println("-".repeat(120));
      for (Process p : finishedProcesses) {
         totalTurnaround += p.getTurnaroundTime();
         totalWaiting += p.getWaitingTime();
         totalResponse += p.getResponseTime();         
         // System.out.println(
         //    String.format(
         //       "%-15s %s",                              
         //       String.format("Process_id=%d:", p.getId()), 
         //       p.toString()
         //    ) + "\n===" 
         // );
         System.out.println(
            String.format(
               "%-12d %-13s %-13d %-11d %-12d %-13d %-13d %-16d %-13d",
               p.getProcessId(), p.getType(), p.getArrivalTime(), p.getBurstTime(), p.getStartedAt(), 
               p.getFinishedAt(), p.getWaitingTime(), p.getTurnaroundTime(), p.getResponseTime()
            )
         );
      }
      System.out.println("-".repeat(120));

      System.out.println("╔════════════════════════════════════════════════╗");
      System.out.println("║                  Average Stats                 ║");
      System.out.println("╚════════════════════════════════════════════════╝\n");
      int n = finishedProcesses.size();
      System.out.println(String.format(
         "%-20s = %.2f\n%-20s = %.2f\n%-20s = %.2f",
         "Average turnaround", totalTurnaround / n,
         "Average waiting", totalWaiting / n,
         "Average response", totalResponse / n
      ));

      System.out.println("╔════════════════════════════════════════════════╗");
      System.out.println("║                CPU Utilization                 ║");
      System.out.println("╚════════════════════════════════════════════════╝\n");
      int totalTime = busyTime + idleTime + ctxSwitchTime;
      double utilization = totalTime == 0 ? 0.0 : (busyTime / (double) totalTime) * 100.0;
      System.out.println(String.format("%-25s = %d", "Busy time", busyTime));
      System.out.println(String.format("%-25s = %d", "Context-switch time", ctxSwitchTime));
      System.out.println(String.format("%-25s = %d", "Idle time", idleTime));
      System.out.println(String.format("%-25s = %d", "Total simulated time", totalTime));
      System.out.println(String.format("%-25s = %.2f%%", "Utilization (busy/total)", utilization));
   }   
}