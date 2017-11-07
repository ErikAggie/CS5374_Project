package edu.ttu.erikpeterson.cs5381.test.testClasses;

public class SynchronizedDeadlock {
    private final String string1 = "String1";
    private final String string2 = "String2";

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
        synchronized(string2)
        {
            synchronized (string1)
            {
                System.out.println("2 then 1");
            }
        }
    });
}
