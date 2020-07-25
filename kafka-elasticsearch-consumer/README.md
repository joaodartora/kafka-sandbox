# ADVANCED CONSUMER CONFIGURATIONS

- **Delivery Semantics**
    - **At Most Once**: offsets are commited as soon as the message batch is received. If the processing goes wrong, the message will be lost (it won’t be read again).
    - **At Least Once**: offsets are commited after the message is processed. If the processing goes wrong, the message will be read again. This can result in duplicate processing of message. Make sure your processing is idempotent (processing again the message won’t impact your systems)
    - **Exactly once**: Can be achieved for Kafka -> Kafka workflows using Kafka Streams API. For Kafka -> Sink workflows, use an idempotent consumer.

- **Poll Behaviour**
    - Kafka Consumers have a “poll” model, while many other message bus in enterprises have a “push” model.
    - This allows consumers to control where in the log they want to consume, how fast, and gives them the ability to replay events.
    - ***fetch.min.bytes*** (default 1):
        - Controls how much data you want to pull at least on each request.
        - Helps improving throughput and decreasing request number.
        - At the cost of latency
    - ***max.poll.record*** (default 500):
        - Controls how many records to receive per poll request.
        - Increase if your messages are very small and have a lot of available RAM.
        - Good to monitor how many records are polled per request.
    - ***max.partitions.fetch.bytes*** (default 1MB):
        - Maximum data returned by the broker per partition.
        - If you read from 100 partitions, you’ll need a lot of memory (RAM).
    - ***fetch.max.bytes*** (default 50MB):
        - Maximum data returned for each fetch request (covers multiple partitions).
        - The consumer performs multiple fetches in parallel.

- **Offset Commits Strategies**
    - **Easy**
        - ***enable.auto.commit*** = true & synchronous processing of batches
        - With auto-commit, offsets will be commited automatically for you at regular interval (***auto.commit.interval.ms*** = 5000 by default) every-time you call .poll()
        - If you don’t use synchronous processing, you will be in “at-most-once” behavior because offsets will be commited before your data is processed.
    - **Medium**
        - ***enable.auto.commit*** = false & manual commit of offsets
        - You control when you commit offsets and what’s the condition for commiting them.
        - Example: accumulating records into a buffer and then flushing the buffer to a database + commiting offsets then.

- **Offset Reset Behaviour**
    - A consumer is expected to read from a log continuously.
    - But if your application has a bug, your consumer can be down.
    - If kafka has a retention of 7 days, and your consumer is down for more than 7 days, the offsets are “invalid”.
    - The behaviour for the consumer is to then use:
        - ***auto.offset.rest*** = latest: will read from the end of the log.
        - ***auto.offset.rest*** = earliest: will read from the start of the log.
        - ***auto.offset.rest*** = none: will throw exception if no offset is found.
    - This can be controlled by the broker setting offset.retention.minutes.
    - If you need to replay data for a consumer group:
        - Take all the consumers from a specific group down.
        - Use ```kafka-consumer-groups``` command to set offset to what you want.
        - Restart consumers. 


