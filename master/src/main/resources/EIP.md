The foundational work by Gregor Hohpe and Bobby Woolf, *Enterprise Integration Patterns*, defines a comprehensive catalog of **65 patterns** for message-based system integration. They are organized into several logical categories that guide the flow of a message from one application to another.

Here is a comprehensive list of the Enterprise Integration Patterns, structured by their categories, relevant to your Senior Developer exam preparation.

### 🏛️ 1. Messaging Systems
These foundational patterns establish the overall architecture and philosophy for an integration solution.

*   **Message Channel**: The core concept of a conduit connecting applications.
*   **Message**: The data packet itself, sent between systems.
*   **Message Router**: The core pattern for directing a message based on specific conditions.
*   **Message Translator**: The core pattern for converting a message from one format to another to accommodate different systems.
*   **Message Endpoint**: The component that connects an application to a messaging channel to send or receive messages.

### 📨 2. Messaging Channels
These patterns define the nature of the "pipe" or connection between systems.

*   **Point-to-Point Channel**: A channel that guarantees exactly one receiver will consume the message.
*   **Publish-Subscribe Channel**: A channel that broadcasts an event to all interested receivers.
*   **Datatype Channel**: A channel that only accepts messages of a specific data type.
*   **Invalid Message Channel**: A channel for handling messages that cannot be delivered.
*   **Dead Letter Channel**: A channel for storing messages that cannot be processed, often for later analysis or manual intervention.
*   **Guaranteed Delivery**: A pattern ensuring a message is delivered even if the messaging system fails.
*   **Channel Adapter**: A component that connects an external application to the messaging system.
*   **Messaging Bridge**: A component that connects two different messaging systems.
*   **Message Bus**: An architecture that enables separate applications to work together in a de-coupled fashion.

### 🧱 3. Message Construction
These patterns define the structure, intent, and handling of the message itself.

*   **Command Message**: A message that invokes a specific procedure or command on the receiver.
*   **Document Message**: A message containing data for a receiver to process.
*   **Event Message**: A message that notifies a receiver of a change in state or a significant occurrence. This is closely related to **Domain Events** in DDD.
*   **Request-Reply**: A pattern where a sender expects a response to its message.
*   **Return Address**: A field in a message specifying where to send a reply.
*   **Correlation Identifier**: A field in a message that allows a requestor to match a reply with the original request.
*   **Message Sequence**: A pattern for breaking a large message into a sequence of smaller ones.
*   **Message Expiration**: A mechanism for indicating when a message is stale and should not be processed.
*   **Format Indicator**: A field that specifies the data format of the message's payload.

### 🧭 4. Message Routing
These patterns control the flow of messages, directing them from a sender to the correct receiver(s).

*   **Content-Based Router**: Routes a message based on its content.
*   **Message Filter**: A router that removes unwanted messages from a channel.
*   **Dynamic Router**: A router whose routing logic is not fixed at design time.
*   **Recipient List**: A router that sends a single message to a list of specified recipients.
*   **Splitter**: A router that breaks a message containing multiple elements into separate messages for processing.
*   **Aggregator**: A router that combines the results of related messages into a single message for processing as a whole.
*   **Resequencer**: A router that puts a stream of related but out-of-sequence messages back into the correct order.
*   **Composed Message Processor**: A router that handles a message consisting of multiple elements, each requiring different processing, while maintaining the overall flow.
*   **Scatter-Gather**: A router that sends a message to multiple recipients and then combines their replies.
*   **Routing Slip**: A router that defines a series of processing steps for a message that can vary at runtime.
*   **Process Manager**: A router that maintains the state of a message as it passes through a complex process flow.
*   **Message Broker**: A central router responsible for mediating communication between applications.

### 🔄 5. Message Transformation
These patterns change the content or format of a message to make it compatible with a receiving system.

*   **Envelope Wrapper**: A pattern for adding metadata to a message, such as encryption or header fields.
*   **Content Enricher**: Adds missing data to a message that the sender didn't have.
*   **Content Filter**: Removes data elements from a message that are not needed by the receiver.
*   **Claim Check**: A pattern that reduces a message's size by storing the payload and sending a "receipt" for retrieval.
*   **Normalizer**: A translator that converts semantically equivalent messages from different formats into a common format.
*   **Canonical Data Model**: A standard data format that all applications in a system use to communicate.

### 🎯 6. Messaging Endpoints
These patterns define how an application produces or consumes messages.

*   **Messaging Gateway**: An interface that encapsulates the messaging system's API and hides its complexity from the application.
*   **Messaging Mapper**: A component that converts between domain objects and the messaging infrastructure.
*   **Transactional Client**: A client that uses transactions to ensure message processing is reliable.
*   **Polling Consumer**: A consumer that actively checks a channel for messages when it is ready.
*   **Event-Driven Consumer**: A consumer that automatically receives and processes messages as they become available.
*   **Competing Consumers**: Multiple consumers on the same channel that process messages concurrently.
*   **Message Dispatcher**: A component that distributes messages from a channel to multiple consumers.
*   **Selective Consumer**: A consumer that chooses to receive only certain messages from a channel.
*   **Durable Subscriber**: A subscriber that does not miss messages even while it is not actively listening.
*   **Idempotent Receiver**: A receiver that can handle duplicate messages without adverse effects.
*   **Service Activator**: A pattern where a message triggers the invocation of a service.

### ⚙️ 7. System Management
These patterns address the monitoring, control, and testing of a complex, message-based system.

*   **Control Bus**: A system-wide management channel that allows administrators to monitor and control the messaging system.
*   **Detour**: A pattern for temporarily routing a message through additional steps, such as for validation or debugging.
*   **Wire Tap**: A simple pattern that allows you to inspect messages flowing through a channel without affecting the main flow.
*   **Message History**: A pattern that tracks the path a message takes as it flows through the system.
*   **Message Store**: A central database that archives all messages for reporting, auditing, and recovery.
*   **Smart Proxy**: A component that performs monitoring or other management tasks on behalf of an endpoint.
*   **Test Message**: A dummy message sent to verify a system is working correctly.
*   **Channel Purger**: A mechanism to remove old or stale messages from a channel.