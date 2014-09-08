package example

import java.math.BigInteger
import akka.actor._
import akka.stream.actor._

import ActorPublisherMessage._

class FibonacciPublisher extends ActorPublisher[BigInteger] with ActorLogging {
  var prev = BigInteger.ZERO
  var curr = BigInteger.ZERO

  def receive = {
    case Request(cnt) => 
      log.debug("[FibonacciPublisher] Received Request ({}) from Subscriber", cnt)
      sendFibs()
    case Cancel => 
      log.info("[FibonacciPublisher] Cancel Message Received -- Stopping")
      context.stop(self)
    case _ =>
  }

  def sendFibs() {
    while(isActive && totalDemand > 0) {
      onNext(nextFib())
    }
  }

  def nextFib(): BigInteger = {    
    if(curr == BigInteger.ZERO) {
      curr = BigInteger.ONE
    } else {
      val tmp = prev.add(curr)
      prev = curr
      curr = tmp
    }
    curr
  }
}