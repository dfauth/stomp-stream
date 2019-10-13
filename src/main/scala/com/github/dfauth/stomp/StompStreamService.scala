package com.github.dfauth.stomp

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

case class StompStreamService(override val system:ActorSystem, override val materializer: ActorMaterializer, override val hostname:String = "0.0.0.0", override val port:Int = 8081) extends ServiceLifecycle {
  override val route:Route = StompStream(system).route
}

