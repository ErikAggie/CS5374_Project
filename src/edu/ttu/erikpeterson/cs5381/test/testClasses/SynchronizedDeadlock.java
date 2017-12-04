package edu.ttu.erikpeterson.cs5381.test.testClasses;

public class SynchronizedDeadlock {
    private final String string1 = "String1";
    private final String string2 = "String2";

    //----------------------------------------------
    // Deadlock with two variables
    //----------------------------------------------

    private Thread thread1 = new Thread() {
        public void run()
        {
            synchronized(string1)
            {
                synchronized (string2)
                {
                    System.out.println("1 then 2");
                }
            }
        }
    };

    private Thread thread2 = new Thread(() -> {
        // Make sure we're following method calls
        this.myMethod();
    });

    private void myMethod()
    {
        lockInOtherOrder();
    }

    private void lockInOtherOrder()
    {
        synchronized(string2)
        {
            synchronized (string1)
            {
                System.out.println("2 then 1");
            }
        }
    }

    //------------------------------------------------------
    // Verify that falling out of a synchronized block does
    // NOT cause a deadlock alert.
    //------------------------------------------------------

    private Thread thread3 = new Thread(() -> {
        synchronized(string2)
        {
            System.out.println("2 only");
        }

        synchronized(string1)
        {
            System.out.println("1 only");
        }
    });

    //------------------------------------------------------
    // Check locks that include synchronizing on methods.
    //------------------------------------------------------

    private Thread thread4 = new Thread() {
        public void run() {
            synchronized(string1)
            {
                synchronized(this)
                {
                    System.out.println("1 then object");
                }
            }
        }
    };

    private Thread thread5 = new Thread() {
        public void run() {
            lockWithSynchronizedMethod();
        }
    };

    private synchronized void lockWithSynchronizedMethod()
    {
        synchronized(string1) {
            System.out.println("Object then 1");
        }
    }
}
