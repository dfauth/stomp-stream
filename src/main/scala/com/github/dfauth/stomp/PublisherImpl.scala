package com.github.dfauth.stomp

import akka.http.scaladsl.model.ws.Message
import org.reactivestreams.{Publisher, Subscriber, Subscription}

class PublisherImpl() extends Publisher[Message] {

  private var subscriber:Option[Subscriber[_ >: Message]] = None

  override def subscribe(s: Subscriber[_ >: Message]): Unit = {
    s.onSubscribe(new Subscription(){
      override def request(n: Long): Unit = {}
      override def cancel(): Unit = {}
    })
    this.subscriber = Some(s)
  }

//  def publish(m:Envelope): Unit = {
//    this.subscriber.map { _.onNext(m.toTextMessage)}
//  }
}
