package ir.dotin.exam.activemq;


import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class QueueProducer {
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final Boolean NON_TRANSACTED = false;

    public static void main(String[] args) {
        ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);

        try (Connection connection = factory.createConnection()) {
            connection.start();

            Session session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);

            // KEY DIFFERENCE: createQueue() instead of createTopic()
            Queue queue = session.createQueue("JOB.QUEUE");
            MessageProducer producer = session.createProducer(queue);

            // Send 10 messages
            for (int i = 1; i <= 10; i++) {
                TextMessage message = session.createTextMessage("Job #" + i);
                producer.send(message);
                System.out.println("Sent: " + message.getText());
                Thread.sleep(500);
            }

            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
