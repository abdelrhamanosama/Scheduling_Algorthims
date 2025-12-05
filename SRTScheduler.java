import java.util.*;

/**
 * كلاس يطبق خوارزمية Shortest Remaining Time (SRT)
 * النسخة الاستباقية من SJF
 */
public class SRTScheduler {
    
    private List<Process> processes;              // قائمة العمليات
    private List<String> ganttChart;              // لتسجيل Gantt Chart
    private List<Integer> ganttTimes;             // أوقات Gantt Chart
    private int totalTime;                        // إجمالي الوقت
    
    /**
     * Constructor
     */
    public SRTScheduler(List<Process> processes) {
        this.processes = new ArrayList<>(processes);
        this.ganttChart = new ArrayList<>();
        this.ganttTimes = new ArrayList<>();
        this.totalTime = 0;
    }
    
    /**
     * تشغيل خوارزمية SRT
     */
    public void schedule() {
        int currentTime = 0;
        int completed = 0;
        int n = processes.size();
        String lastProcess = "";
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     SRT Scheduling Algorithm          ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // بداية Gantt Chart
        ganttTimes.add(0);
        
        // استمر حتى تنتهي جميع العمليات
        while (completed < n) {
            
            // إيجاد العملية ذات أقل remaining time
            Process shortestProcess = findShortestRemainingTime(currentTime);
            
            // إذا لم توجد عملية متاحة (IDLE)
            if (shortestProcess == null) {
                if (!lastProcess.equals("IDLE")) {
                    ganttChart.add("IDLE");
                    ganttTimes.add(currentTime);
                    lastProcess = "IDLE";
                }
                currentTime++;
                continue;
            }
            
            // تسجيل Response Time (أول مرة تبدأ)
            if (shortestProcess.getStartedAt() != -1) {
                shortestProcess.setResponseTime(currentTime - shortestProcess.getArrivalTime());
            }
            
            // إضافة إلى Gantt Chart (فقط إذا تغيرت العملية)
            if (!shortestProcess.getName().equals(lastProcess)) {
                ganttChart.add(shortestProcess.getName());
                ganttTimes.add(currentTime);
                lastProcess = shortestProcess.getName();
            }
            
            // تنفيذ العملية لوحدة زمنية واحدة
            shortestProcess.decrement();
            currentTime++;
            
            // إذا انتهت العملية
            if (shortestProcess.getRemainingTime() == 0) {
                completed++;
                shortestProcess.setFinishedAt(currentTime);
                shortestProcess.calculateAllTimes();
                
                System.out.println("✓ " + shortestProcess.getName() + 
                                 " completed at time " + currentTime);
            }
        }
        
        // إضافة الوقت النهائي
        ganttTimes.add(currentTime);
        totalTime = currentTime;
        
        System.out.println("\n✓ All processes completed!\n");
    }
    
    /**
     * إيجاد العملية ذات أقل remaining time
     */
    private Process findShortestRemainingTime(int currentTime) {
        Process shortest = null;
        int minRemainingTime = Integer.MAX_VALUE;
        
        for (Process p : processes) {
            if (p.isAvailable(currentTime)) {
                if (p.getRemainingTime() < minRemainingTime) {
                    minRemainingTime = p.getRemainingTime();
                    shortest = p;
                }
            }
        }
        
        return shortest;
    }
    
    /**
     * طباعة Gantt Chart
     */
    public void printGanttChart() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║           Gantt Chart                 ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // الخط العلوي
        System.out.print(" ");
        for (int i = 0; i < ganttChart.size(); i++) {
            System.out.print("--------");
        }
        System.out.println();
        
        // أسماء العمليات
        System.out.print("|");
        for (String process : ganttChart) {
            System.out.printf("  %-4s |", process);
        }
        System.out.println();
        
        // الخط السفلي
        System.out.print(" ");
        for (int i = 0; i < ganttChart.size(); i++) {
            System.out.print("--------");
        }
        System.out.println();
        
        // الأوقات
        for (Integer time : ganttTimes) {
            System.out.printf("%-8d", time);
        }
        System.out.println("\n");
    }
    
    /**
     * طباعة النتائج التفصيلية
     */
    public void printResults() {
        System.out.println("╔═══════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                              Detailed Results                                        ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════════════╝\n");
        
        System.out.printf("%-10s %-12s %-12s %-15s %-15s %-12s %-12s\n",
                "Process", "Arrival", "Burst", "Completion", "Turnaround", "Waiting", "Response");
        System.out.println("-------------------------------------------------------------------------------------------");
        
        for (Process p : processes) {
            System.out.println(p.toString());
        }
        
        System.out.println();
    }
    
    /**
     * حساب وطباعة المتوسطات
     */
    public void printAverages() {
        double totalTAT = 0;
        double totalWT = 0;
        double totalRT = 0;
        int n = processes.size();
        
        for (Process p : processes) {
            totalTAT += p.getTurnaroundTime();
            totalWT += p.getWaitingTime();
            totalRT += p.getResponseTime();
        }
        
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║           Averages                    ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.printf("Average Turnaround Time : %.2f\n", totalTAT / n);
        System.out.printf("Average Waiting Time    : %.2f\n", totalWT / n);
        System.out.printf("Average Response Time   : %.2f\n", totalRT / n);
        System.out.printf("Total Execution Time    : %d\n", totalTime);
        System.out.printf("CPU Utilization         : %.2f%%\n", calculateCPUUtilization());
        System.out.println();
    }
    
    /**
     * حساب CPU Utilization
     */
    private double calculateCPUUtilization() {
        int idleTime = 0;
        for (String process : ganttChart) {
            if (process.equals("IDLE")) {
                // حساب الوقت الخامل
                int index = ganttChart.indexOf(process);
                if (index < ganttTimes.size() - 1) {
                    idleTime += ganttTimes.get(index + 1) - ganttTimes.get(index);
                }
            }
        }
        
        return ((totalTime - idleTime) / (double) totalTime) * 100;
    }
    
    /**
     * طباعة ملخص سريع
     */
    public void printSummary() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         Quick Summary                 ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.println("Number of Processes: " + processes.size());
        System.out.println("Total Time: " + totalTime);
        System.out.println("Context Switches: " + (ganttChart.size() - 1));
        System.out.println();
    }
    
    /**
     * عرض جميع النتائج
     */
    public void displayAllResults() {
        printGanttChart();
        printResults();
        printAverages();
        printSummary();
    }
}