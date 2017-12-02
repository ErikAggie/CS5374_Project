package edu.ttu.erikpeterson.cs5381.test.testClasses;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockExample {

    private ReadWriteLock lock1 = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock lock2 = new ReentrantReadWriteLock();

    private Thread thread1 = new Thread() {
        public void run() {
            lock1.readLock().lock();
            lock2.writeLock().lock();
            System.out.println("1 then 2");
            lock2.writeLock().unlock();
            lock1.readLock().unlock();
        }
    };

    private Thread thread2 = new Thread() {
        public void run() {
            lock2.readLock().lock();
            lock1.writeLock().lock();
            System.out.println("1 then 2");
            lock1.writeLock().unlock();
            lock1.readLock().unlock();
        }
    };


}
