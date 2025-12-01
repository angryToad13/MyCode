import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.BytesMessage;
import javax.jms.MessageConsumer;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;

public class Consumer {
    // Usage: java -cp ".;activemq-all-5.16.3.jar" Consumer tcp://localhost:61616 QUEUE_NAME fileNameProperty outputDir
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: java Consumer <brokerUrl> <queueName> <fileNameProperty> <outputDir>");
            System.exit(1);
        }

        String brokerUrl = args[0];
        String queueName = args[1];
        String fileNameProperty = args[2];
        String outputDir = args[3];

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = null;

        try {
            connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination dest = session.createQueue(queueName);
            MessageConsumer consumer = session.createConsumer(dest);

            System.out.println("Waiting for messages on queue: " + queueName);
            // receive loop - change timeout or use receive() for block forever
            while (true) {
                // wait up to 5 seconds for a message, then exit if none (adjust timeout as needed)
                Message msg = consumer.receive(5000);
                if (msg == null) {
                    System.out.println("No more messages (timeout). Exiting.");
                    break;
                }

                if (!(msg instanceof BytesMessage)) {
                    System.out.println("Skipping non-bytes message of type: " + msg.getClass().getName());
                    continue;
                }

                BytesMessage bmsg = (BytesMessage) msg;

                String filename = null;
                try {
                    Object p = msg.getObjectProperty(fileNameProperty);
                    if (p != null) filename = p.toString();
                    else filename = msg.getStringProperty(fileNameProperty);
                } catch (JMSException je) {
                    // fallback
                    System.out.println("Could not read filename property directly: " + je.getMessage());
                }

                if (filename == null || filename.trim().isEmpty()) {
                    // fallback to JMSMessageID or timestamp
                    filename = "message-" + System.currentTimeMillis() + ".bin";
                }

                // read bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                try {
                    // BytesMessage.readBytes returns -1? in JMS it returns number of bytes read or -1 when EOF
                    while ((read = bmsg.readBytes(buffer)) != -1) {
                        if (read > 0) baos.write(buffer, 0, read);
                        else break;
                    }
                } catch (JMSException e) {
                    // Some providers return 0 at end; ignore
                }

                byte[] content = baos.toByteArray();

                // ensure output dir
                File dir = new File(outputDir);
                if (!dir.exists()) dir.mkdirs();

                File outFile = new File(dir, filename);
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(content);
                    fos.flush();
                } catch (IOException ioe) {
                    System.err.println("Failed to write file " + outFile.getAbsolutePath() + ": " + ioe.getMessage());
                }

                System.out.println("Wrote file: " + outFile.getAbsolutePath() + " (" + content.length + " bytes)");
            }

            consumer.close();
            session.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) try { connection.close(); } catch (JMSException ignored) {}
        }
    }
}