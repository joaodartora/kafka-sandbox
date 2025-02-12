# KAFKA SANDBOX

- For information on advanced **CONSUMER** settings, click here: [Project With Consumer Advanced Settings](/kafka-elasticsearch-consumer)

- For information on advanced **PRODUCER** settings, click here: [Project With Producer Advanced Settings](/kafka-twitter-producer)

## STARTING KAFKA

### In summary, for Linux:

- Download and Setup Java 8 JDK with the comand ```sudo apt install openjdk-8-jdk```
- Download & Extract the Kafka binaries from https://kafka.apache.org/downloads
- Try Kafka commands using ```bin/kafka-topics.sh``` (for example)
- Edit PATH to include Kafka (in ~/.bashrc for example) PATH="$PATH:/your/path/to/your/kafka/bin"
- Edit Zookeeper & Kafka configs using a text editor

    - *zookeeper.properties*: dataDir=/your/path/to/data/zookeeper

    - *server.properties*: log.dirs=/your/path/to/data/kafka

- Start Zookeeper in one terminal window: ```zookeeper-server-start.sh config/zookeeper.properties```

- Start Kafka in another terminal window: ```kafka-server-start.sh config/server.properties```

## USING THE KAFKA CLI 

### OBS: Required Zookeper and Kafka running

- **Creating, viewing and deleting topics**
    - A replication factor greater than the number of brokers in the cluster cannot be set
    - Create a topic: ```kafka-topics.sh --zookeeper localhost:2181 --topic first-topic --create --partitions 3 --replication-factor 1```
    - Display existing topics: ```kafka-topics.sh --zookeeper localhost:2181 --list```
    - Display information on a topic: ```kafka-topics.sh --zookeeper localhost:2181 --topic {topic_name} --describe```
    - Delete a topic: ```kafka-topics.sh --zookeeper localhost:2181 --topic {topic_name} --delete```

- **Producer**
    - To produce messages: ```kafka-console-producer.sh --broker-list localhost:9092 --topic {topic_name}```.
    - This command opens a console to create messages, giving the command ```ctrl + C``` it will produce the messages.
    - The ```--broker-list``` and ```--topic``` parameters are mandatory.
    - To pass properties to the producer: ```kafka-console-producer.sh --broker-list localhost:9092 --topic {topic_name} --producer-property acks = all```
    - When trying to produce for a non-existent topic, Kafka will raise a warning and create a topic with default configs (replication factor 1, partitions 1), which can be edited in server.properties

- **Consumer**
    - To consume messages: ```kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic {topic_name}```
    - The consumer will ONLY consume messages from when he is UP, not consuming messages previously produced.
   - To consume previous messages (from the beginning): ```kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic {top_name} --from-beginning```
    - **Important**: messages do not return sorted, as the order is by partition, it will be sorted only at the partition level.
    - It is possible to define a group for each consumer, the group causes messages to be distributed AMONG consumers in the group (load balancing).
    - If there are two groups connected to a consumer, the messages will go to both and be distributed internally among consumers in the group.
    - The distribution within a group is given by partitions, each consumer will consume from a partition of the topic, if there are more consumers than partitions, the consumers who “remain” - will not consume, if there are more partitions than consumers, the load is distributed among consumers
    - To place a consumer in a group, simply use the parameter ```--group {group_name}```
    - This command will replace the consumer's default group id (which is generated by kafka) with the group id passed as a parameter.
    - **Example**: ```kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic {top_name} --group {group_name}```

- **Consumer Groups**
    - To list consumer-groups: ```kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list```
    - To view infos for a specific consumer group: ```kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group {group_name}```
    - To reset the offsets: ```kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group {group_name} --reset-offsets --to-earliest --execute --topic {top_name}```
    - Resetting the offsets causes the current-offset (last offset consumed) to be 0, causing all previous messages to lag (unread messages), the log-end-offset (last offset received) does not change.
    - To change reduce / increase current-offset: ```kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group {group_name} --reset-offsets --shift-by -2 --execute --topic {top_name}```, this command will cause the current-offset to be reduced by 2.

## ADVANCED TOPIC CONFIGURATIONS

- **Topic configuration**
    - To change a topic configuration you can use the following command:
    ```kafka-configs --zookeeper localhost:2181 --entity topics --entity-name {topic_name} --add-config {config_name} --alter```
    - To describe a topic config you can use:
    ```kafka-configs --zookeeper localhost:2181 --entity topics --entity-name {topic_name} --describe```

- **Segments**
    - Topics are made of partitions, and partitions are made of segments (files)
    - Only one segment is ACTIVE (the one date is being written to)
    - Two segment settings:
        - ***log.segment.bytes***: the max size of a single segment in bytes
        - **log.segment.ms**: the time kafka will wait before commiting the segment if not full
    - Segments come with two indexes (files):
        - An offset to position index: allows Kafka where to read to find a message
        - A timestamp to offset index: allows Kafka to find messages with a timestamp
    - A smaller ***log.segment.bytes*** (size, default: 1 GB) means:
        - More segments per partitions
        - Log Compaction happens more often
        - BUT Kafka has to keep more files opened (possible too many open files error)
        - Ask yourself: how fast will i have new segments based on throughput?
    - A smaller ***log.segment.ms*** (time, default 1 week) means:
        - You set a max frequency for log compaction (more frequent triggers)
        - Maybe you want daily compaction instead of weekly?
        - Ask yourself: how often do i need log compaction to happen?

- **Log Cleanup**
    - Is the concept of making data expire according to a policy, which can be based on age of data or max size of the log.
    - Deleting data from Kafka allows to:
        - Control the size of the data on the disk, deleting obsolete data.
        - **Overall**: Limit maintenance work on the Kafka cluster.
    - How often does log cleanup happen?
        - Log cleanup happens on the partition segments.
        - Smaller / More segments means that log cleanup will happen more ofter.
        - Log cleanup shouldn’t happen too often => takes CPU and RAM resources
        - The cleaner checks for work every 15 seconds (***log.cleaner.backoff.ms***)






