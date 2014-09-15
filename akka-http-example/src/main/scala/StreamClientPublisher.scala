package example

import scala.collection.mutable.{Queue => MQueue}
import scala.concurrent.{Future,Await}
import scala.concurrent.duration._
import scala.async.Async.{async, await}

import akka.util.{ByteString}
import akka.actor.{ActorSystem,ActorLogging,Props}
import akka.stream.{FlowMaterializer,MaterializerSettings}
import akka.stream.actor._
import akka.http.model.HttpEntity
import org.reactivestreams.Publisher

import HttpEntity._

object StreamClientPublisher {

  def apply(port: Int)(implicit system: ActorSystem, materializer: FlowMaterializer): Publisher[ChunkStreamPart] = {
    implicit val ec = system.dispatcher
    val publisherFuture = async {
      val response = await(HttpClient.makeRequest(port, "/"))
      val processor = system.actorOf(Props[DataChunkProcessor])
      val processorSubscriber = ActorSubscriber[ByteString](processor)
      val processorPublisher = ActorPublisher[ChunkStreamPart](processor)
      response.entity.dataBytes(materializer).subscribe(processorSubscriber)
      processorPublisher
    }
    Await.result(publisherFuture, 1.seconds)
  }
}

import ActorSubscriberMessage._
import ActorPublisherMessage._

class DataChunkProcessor extends ActorSubscriber with ActorPublisher[ChunkStreamPart] with ActorLogging {
  log.info("Data Chunk Stream Subscription Started")
  val queue = MQueue[ByteString]()
  var requested = 0L

  def receive = {
    case Request(cnt) => requested += cnt; sendDataChunks()
    case Cancel => onComplete(); context.stop(self)
    case OnNext(bytes: ByteString) => queue.enqueue(bytes); sendDataChunks()
    case OnComplete => onComplete()
    case OnError(err) => onError(err)
    case _ => 
  }

  def sendDataChunks() {
    while(requested > 0 && queue.nonEmpty && isActive && totalDemand > 0) {
      println("Sending Data Chunk -- DataChunkProcessor")
      onNext(ChunkStreamPart(queue.dequeue()))
      requested -= 1
    }
  }

  val requestStrategy = new MaxInFlightRequestStrategy(50) {
    def inFlightInternally(): Int = { println("In flight internally: " + queue.size); queue.size }
  }
}

