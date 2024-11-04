import java.util.Random;
import java.util.concurrent.Semaphore;

public class Teller extends Thread {
    private int tellerId;
    private Random random = new Random();
    private Semaphore customerReady = new Semaphore(0);
    private Semaphore transactionRequest = new Semaphore(0);
    private Semaphore transactionResponse = new Semaphore(0);
    private Customer currentCustomer;
    private TransactionType currentTransaction;
    private volatile boolean isWorking = true;
    private volatile boolean available = true;
    private Semaphore ready = new Semaphore(0);

    public Teller(int id) {
        this.tellerId = id;
    }

    public void signalShutdown() {
        isWorking = false;
        customerReady.release();
    }

    public void waitUntilReady() throws InterruptedException {
        ready.acquire();
    }

    public void run() {
        try {
            System.out.println("Teller " + tellerId + " is ready to serve.\n" + "Teller " + tellerId + " is waiting for a customer." );
            ready.release();
//            System.out.println();

            while (isWorking) {
                customerReady.acquire();

                if (!isWorking) break;

                System.out.println("Teller " + tellerId + " is serving Customer " +
                        currentCustomer.getCustomerId());
                transactionRequest.release();
                transactionResponse.acquire();

                System.out.println("Teller " + tellerId + " is handling the " +
                        currentTransaction.toString().toLowerCase() + " transaction.");

                if (currentTransaction == TransactionType.WITHDRAWAL) {
                    System.out.println("Teller " + tellerId + " is going to the manager.");
                    Bank.manager.acquire();
                    System.out.println("Teller " + tellerId + " is getting the manager's permission.");
                    Thread.sleep(random.nextInt(26) + 5);
                    System.out.println("Teller " + tellerId + " got the manager's permission.");
                    Bank.manager.release();
                }

                System.out.println("Teller " + tellerId + " is going to the safe.");
                Bank.safe.acquire();
                System.out.println("Teller " + tellerId + " is in the safe.");
                Thread.sleep(random.nextInt(41) + 10);
                System.out.println("Teller " + tellerId + " is leaving the safe.");
                Bank.safe.release();

                System.out.println("Teller " + tellerId + " finishes Customer " +
                        currentCustomer.getCustomerId() + "'s " +
                        currentTransaction.toString().toLowerCase() + " transaction.");
                currentCustomer.signalTransactionComplete();
                setAvailable(true);

                Bank.counterLock.acquire();
                if (Bank.remainingCustomers > 1) {
                    System.out.println("Teller " + tellerId + " is ready to serve.");
                    System.out.println("Teller " + tellerId + " is waiting for a customer.");
                }
                Bank.counterLock.release();
            }
        } catch (InterruptedException e) {
            System.err.println("Error in Teller " + tellerId + ": " + e);
        }
        System.out.println("Teller " + tellerId + " is leaving for the day.");
    }

    public int getTellerId() { return tellerId; }

    public void signalCustomerArrived(Customer customer) {
        setAvailable(false);
        currentCustomer = customer;
        customerReady.release();
    }

    public Semaphore getTransactionRequest() { return transactionRequest; }

    public void setTransaction(TransactionType transaction) {
        currentTransaction = transaction;
        transactionResponse.release();
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean value) {
        available = value;
    }
}