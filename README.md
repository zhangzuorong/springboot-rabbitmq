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
    8.测试死信队列(可作为延时队列使用)
    http://localhost:8667/rabbitmq/setDLX
  ```
#### 集成spring-boot-starter-amqp：
  ```
          <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>

  ```
  ```
  配置类共四个：
  ExchangeConfig：交换机配置类
  QueueConfig：   队列配置类
  RabbitMqConfig：基本配置（连接工厂，消息队列和交换机的绑定，消息确认机制等）
  MsgSendConfirmCallBack： 消息发送到交换机确认机制
  交换机和队列的额外属性可参考以上测试代码配置
  ```
  
  ### rabbitmq 集群配置（单机集群，使用docker）
  #### 启动多个rabbitmq
  ```
  安装镜像省略。。。
  docker run -d --hostname node1 --name rabbitmq1 -p 15672:15672 -p 5672:5672 -e RABBITMQ_ERLANG_COOKIE='rabbitcookie' rabbitmq:management
  
  docker run -d --hostname node2 --name rabbitmq2 -p 15673:15672 -p 5673:5672 --link rabbitmq1:node1 -e RABBITMQ_ERLANG_COOKIE='rabbitcookie' rabbitmq:management
  
  docker run -d --hostname node3 --name rabbitmq3 -p 15674:15672 -p 5674:5672 --link rabbitmq1:node1 --link rabbitmq2:node2 -e RABBITMQ_ERLANG_COOKIE='rabbitcookie' rabbitmq:management
  参数说明：
    -d 后台进程运行
    hostname RabbitMQ主机名称
    name 容器名称
    -p port:port 本地端口:容器端口
    -p 15672:15672 http访问端口
    -p 5672:5672 amqp访问端口
    多个容器之间使用“--link”连接，此属性不能少
    Erlang Cookie值必须相同，也就是RABBITMQ_ERLANG_COOKIE参数的值必须相同
  ```
  #### 加入RabbitMQ节点到集群
  ```
  将node2节点加入node1节点的集群中：
      docker exec -it rabbitmq2 bash
      rabbitmqctl stop_app
      rabbitmqctl reset
      rabbitmqctl join_cluster --ram rabbit@node1
      rabbitmqctl start_app
      
  将node3节点加入node1节点的集群中：
    docker exec -it rabbitmq3 bash
    rabbitmqctl stop_app
    rabbitmqctl reset
    rabbitmqctl join_cluster rabbit@node1
    rabbitmqctl start_app   
    
   此时 node1节点和node2节点和node3节点便处于同一个集群中，可以在这两个节点上都执行 rabbitmqctl cluster_status 命令，可以看到相同的输出： 
     [{nodes,[{disc,[rabbit@node3,rabbit@node1]},{ram,[rabbit@node2]}]},
     {running_nodes,[rabbit@node3,rabbit@node1,rabbit@node2]},
     {cluster_name,<<"rabbit@node1">>},
     {partitions,[]},
     {alarms,[{rabbit@node3,{badrpc,nodedown}},
              {rabbit@node1,[]},
              {rabbit@node2,[]}]}]
    
    注意：上面命令中 "--ram" 为设置节点为内存节点，默认不添加则表示节点为磁盘节点，rabbitmq集群中至少要有一个磁盘节点

  ```
