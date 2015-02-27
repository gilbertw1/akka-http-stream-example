package example

import java.math.BigInteger
import akka.actor._
import akka.stream.actor._

import ActorSubscriberMessage._

class FibonacciSubscriber(delay: Long) extends ActorSubscriber with ActorLogging {
  val requestStrategy = WatermarkRequestStrategy(50)

  def receive = {
    case OnNext(fib: BigInteger) =>
      log.debug("[FibonacciSubscriber] Received Fibonacci Number: {}", fib)
      Thread.sleep(delay)
    case OnError(err: Exception) => 
      log.error(err, "[FibonacciSubscriber] Receieved Exception in Fibonacci Stream")
      context.stop(self)
    case OnComplete => 
      log.info("[FibonacciSubscriber] Fibonacci Stream Completed!")
      context.stop(self)
    case _ =>
  }
}