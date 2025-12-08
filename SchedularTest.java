import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class SchedularTest {

    public static void main(String[] args) {

        LinkedList<Process> processes;

        String opt = helpOptions();

        if (opt.equalsIgnoreCase("A")) {
            processes = readFromFileProcesses();
        } else {
            processes = readProcessesFromInteractiveMode();
        }

        // Debug print
        System.out.println("\nProcesses loaded:");
        
        for (Process p : processes) {
            System.out.println(p.toString());
        }
        System.out.println("\t\t\t the big moment \t\t\t");
        String AlgorthimsScheduling [] = {
            "FCFS",
            "SJF nonPremptive",
            "SRT",
            "Round Robin",
            "Priority",
            "Multi Level Queue",
            "Multi Level Queue Feedback"
        };
        
        Scheduler scheduler; // interface the implemented in all class to help us make one referrence to all
        for(int i = 0 ; i < AlgorthimsScheduling.length; i++) {
            System.out.println(i + ")\t" + AlgorthimsScheduling[i]);

        }
        //  make part of take the schedulars 
        // then tries to run them by run  method
        scheduler = getScheduler(processes);
        scheduler.run();

    }
    private static Scheduler getScheduler(LinkedList<Process> p){
    Scanner cin = new Scanner(System.in);
    System.out.print("Choose algorithm: ");
    int index = cin.nextInt();

    switch (index) {
        case 0 -> {
            return new FCFS(p);
            }
        case 1 -> {
            return new SJF_NonPreemptive(p);
            }
        case 2 -> {
            return new SRTScheduler(p);
            }
        case 3 -> {
            System.out.println("please enter our time quantum");
            int quentumtime = cin.nextInt();
            return new RoundRobinScheduler(p,quentumtime);
            }
        case 4 -> {
            System.out.println("please enter is preemptive 1 or 0");
            int Preemptive = cin.nextInt();
            return new PriorityScheduler(p,(Preemptive == 1? true : false));
            }
        case 5 -> {
            return new MQScheduler(p);
            }
        case 6 -> {
            return new MLFQScheduler(p);
            }
        default -> {
            System.out.println("Invalid choice!");
            return null;
            }
    }
}

    private static String helpOptions() {
        Scanner cinOption = new Scanner(System.in);
        String opt;

        do {
            System.out.println("A) Add processes from file");
            System.out.println("B) Add processes interactively");
            opt = cinOption.next().trim();
        } while (!opt.equalsIgnoreCase("A") &&
                 !opt.equalsIgnoreCase("B"));

        return opt;
    }

    private static LinkedList<Process> readFromFileProcesses() {

        LinkedList<Process> processes = new LinkedList<>();
        boolean flag = true;

        while (flag) {
            try {
                File file = new File("input.txt");
                Scanner cin = new Scanner(file);

                int numberOfProcesses = cin.nextInt();
                System.out.println("Reading processes from file...");
                System.out.println("----------------------------------------");

                for (int i = 0; i < numberOfProcesses; i++) {
                    String name = cin.next();
                    int arrival = cin.nextInt();
                    int burst = cin.nextInt();
                    int priority = cin.nextInt();
                    String type = cin.next();

                    processes.add(new Process(
                            name,
                            i + 1,
                            arrival,
                            burst,
                            priority,
                            ProcessType.from(type)
                    ));
                }

                flag = false;
                cin.close();

            } catch (FileNotFoundException e) {
                System.out.println("ERROR: input.txt not found!");
                flag = true;
            }
        }
        System.out.println("Processes loaded successfully!");
        return processes;
    }

    private static LinkedList<Process> readProcessesFromInteractiveMode() {

        Scanner cin = new Scanner(System.in);
        LinkedList<Process> processes = new LinkedList<>();

        System.out.print("Enter number of processes: ");
        int n = cin.nextInt();

        for (int i = 0; i < n; i++) {
            System.out.println("\nProcess " + (i + 1));

            System.out.print("Name: ");
            String name = cin.next();

            System.out.print("Arrival Time: ");
            int arrival = cin.nextInt();

            System.out.print("Burst Time: ");
            int burst = cin.nextInt();

            System.out.print("Priority: ");
            int priority = cin.nextInt();

            System.out.print("Type (CPU/IO/etc): ");
            String type = cin.next();

            processes.add(new Process(
                    name,
                    i + 1,
                    arrival,
                    burst,
                    priority,
                    ProcessType.from(type)
            ));
        }
        System.out.println("Processes loaded successfully!");
        return processes;
    }
}
