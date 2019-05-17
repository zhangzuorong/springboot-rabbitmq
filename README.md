# springboot-rabbitmq
springboot 集成rabbitmq 开箱即用，结合朱忠华老师的《RabbitMQ实战指南》

#### 随书知识点：


  ```
  1.mandatory 参数：
      mandatory: 参数设置为true时，交换器无法根据自身的类型和路由键找到一个符合条件的队列，那么RabbitMQ会调用Basic.return命令将消息返回给生产者，如果未false,出现上述情况，则消息直接被丢弃。
  2.备份交换器：
      * 如果设置的设备交换器不存在，客户端和RabbitMQ服务端都不会有异常出现，此时消息会丢失
      * 如果备份交换器没有绑定任何队列，客户端和RabbitMQ服务端都不会有异常出现，此时消息会丢失
      * 如果备份交换器没有任何匹配的队列，客户端和RabbitMQ服务端都不会有异常出现，此时消息会丢失
      * 如果备份交换器和mandatory参数一起使用，那么mandatory参数无效
  ```


#### 测试接口：
  ```
    测试例子中，交换机默认testExchange  消息队列默认testQueue
    
    1.发送消息
    http://localhost:8667/rabbitmq/sendMsg?str=zaihello
    2.消费消息（有两种模式 push 和 pull 此处为push 推模式）
    http://localhost:8667/rabbitmq/getMsg?queueName=xxxx
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

