# ADVANCED PRODUCER CONFIGURATIONS

- **Acks**
    - **acks = 0 (no acks)**
        - No response needed
        - Lose data if broker is down
        - Uselful for data where it’s okay lose messages (ex: metrics and logs)
    - **acks = 1 (leader acks)**
        - Only leader broker need to ack
        - Not guaranteed replication
        - If an ack is not received, the producer may retry
        - Lose data if broker goes down before replication
    - **acks = all (replicas acks)**
        - Leader + replicas brokers need to acks
        - Add latency AND safety
        - No data loss if enough replicas

- **Insync Replicas**
    - ***min-insync.replicas*** is a setting used together with acks = all, he defines the number of replicas (including leader) that needs to respond that they have the data
    - Can be setted at broker or topic level (override)
    - If not enough replicas had the message, an exception will be through on producer (NOT_ENOUGH_REPLICAS)

- **Retries**
    - Define the number of retries that a message will do
    - Defaults to 0 for  Kafka <= 2.0
    - Defaults to 2147483647 for Kafka >= 2.1
    - ***retry.backoff.ms*** defines the retry interval in ms (default: 100 ms)

- **Timeouts**
    - If retries > 0 then the producer won’t try the request forever, because it’s bounded by a timeout
    - ***delivery.timeout.ms*** defines the time interval that the producer will keep retrying in ms (default: 120000 ms (2 minutes))
    - After the timeout the data will not be sent to Kafka anymore.
    - Records will be failed if they can’t be acknowledged in *delivery.timeout.ms*
    - In case of retries, there is a chance that messages will be sent out of order (if a batch has failed to be sent, for example), if you are reliyng on key-based ordering, that can be an issue
    - For this, you can set the setting who controls how many producer requests can be made in parallel to a single broker with ***max.in.flight.requests.per.connection*** (default: 5). 
    - If ordering is needed, this setting can be set to 1, at the cost of a lower throughput.

- **Idempotent Producer**
    - Guarantee that a message will not be duplicated
    - Kafka looks at the requestId that the producer sends and verify if the ack of the *requestId* has been sented yet, if already sented, Kafka only commit without duplicates
    - Idempotent producers are great to guarantee a stable and safe pipeline
    - To set the producer to be idempotent, set *producerProps.put(“enable.idempotence”, true);*

- **Message Compression**
    - Compression is enabled at Producer level and doesn’t require any configuration change in the brokers or in the consumers
    - The bigger the batch of a message, the bigger effective is the compression.
    - ***compression.type*** can be **‘none’** (default), **‘gzip’**, **‘lz4’** or **‘snappy’**
    - Producer awaits till the messages forms a batch to send then to Kafka
    - **Advantages**: 
        - Much smaller producer request size (compression ratio up to 4x in some cases)
        - Faster to transfer data over the network => less latency
        - Better throughput
        - Better disk utilization in Kafka (stored messages on disk are smaller)
    - **Disadvantages** (very minor):
        - Producers must commit some CPU cycles to compression.
        - Consumers must commit some CPU cycles to decompression.
    - **Overall**:
        - Consider TESTING snappy or lz4 for optimal speed/compression ratio
        - **snappy** is good for text based messages (like logs or json)
        - **gzip** has the highest compression ratio, but is not very fast
    - Consider tweaking *linger.ms* and *batch.size* to have bigger batches, and therefore more compression and higher throughput.

- **Batching**
    - By default, Kafka tries to send records as soon as possible.
        - It will have up to 5 requests in flight, meaning up to 5 messages individually sent at the same time.
        - After this, if more messages have to be sent while others are in flight, Kafka is smart and will start batching them while they wait to send them all at once.
    - This smart batching allows Kafka to increase throughput while maintaining very low latency.
    - Batches have higher compression ratio so better eficiency.

- **Linger ms**
    - The ***linger.ms*** is the number of milliseconds a producer is willing to wait before sending a batch out. (default 0)
    - By introducing some lag (for example *linger.ms*=5), we increase the chances of messages being sent together in a batch
    - So at the expense of introducing a small delay, we can increase throughput, compression and efficiency of our producer.
    - If a batch is full (see *batch.size*) before the end of the *linger.ms* period, it will be sent to Kafka right away.

- **Batch Size**
    - The ***batch.size*** is the maximum number of bytes that will be included in a batch. The default is 16KB.
    - Increasing a batch size to something like 32KB or 64KB can help increasing the compression, throughput, and efficiency of requests
    - Any message that is bigger that the batch size will not be batched
    - A batch is allocated per partition, so make sure that you don’t set it to a number that’s to high, otherwise you’ll run waste memory!
    - (**Note**: you can monitor the average batch size metric using Kafka Producer Metrics)

- **Buffer Memory**
    - If the producer produces fast than the broker can take, the records will be buffered in memory
    - ***buffer.memory*** defines the size of the send buffer, and the default value is 33554432 (32MB)
    - That buffer will fill up over time and fill back down when the throughput tp the broker increases
    - If that buffer is full (all 32MB), then the .send() method will start to block (won’t return right away)

- **Max Block ms**
    - The ***max.block.ms*** defines the time .send() will block until throwing an exception. Exceptions are basically thrown when:
        - The producer has filled up its buffer
        - The broker is not accepting any new data
        - 60 seconds has elapsed
    - If you hit an exception hit that usually means your brokers are down or overloaded as the can’t respond to requests





    

