Bank Simulation Project
======================

Files Included:
--------------
1. Bank.java - Driver class that manage bank simulation, including semaphores and thread coordination
2. Customer.java - Implement customer thread behavior and transaction processing
3. Teller.java - Implement teller thread behavior and resource management
4. TransactionType.java - Enum defining transaction types (DEPOSIT, WITHDRAWAL)
5. output.txt - Generated output file showing simulation results

Compilation Instructions:
-----------------------
1. Ensure Java JDK is installed
2. Open terminal/command prompt in the project directory
3. Compile all files:
   javac *.java

Running Instructions:
-------------------
1. After compilation, run:
   java Bank
2. The program will create/overwrite output.txt with simulation results

Notes:
------
- Simulate a bank with 3 tellers and 50 customers
- Uses semaphores for resource management:
  * Bank door (2 permits)
  * Safe (2 permits)
  * Manager (1 permit)
- Random delays for manager interaction (5-30ms) and safe access (10-50ms)
- Customer transaction types are randomly assigned

Notes for TA:
------
- I added a method to export the output to .txt file, this is not require by the project but I made them anyway for searching and testing the output.
