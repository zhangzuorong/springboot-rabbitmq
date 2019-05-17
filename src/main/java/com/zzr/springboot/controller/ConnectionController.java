package com.zzr.springboot.controller;

import com.rabbitmq.client.*;
import java.lang.String;
import com.zzr.springboot.config.ConnectionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * ConnectionController
 *
 * @author zzr
 * @created Create Time: 2019/5/15
 */
@RestController
@RequestMapping("/rabbitmq")
public class ConnectionController {

    @Autowired
    public ConnectionConfig connectionConfig;

    @RequestMapping("/sendMsg")
    public String sendMsg(String str) throws IOException, TimeoutException {

        Connection connection = connectionConfig.getConnectionFactory().newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道
        //创建一个持久化的，非自动删除的，绑定类型为direct的交换器
        /**
         * 参数说明
         * exchange: 交换器的名称
         * type: 交换器类型，eg:fanout direct topic
         * durable: 设置是否持久化 true表示持久化，繁殖为非持久化
         * autoDelete: 是否自动删除
         * internal: 设置是否是内置的。
         * argument: 其它一些结构化参数
         */
        channel.exchangeDeclare("testExchange","direct",true,false,null);
        /**
         * 参数说明
         * queue: 队列的名称
         * durable: 设置是否持久化。 true则设置队列为持久化
         * exclusive: 设置是否排他。 true为排他队列
         * autoDelete: 设置是否自动删除。 true为自动删除
         * arguments: 设置队列的其它一些参数
         */
        //创建一个30分钟自动删除，非排他
        Map<String,Object> args = new HashMap<>();
        args.put("x-expires",1800000);
        channel.queueDeclare("testQueue",false,false,false,args);

        /**
         * 参数说明
         * queue: 队列名称
         * exchange: 交换器的名称
         * routingKey: 用来绑定队列和交换器的路由键
         * argument: 定义绑定的一些参数
         */
        //使用路由键将交换器和队列绑定
        channel.queueBind("testQueue","testExchange","routingkey_demo");

        String msg = str;

        /**
         * 参数说明
         * routingKey: 路由键，交换器根据路由键将消息存储到相应的队列中
         * mandatory: 参数设置为true时，交换器无法根据自身的类型和路由键找到一个符合条件的队列，那么RabbitMQ会调用Basic.return命令将消息返回给生产者，如果未false,
         * 出现上述情况，则消息直接被丢弃
         * props: 消息的基本属性集
         * byte[] body: 消息体，真正需要发送的消息
         */
        channel.basicPublish("testExchange","routingkey_demo",true,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                msg.getBytes());

        /**
         * 监听器
         */
        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
                                     AMQP.BasicProperties basicProperties, byte[] body) throws IOException {
                String message = new String(body);
                System.out.println("Basic.Return返回的结果是："+ message);

                try {
                    channel.close();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
                connection.close();
            }
        });

        //关闭资源
//        channel.close();
//        connection.close();

        return "发送成功,交换机为testExchange，队列为testQueue，内容为："+str;
    }

    /**
     * 消费消息
     * @return
     * @throws IOException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    @RequestMapping("/getMsg")
    public String getMsg(String queueName) throws IOException, TimeoutException, InterruptedException {
        Address[] addresses = new Address[]{new Address("47.95.117.206",5672)};
        Connection connectionXiaoFei = connectionConfig.getConnectionFactory().newConnection(addresses);
        final Channel channelXiaoFei = connectionXiaoFei.createChannel();
        boolean autoAck = false;
        channelXiaoFei.basicQos(64);//设置客户端最多接收未被ack的消息个数
        final String[] result = {""};
        /**
         * 回调方法
         */
        Consumer consumer = new DefaultConsumer(channelXiaoFei){
            @Override
            public void handleDelivery(String consumerTag,Envelope envelope,AMQP.BasicProperties properties,byte[] body) throws IOException {
                result[0] = result[0] + new String(body)+ " ";
                System.out.println("messges===="+ new String(body));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                channelXiaoFei.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        /**
         * 上面代码中显示地设置autoAck为false,然后再接收到消息之后进行显示ack操作（channel.basicAck）,对于消费者来说这个设置是非常必要的，
         * 可以防止消息不必要的丢失
         *
         * 参数说明
         * queue: 队列的名称
         * autoAck: 设置是否自动确认，建议设置成false,即不自动确认
         * consumerTag: 消费者标签，用来区分多个消费者
         * noLocal: true则表示不能将同一个Connection中生产者发送的消息传递给这个Connection中的消费者
         * exclusive: 设置消费者的其他参数
         * callback: 设置消费者的回调函数，用来处理RabbitMQ推送过来的消息，比如DefaultConsumer
         */
        channelXiaoFei.basicConsume(queueName,autoAck,consumer);
        TimeUnit.SECONDS.sleep(5);
        channelXiaoFei.close();
        connectionXiaoFei.close();
        return StringUtils.isEmpty(result[0]) ? "无订阅消息" : "消息内容："+result[0];
    }

    /**
     * 删除交换机
     */
    @RequestMapping("/exchangeDelete")
    public String exchangeDelete(String exchangeName) throws IOException, TimeoutException {
        Connection connection = connectionConfig.getConnectionFactory().newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道
        channel.exchangeDelete(exchangeName,false);
        //关闭资源
        channel.close();
        connection.close();
        return "删除交换机"+ exchangeName +"成功";
    }

    /**
     * 删除队列
     */
    @RequestMapping("/queueDelete")
    public String queueDelete(String queueName) throws IOException, TimeoutException {
        Connection connection = connectionConfig.getConnectionFactory().newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道
        channel.queueDelete(queueName,false,false);
        //关闭资源
        channel.close();
        connection.close();
        return "删除"+queueName+"成功";
    }

    /**
     * 将已绑定的队列和交换器解绑
     */
    @RequestMapping("/queueUnbind")
    public String queueUnbind(String queueName,String exchange) throws IOException, TimeoutException {
        Connection connection = connectionConfig.getConnectionFactory().newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道
        channel.queueUnbind(queueName,exchange,"routingkey_demo");
        //关闭资源
        channel.close();
        connection.close();
        return "解除绑定成功";
    }


    /**
     * 测试交换器与交换器之间的绑定
     * @throws IOException
     * @throws TimeoutException
     */
    @RequestMapping("/sendMsgTwo")
    public String sendMsgTwo() throws IOException, TimeoutException {

        Connection connection = connectionConfig.getConnectionFactory().newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道
        //创建一个持久化的，非自动删除的，绑定类型为direct的交换器
        channel.exchangeDeclare("testExchangeTwo","direct",true,false,null);
        //将testExchangeTwo交换器与testExchange交换器绑定，消息从testExchangeTwo交换器转发到testExchange交换器
        channel.exchangeBind("testExchange","testExchangeTwo","routingkey_demo");
        //创建一个30分钟自动删除，非排他
        Map<String,Object> args = new HashMap<>();
        args.put("x-expires",1800000);
        channel.queueDeclare("testQueue",false,false,false,args);
        //使用路由键将交换器和队列绑定
        channel.queueBind("testQueue","testExchange","routingkey_demo");

        /**
         * * arguments: 设置队列的其它一些参数
         * 此处设置了消息的过期时间 单位为毫秒
         */
        String msg = "Hello World";
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.deliveryMode(2);//持久化消息
        builder.expiration("6000");//设置TTL=6000ms
        AMQP.BasicProperties properties = builder.build();
        channel.basicPublish("testExchangeTwo","routingkey_demo",
                properties,
                msg.getBytes());

        //关闭资源
        channel.close();
        connection.close();
        return "发送成功,交换机为testExchange，队列为testQueue，内容为："+msg;
    }

    /**
     * 消费消息
     * @return
     * @throws IOException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    @RequestMapping("/getMsgTwo")
    public String getMsgTwo() throws IOException, TimeoutException, InterruptedException {
        Address[] addresses = new Address[]{new Address("47.95.117.206", 5672)};
        Connection connection = connectionConfig.getConnectionFactory().newConnection(addresses);
        final Channel channel = connection.createChannel();
        channel.basicQos(64);//设置客户端最多接收未被ack的消息个数
        GetResponse response = channel.basicGet("testQueue",false);
        System.out.println(response.getBody());
        channel.basicAck(response.getEnvelope().getDeliveryTag(),false);
        return response.toString();
    }

    /**
     * 发送消息
     * 测试备份交换机
     * 可以通过在声明交换器的时候添加alternate-exchange参数来实现
     *
     * 如果发送一条消息到normalExchange上，当路由键等于normalKey的时候,消息能正确路由到normalQueue这个队列中，如果路由健设置为其它，
     * 消息不能被正确的路由到normalExchange绑定的任何队列上，此时就会发送给myAe,进而发送到unroutedQueue这个队列
     *
     * 对于备份交换器，总结了以下几种特殊情况
     *  1：如果设置的设备交换器不存在，客户端和RabbitMQ服务端都不会有异常出现，此时消息会丢失
     *  2：如果备份交换器没有绑定任何队列，客户端和RabbitMQ服务端都不会有异常出现，此时消息会丢失
     *  3：如果备份交换器没有任何匹配的队列，客户端和RabbitMQ服务端都不会有异常出现，此时消息会丢失
     *  4：如果备份交换器和mandatory参数一起使用，那么mandatory参数无效
     */
    @RequestMapping("/sendMsgThr")
    public String sendMsgThr(String msg,String normalKey) throws IOException, TimeoutException {
        Connection connection = connectionConfig.getConnectionFactory().newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道
        Map<String,Object> args = new HashMap<>();
        args.put("alternate-exchange","myAe");
        channel.exchangeDeclare("normalExchange","direct",true,false,args);
        channel.exchangeDeclare("myAe","fanout",true,false,null);
        channel.queueDeclare("normalQueue",true,false,false,null);
        channel.queueBind("normalQueue","normalExchange",normalKey);
        channel.queueDeclare("unroutedQueue",true,false,false,null);
        channel.queueBind("unroutedQueue","myAe","");

        channel.basicPublish("normalExchange","normalKey",true,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                msg.getBytes());

        //关闭资源
        channel.close();
        connection.close();
        return "发送成功。  测试代码中声明了两个交换器normalExchange，myAe，分别绑定了normalQueue和unroutedQueue两个队列，同时将myAe设置为normalExchange的备份交换器";
    }

    /**
     * 测试死信队列(可作为延时队列使用)
     *
     */
    @RequestMapping("/setDLX")
    public String setDLX() throws IOException, TimeoutException {
        Connection connection = connectionConfig.getConnectionFactory().newConnection();//创建连接
        Channel channel = connection.createChannel();//创建信道
        channel.exchangeDeclare("dlx.exchange","direct",true);//创建DLX
        channel.exchangeDeclare("normal.exchange","fanout",true);

        Map<String,Object> args = new HashMap<>();
        args.put("x-message-ttl",10000);
        args.put("x-dead-letter-exchange","dlx.exchange");//为这个队列添加DLX
        args.put("x-dead-letter-routing-key","dlx-routing-key");
        channel.queueDeclare("queue.normal",true,false,false ,args);

        channel.queueBind("queue.normal","normal.exchange","dlx-routing-key");

        channel.queueDeclare("queue.dlx",true,false,false,null);
        channel.queueBind("queue.dlx","dlx.exchange","dlx-routing-key");

        /**
         * 生产者首先发送一条携带路由键为""的消息，然后经过normal.exchange顺利的存储到queue.normal队列中，由于queue.normal设置了过期时间
         * ，10s内没有消费者消费这条消息，那么判定这条消息为过期，由于设置了DLX，过期之时，消息被丢弃给dlx.exchange中，这时找到与dlx.exchange匹配的队列queue.dlx
         */
        channel.basicPublish("normal.exchange","",
                MessageProperties.PERSISTENT_TEXT_PLAIN,"dlx".getBytes());

        return "死信队列成功";
    }


}
