package ir.dotin.exam.multithread;

public class Main {

//    public static void main(String[] args) {
//
//        Thread t1 = new Thread(() -> System.out.println("Thread A"));
//
//        Thread t2 = new Thread(() -> System.out.println("Thread B"));
//
//        t1.start();
//        t2.start();
//    }


    public static void main(String[] args)
            throws InterruptedException {

        Thread t = new Thread(() -> {
            System.out.println("Worker started Done");
            try {
                Thread.sleep(5);
            } catch(Exception e) {}

            System.out.println("Worker Done");
        });

        t.join();
        t.start();

        try {
            Thread.sleep(15);
        } catch(Exception e) {}

        System.out.println("Main Done");
    }
}
