package edu.ttu.erikpeterson.cs5381.test.testClasses;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

public class MainClass {

    private static final String MY_STRING = "";

    private static final int NUM_THREADS = 5;

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);

        ArrayList<Future<Integer>> awaitedValues = new ArrayList<>();
        for (int i=0; i<NUM_THREADS; i++) {
            final Future<Integer> integerFuture = threadPool.submit(() -> new Random().nextInt(10));
            awaitedValues.add(integerFuture);

            final Future<Integer> methodFuture = threadPool.submit(() ->
            {
                return new Random().nextInt(10);
            });
            awaitedValues.add(methodFuture);
        }

        int total = 0;
        for (Future<Integer> awaitedValue : awaitedValues) {
            try {
                total += awaitedValue.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        synchronized(MY_STRING)
        {
            System.out.println("Total: " + total);
        }

        int i=0;
        do {
            i++;
        } while (i<10);

        while ( i>0)
        {
            i--;
        }

    }
}
