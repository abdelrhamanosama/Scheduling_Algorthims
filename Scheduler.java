import java.util.List;

public abstract class Scheduler {
    abstract void  run();
    public void printStatsDetials(List<Process> finishedProcesses , int busyTime , int idleTime, int ctxSwitchTime) {
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
