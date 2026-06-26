package ir.dotin.exam.activemq;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class StockConsumer implements MessageListener {
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final Boolean NON_TRANSACTED = false;

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
            MessageConsumer consumer = session.createConsumer(topic);

            consumer.setMessageListener(new StockConsumer());

            System.out.println("Waiting for stock messages...");
            Thread.sleep(10000); // Wait 10 seconds

            consumer.close();
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        MapMessage mapMsg = (MapMessage) message;
        try {
            String stock = mapMsg.getString("stock");
            double price = mapMsg.getDouble("price");
            double offer = mapMsg.getDouble("offer");
            boolean up = mapMsg.getBoolean("up");

            System.out.println("Stock: " + stock +
                    ", Price: " + price +
                    ", Offer: " + offer +
                    ", Up: " + up);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
