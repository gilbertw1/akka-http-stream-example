package example

import java.math.BigInteger
import scala.collection.mutable.{Queue => MQueue}
import akka.actor._
import akka.stream.actor._

import ActorPublisherMessage._
import ActorSubscriberMessage._

class DoublingProcessor extends ActorSubscriber with ActorPublisher[BigInteger] {  
  val dos = BigInteger.valueOf(2L)
  val doubledQueue = MQueue[BigInteger]()

  def receive = {
    case OnNext(biggie: BigInteger) =>
      doubledQueue.enqueue(biggie.multiply(dos))
      sendDoubled()
    case OnError(err: Exception) => 
      onError(err)
      context.stop(self)
    case OnComplete => 
      onComplete()
      context.stop(self)
    case Request(cnt) =>
      sendDoubled()
    case Cancel =>
      cancel()
      context.stop(self)
    case _ =>
  }

  def sendDoubled() {
    while(isActive && totalDemand > 0 && !doubledQueue.isEmpty) {
      onNext(doubledQueue.dequeue())
    }
  }

  val requestStrategy = new MaxInFlightRequestStrategy(50) {
    def inFlightInternally(): Int = { doubledQueue.size }
  }
}