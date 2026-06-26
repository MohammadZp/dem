package ir.dotin.exam.test.sample;

class AuditService {

    public void audit(String message) {
        writeToFile(message);
    }

    public void writeToFile(String msg) {
        System.out.println("Writing to disk: " + msg);
    }
}