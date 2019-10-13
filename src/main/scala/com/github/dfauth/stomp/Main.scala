package com.github.dfauth.stomp

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("wsbStreamingService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  StompStreamService(system, materializer).start()
  Await.result(system.whenTerminated, Duration.Inf)

}
