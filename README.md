# springboot-rabbitmq
springboot 集成rabbitmq 开箱即用
#### 测试接口：
  ```
    测试例子中，交换机默认testExchange  消息队列默认testQueue
    
    1.发送消息
    http://localhost:8667/rabbitmq/sendMsg?str=zaihello
    2.消费消息（有两种模式 push 和 pull 此处为push 推模式）
    http://localhost:8667/rabbitmq/getMsg
    2.1.消费消息（有两种模式 push 和 pull 此处为pull 拉模式）
    http://localhost:8667/rabbitmq/getMsgTwo
    3.删除交换机
    http://localhost:8667/rabbitmq/exchangeDelete?exchangeName=xxxxx
    4.删除队列
    http://localhost:8667/rabbitmq/queueDelete?queueName=xxxxxx
    5.将已绑定的队列和交换器解绑
    http://localhost:8667/rabbitmq/queueUnbind?queueName=testQueue&exchange=testExchange
    6.测试交换器与交换器之间的绑定
    http://localhost:8667/rabbitmq/sendMsgTwo
    7.测试备份交换机
    http://localhost:8667/rabbitmq/sendMsgThr?msg=ceshi&normalKey=normalKey
  ```

