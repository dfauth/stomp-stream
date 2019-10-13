package com.github.dfauth.stomp

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import com.typesafe.scalalogging.LazyLogging
import org.reactivestreams.Publisher
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TestSpec extends FlatSpec with Matchers with BeforeAndAfterEach with LazyLogging {

  implicit val system = ActorSystem("TestSpec")
  implicit val materializer = ActorMaterializer()

//  var thing:WsStreamingService = null
//  var f:Future[Http.ServerBinding] = null
//
//  override protected def beforeEach(): Unit = {
//    thing = stomp.WsStreamingService(system, materializer)
//    f = thing.start()
//  }
//
//  override protected def afterEach(): Unit = {
//    this.thing.stop(f)
//  }
//
//  "a stream" should "be set up between websocket client and server" in {
//    val publisher = PublisherImpl(AuthMessage("gjkdghkghlkfjghlkjhg"))
//    ThingyImpl(system, publisher).subscribe("ws://127.0.0.1:8081/subscribe") {
//      case x =>
//        logger.info(s"received: ${x}")
//    }
//
//    Thread.sleep(30 * 1000)
//    publisher.publish(SubscribeMessage(Seq("rfqEvents")))
//    Thread.sleep(10 * 1000)
//  }

}

case class ThingyImpl(system:ActorSystem, publisher:Publisher[Message]) extends Thingy

trait Thingy extends LazyLogging {

  implicit val system:ActorSystem
  implicit val materializer:ActorMaterializer = ActorMaterializer()

  implicit val publisher:Publisher[Message]

  def subscribe(url: String)(pf:PartialFunction[Message, Unit]):Unit = {
    val incoming:Sink[Message, Future[Done]] = Sink.foreach[Message](pf.applyOrElse(_, (z:Message) => logger.info(s"received unlandled message ${z}")))
    val outgoing = Source.fromPublisher(publisher)

    val webSocketFlow:Flow[Message, Message, Future[WebSocketUpgradeResponse]] = Http().webSocketClientFlow(WebSocketRequest(url))

    val (upgradeResponse, closed) =
      outgoing
        .viaMat(webSocketFlow)(Keep.right)
        .toMat(incoming)(Keep.both)
        .run()

    val connected = upgradeResponse.flatMap { upgrade =>
      logger.info(s"upgrade: ${upgrade}")
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Future.successful(Done)
      } else {
        throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
      }
    }
    connected.onComplete(println)
    closed.foreach(_ => println("closed"))
  }
}
