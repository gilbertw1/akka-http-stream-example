package example

import java.util.Random
import scala.concurrent.duration._

import akka.actor.{ActorLogging,ActorRefFactory,Props}
import akka.util.{ByteString}
import akka.stream.actor.{ActorPublisher,ActorPublisherMessage}
import akka.http.model.HttpEntity
import org.reactivestreams.Publisher

import ActorPublisherMessage._
import HttpEntity._

object RandomDataPublisher {
  def apply()(implicit arf: ActorRefFactory): Publisher[ChunkStreamPart] = {
    ActorPublisher[ChunkStreamPart](arf.actorOf(Props[RandomDataPublisher]))
  }
}

class RandomDataPublisher extends ActorPublisher[ChunkStreamPart] with ActorLogging {
  implicit val ec = context.dispatcher
  var count = 0
  val random = new Random()

  log.info("Starting Data Publisher")
  
  def receive = {
    case Request(cnt) => sendDataChunks(cnt)
    case Cancel => context.stop(self)
    case _ =>
  }

  def sendDataChunks(cnt: Long) {  
    for(i <- 1L to cnt) {
      if(isActive && totalDemand > 0) {
        count += 1
        println(s"Sending Data! ($count)")
        onNext(generateDataChunk())
      }
    }
  }

  def generateDataChunk(): ChunkStreamPart = {
    val b = new Array[Byte](1024)
    random.nextBytes(b)
    ChunkStreamPart(ByteString(b))
  }
}