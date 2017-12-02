package edu.ttu.erikpeterson.cs5381.test.testClasses;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockExample {

    private ReentrantLock lock1 = new ReentrantLock();
    private ReentrantLock lock2 = new ReentrantLock();

    private Thread thread1 = new Thread() {
        public void run() {
            lock1.tryLock();
            lock2.lock();
            System.out.println("1 then 2");
            lock2.unlock();
            lock1.unlock();
        }
    };

    private Thread thread2 = new Thread() {
        public void run() {
            lock2.lock();
            for ( int i=0; i<100; i++)
            {
                lock1.tryLock();
                System.out.println("2 then 1");
            }
            lock2.unlock();
            lock1.unlock();
        }
    };
}
