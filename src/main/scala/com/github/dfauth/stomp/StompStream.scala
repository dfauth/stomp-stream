package com.github.dfauth.stomp

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path, _}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import org.springframework.messaging.simp.stomp.{StompCommand, StompDecoder}
import org.springframework.web.socket.PingMessage

import scala.concurrent.duration._

class StompStream(system: ActorSystem) extends LazyLogging{

  val decoder = new StompDecoder()
  val route = subscribe
  val brokerList = system.settings.config.getString("bootstrap.servers")
  val controller = new StompController[Message]() {
    override protected def wrap(s: String): Message = TextMessage.Strict(s)

    override protected def _onNext(m: Message): Unit = m match {
      case t: TextMessage.Strict => handleStrict(t)
    }
  }

  def authenticateToken(t: String): Boolean = true

  controller.addStompCommandConsumer((c,m) => c match {
    case StompCommand.CONNECT => controller.extractBearerToken(m).map[Boolean](t => authenticateToken(t)).filter(v => v).ifPresent { _ =>
      val response = controller.createMessage(StompCommand.CONNECTED)
      controller.publish(response);
    }
    case StompCommand.SUBSCRIBE => controller.extractTopic(m).map[Boolean](t => authenticateToken(t)).filter(v => v).ifPresent { _ =>
      val response = controller.createMessage(StompCommand.RECEIPT)
      controller.publish(response);
    }
  })

  def subscribe = {
    path("subscribe") {
      get {
        handleWebSocketMessages(
          Flow.fromSinkAndSource(Sink.fromSubscriber(controller),
            Source.fromPublisher(controller))
            .keepAlive(5.seconds, () => TextMessage.Strict(new PingMessage().getPayload.toString))
        )
      }
    }
  }

}

object StompStream {
  def apply(system: ActorSystem): StompStream = new StompStream(system)
}