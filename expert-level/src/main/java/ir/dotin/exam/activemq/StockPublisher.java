package ir.dotin.exam.activemq;


import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class StockPublisher {
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final Boolean NON_TRANSACTED = false;
    private static final int NUM_STOCKS = 5;
    private static final String[] STOCKS = {"ORCL", "CSCO", "VMW", "ADBE", "MSFT"};

    public static void main(String[] args) {
        String url = BROKER_URL;
        if (args.length > 0) {
            url = args[0];
        }

        ConnectionFactory factory = new ActiveMQConnectionFactory(url);

        try (Connection connection = factory.createConnection()) {
            connection.start();

            Session session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic("STOCKS");
            MessageProducer producer = session.createProducer(topic);

            for (int i = 1; i <= NUM_STOCKS; i++) {
                for (String stock : STOCKS) {
                    MapMessage message = session.createMapMessage();
                    message.setString("stock", stock);
                    message.setDouble("price", 1.0 + Math.random());
                    message.setDouble("offer", 1.0 + Math.random());
                    message.setBoolean("up", Math.random() > 0.5);

                    producer.send(message);
                    System.out.println("Sent: " + stock + " - " + message.getDouble("price"));
                }
                Thread.sleep(1000);
            }

            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
