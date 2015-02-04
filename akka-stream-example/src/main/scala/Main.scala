package example

import java.math.BigInteger
import akka.actor._
import akka.stream.actor._

object Main extends App {
  val system = ActorSystem("example-stream-system")

  Examples.startSimplePubSubExample(system)
  Examples.startPubSubTransformerExample(system)
}

object Examples {

  def startSimplePubSubExample(system: ActorSystem) {
    system.log.info("Starting Publisher")
    val publisherActor = system.actorOf(Props[FibonacciPublisher])
    val publisher = ActorPublisher[BigInteger](publisherActor)

    system.log.info("Starting Subscriber")
    val subscriberActor = system.actorOf(Props(new FibonacciSubscriber(500)))
    val subscriber = ActorSubscriber[BigInteger](subscriberActor)

    system.log.info("Subscribing to Publisher")
    publisher.subscribe(subscriber)
  }

  def startPubSubTransformerExample(system: ActorSystem) {
    system.log.info("Starting Publisher")
    val publisherActor = system.actorOf(Props[FibonacciPublisher])
    val publisher = ActorPublisher[BigInteger](publisherActor)

    system.log.info("Starting Doubling Processor")
    val doubleProcessorActor = system.actorOf(Props[DoublingProcessor])
    val doublePublisher = ActorPublisher[BigInteger](doubleProcessorActor)
    val doubleSubscriber = ActorSubscriber[BigInteger](doubleProcessorActor)

    system.log.info("Starting Subscriber")
    val subscriberActor = system.actorOf(Props(new FibonacciSubscriber(500)))
    val subscriber = ActorSubscriber[BigInteger](subscriberActor)

    system.log.info("Subscribing to Processor to Publisher")
    publisher.subscribe(doubleSubscriber)
    system.log.info("Subscribing Subscriber to Processor")
    doublePublisher.subscribe(subscriber)
  }
}