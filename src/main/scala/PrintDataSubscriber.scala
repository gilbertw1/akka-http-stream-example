package example

import akka.util.ByteString
import akka.actor.{ActorLogging,ActorRefFactory,Props}
import akka.stream.actor.{ActorSubscriber,ActorSubscriberMessage,WatermarkRequestStrategy}
import akka.http.model.HttpEntity
import org.reactivestreams.Subscriber

import HttpEntity._

object PrintDataSubscriber {
  def apply(delay: Long)(implicit arf: ActorRefFactory): Subscriber[ChunkStreamPart] = {
    ActorSubscriber[ChunkStreamPart](arf.actorOf(Props(new PrintDataSubscriber(delay))))
  }
}

import ActorSubscriberMessage._

class PrintDataSubscriber(delay: Long) extends ActorSubscriber with ActorLogging {
  log.info("Data Chunk Stream Subscription Started")
  def requestStrategy = WatermarkRequestStrategy(50)

  def receive = {
    case OnNext(chunk: ChunkStreamPart) => 
      log.info("Received Bytes ({})", chunk.data.length)
      if(delay > 0) { Thread.sleep(delay) }      
    case OnComplete => log.info("Data Chunk Stream Completed")
    case OnError(err) => log.error(err, "Data Chunk Stream Error")
    case _ => 
  }
}