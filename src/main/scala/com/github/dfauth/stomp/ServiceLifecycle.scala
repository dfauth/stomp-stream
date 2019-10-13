package com.github.dfauth.stomp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

trait ServiceLifecycle extends LazyLogging {

  implicit val system: ActorSystem
  implicit val materializer:ActorMaterializer
  implicit val executionContext = system.dispatcher
  val hostname:String
  val port:Int
  val route: Route

  def start(): Future[Http.ServerBinding] = {
    logger.info("starting the server")
    Http().bindAndHandle(route, hostname, port)
  }

  def stop(bindingFuture:Future[ServerBinding]):Unit = {
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => {
      system.terminate()
      logger.info("system terminated")
    }) // and shutdown when done
  }

}
