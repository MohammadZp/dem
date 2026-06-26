package ir.dotin.exam.test;

import java.util.ArrayList;
import java.util.List;

public class AuditService {

    private final List<String> logs =
            new ArrayList<>();

    public void log(String msg) {

        logs.add(msg);
    }

    public int count() {

        return logs.size();
    }
}