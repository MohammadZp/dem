package ir.dotin.exam.multithread;

public class VolatileDemo {


    private static volatile boolean running = true;

    public static void main(String[] args)
            throws InterruptedException {

        Thread worker = new Thread(() -> {

            System.out.println("Worker started");

            while (running) {
                // busy work
            }

            System.out.println("Worker stopped");
        });

        worker.start();

        Thread.sleep(2000);

        System.out.println("Stopping worker...");

        running = false;

        System.out.println("Main finished");
    }
}
