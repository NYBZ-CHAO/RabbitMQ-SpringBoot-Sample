package com.zysaaa;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan("com.zysaaa.service")
public class Config {

  public static final String NORMAL_QUEUE_NAME = "normal_queue_name_for_delay";
  public static final String DELAY_QUEUE_NAME = "delay_queue_name";

  public static final String NORMAL_EXCHANGE_NAME = "normal_exchange_name_for_delay";
  public static final String DELAY_EXCHANGE_NAME = "delay_exchange_name";

  /** 注意，分别为 .normal形式和 .dead形式，匹配 exchange 和 queue 的 bindingkey。 */
  public static final String NORMAL_ROUTING_KEY = "msg.normal";
  public static final String DELAY_ROUTING_KEY = "msg.delay";

  @Bean
  public AmqpTemplate amqpTemplate(@Autowired ConnectionFactory amqpConnectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(amqpConnectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter());
    return rabbitTemplate;
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }


  /**
   * dead-msg 会带着 {@link #DELAY_ROUTING_KEY} 的 RoutingKey 由 {@link #DELAY_EXCHANGE_NAME} 进行转发
   *
   * @see <a href="https://www.rabbitmq.com/dlx.html">Dead Letter Exchanges</a>
   * @see #DELAY_ROUTING_KEY
   * @see #DELAY_EXCHANGE_NAME
   * @return 队列。
   */
  @Bean
  public Queue queue() {
    return QueueBuilder.durable(NORMAL_QUEUE_NAME)
      .withArgument("x-dead-letter-exchange", DELAY_EXCHANGE_NAME)
      .withArgument("x-dead-letter-routing-key", DELAY_ROUTING_KEY)
      .withArgument("x-message-ttl", 5000)
      .build();
  }

  @Bean
  public Queue deadMsgQueue() {
    return new Queue(DELAY_QUEUE_NAME); // durable, non-exclusive and non auto-delete.
  }

  @Bean
  public TopicExchange exchange() {
    return new TopicExchange(NORMAL_EXCHANGE_NAME);
  }

  @Bean
  public TopicExchange exchangeForDeadMsg() {
    return new TopicExchange(DELAY_EXCHANGE_NAME);
  }

  @Bean
  public Binding binding() {
    return BindingBuilder.bind(queue()).to(exchange()).with("*.normal");   // 转发routingkey格式为 *.normal 的信息。
  }

  @Bean
  public Binding bindingWithDead() {
    return BindingBuilder.bind(deadMsgQueue()).to(exchangeForDeadMsg()).with("*.delay");   // 转发routingkey格式为 *.dead 的信息。
  }
}
