import java.util.concurrent.Semaphore;
import java.io.PrintStream;
import java.io.File;
import java.io.FileOutputStream;

public class Bank {
    public static final int NUM_TELLERS = 3;
    public static final int NUM_CUSTOMERS = 50;

    // Shared resources and semaphores
    public static Semaphore bankDoor = new Semaphore(2);
    public static Semaphore safe = new Semaphore(2);
    public static Semaphore manager = new Semaphore(1);
    public static Semaphore queueLock = new Semaphore(1);
    public static Semaphore counterLock = new Semaphore(1);
    public static Semaphore bankOpen = new Semaphore(0);
    public static Semaphore printOrder = new Semaphore(1);

    public static int remainingCustomers = NUM_CUSTOMERS;

    static private int nextTellerIndex = 0;
    static private Teller[] tellers;

    public static Teller getNextAvailableTeller() {
        int attempts = 0;
        while (attempts < NUM_TELLERS) {
            Teller teller = tellers[nextTellerIndex];
            nextTellerIndex = (nextTellerIndex + 1) % NUM_TELLERS;
            if (teller.isAvailable()) {
                return teller;
            }
            attempts++;
        }
        return tellers[nextTellerIndex];
    }

    public static void main(String[] args) {
        try {
            File outputFile = new File("output.txt");
            if (outputFile.exists()) {
                outputFile.delete();
            }

            PrintStream fileOut = new PrintStream(new FileOutputStream("output.txt", true));
            PrintStream multiOut = new PrintStream(System.out) {
                @Override
                public void println(String x) {
                    super.println(x);
                    fileOut.println(x);
                }
            };
            System.setOut(multiOut);

            tellers = new Teller[NUM_TELLERS];
            Customer[] customers = new Customer[NUM_CUSTOMERS];

            // Start teller
            for (int i = 0; i < NUM_TELLERS; i++) {
                tellers[i] = new Teller(i);
                tellers[i].start();
            }

            // Wait all teller to be ready
            for (int i = 0; i < NUM_TELLERS; i++) {
                tellers[i].waitUntilReady();
            }
            bankOpen.release(NUM_CUSTOMERS);

            // Start customers
            for (int i = 0; i < NUM_CUSTOMERS; i++) {
                customers[i] = new Customer(i);
                customers[i].start();
            }

            // Wait all customers to complete
            for (Customer customer : customers) {
                customer.join();
            }

            // Signal teller to finish
            for (Teller teller : tellers) {
                teller.signalShutdown();
            }

            // Wait for teller to complete
            for (Teller teller : tellers) {
                teller.join();
            }

            System.out.println("The bank closes for the day.");
            fileOut.close();

        } catch (Exception e) {
            System.err.println("Error with file operations: " + e);
        }
    }
}