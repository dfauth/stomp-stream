package com.github.dfauth.stomp

import java.nio.ByteBuffer

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path, _}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import org.springframework.messaging.simp.stomp.{StompCommand, StompDecoder}
import org.springframework.web.socket.PingMessage

import scala.concurrent.duration._
import scala.util.Try

class StompStream(system: ActorSystem) extends LazyLogging{

  val decoder = new StompDecoder()
  val route = subscribe ~ static
//  val brokerList = system.settings.config.getString("bootstrap.servers")
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
        try {
          handleWebSocketMessages(
            Flow.fromSinkAndSource(Sink.fromSubscriber(controller),
              Source.fromPublisher(controller))
              .keepAlive(5.seconds, () => TextMessage.Strict(new String(new PingMessage(ByteBuffer.wrap("ping".getBytes)).getPayload.array())))
          )
        } catch {
          case t:Throwable => logger.error(t.getMessage, t)
            throw t
        }
      }
    }
  }

  def static =
    path("") {
      getFromResource("static/index.html")
    } ~ pathPrefix("") {
      getFromResourceDirectory("static")
    } ~ pathPrefix("webjars") {
      getFromDirectory("static/webjars")
    }
}

object StompStream {
  def apply(system: ActorSystem): StompStream = new StompStream(system)
}