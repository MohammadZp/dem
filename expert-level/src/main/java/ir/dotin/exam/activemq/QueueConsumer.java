package ir.dotin.exam.activemq;


import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class QueueConsumer implements MessageListener {
    private String consumerName;

    public QueueConsumer(String name) {
        this.consumerName = name;
    }

    public static void main(String[] args) {
        String consumerId = args.length > 0 ? args[0] : "Consumer-1";

        ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        try (Connection connection = factory.createConnection()) {
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("JOB.QUEUE");
            MessageConsumer consumer = session.createConsumer(queue);

            consumer.setMessageListener(new QueueConsumer(consumerId));

            System.out.println(consumerId + " waiting for messages...");
            Thread.sleep(60000); // Keep alive for 60 seconds

            consumer.close();
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMsg = (TextMessage) message;
            System.out.println(consumerName + " RECEIVED: " + textMsg.getText());

            // Simulate processing time
            Thread.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}