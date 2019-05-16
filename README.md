# springboot-rabbitmq
springboot 集成rabbitmq 开箱即用
#### 测试接口：
  ```
    测试例子中，交换机默认testExchange  消息队列默认testQueue
    
    1.推消息
    http://localhost:8667/rabbitmq/sendMsg?str=zaihello
    2.拉消息
    http://localhost:8667/rabbitmq/getMsg
    3.删除交换机
    http://localhost:8667/rabbitmq/exchangeDelete?exchangeName=xxxxx
    4.删除队列
    http://localhost:8667/rabbitmq/queueDelete?queueName=xxxxxx
  ```

