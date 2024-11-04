import java.util.Random;
import java.util.concurrent.Semaphore;

public class Customer extends Thread {
    private int customerId;
    private TransactionType transaction;
    private Random random = new Random();
    private Semaphore transactionComplete;
    private Teller assignedTeller;

    public Customer(int id) {
        this.customerId = id;
        this.transaction = random.nextBoolean() ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL;
        this.transactionComplete = new Semaphore(0);
    }

    public void run() {
        try {
            Bank.printOrder.acquire();
            System.out.println("Customer " + customerId + " wants to perform a " +
                    transaction.toString().toLowerCase() + " transaction.");
            Bank.printOrder.release();

            Bank.bankOpen.acquire();
            Bank.bankDoor.acquire();
            System.out.println("Customer " + customerId + " is going to the bank.");
            System.out.println("Customer " + customerId + " is getting in line.");
            System.out.println("Customer " + customerId + " is selecting a teller.");

            Bank.queueLock.acquire();
            assignedTeller = Bank.getNextAvailableTeller();
            Bank.queueLock.release();

            System.out.println("Customer " + customerId + " goes to Teller " +
                    assignedTeller.getTellerId() + ".");
            System.out.println("Customer " + customerId + " introduces itself to Teller " +
                    assignedTeller.getTellerId() + ".");

            assignedTeller.signalCustomerArrived(this);
            assignedTeller.getTransactionRequest().acquire();

            System.out.println("Customer " + customerId + " asks for a " +
                    transaction.toString().toLowerCase() + " transaction.");
            assignedTeller.setTransaction(transaction);

            transactionComplete.acquire();
            System.out.println("Customer " + customerId + " thanks Teller " +
                    assignedTeller.getTellerId() + " and leaves.");

            Bank.bankDoor.release();

            Bank.counterLock.acquire();
            Bank.remainingCustomers--;
            Bank.counterLock.release();

        } catch (InterruptedException e) {
            System.err.println("Error in Customer " + customerId + ": " + e);
        }
    }

    public int getCustomerId() { return customerId; }
    public void signalTransactionComplete() { transactionComplete.release(); }
}