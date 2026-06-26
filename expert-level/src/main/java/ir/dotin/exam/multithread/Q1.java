package ir.dotin.exam.multithread;

import java.util.concurrent.*;

public class Q1 {

    public static void main(String[] args) throws Exception {
        CompletableFuture<Integer> price =
                CompletableFuture
                        .supplyAsync(() -> 100).thenApply(i -> i*2);

        CompletableFuture<Integer> tax =
                CompletableFuture
                        .supplyAsync(() -> 20);

        CompletableFuture<Integer> total =
                price.thenCombine(
                        tax,
                        Integer::sum
                );

        System.out.println(
                total.join()
        );


        ConcurrentHashMap<String,String> cache =
                new ConcurrentHashMap<>();

        cache.put("A", "Apple");

        String value =
                cache.get("A");

    }
}
