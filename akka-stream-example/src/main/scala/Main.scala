package example

import java.math.BigInteger
import akka.actor._
import akka.stream.actor._
import akka.stream.scaladsl.Flow
import akka.stream.{ MaterializerSettings, FlowMaterializer }

object Main extends App {
  val system = ActorSystem("example-stream-system")

  Examples.startSimplePubSubExample(system)
  Examples.startPubSubTransformerExample(system)
  Examples.startFlowExample(system)
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

  def startFlowExample(implicit system: ActorSystem) {
    import system.dispatcher
    implicit val materializer = FlowMaterializer(MaterializerSettings())

    val publisherActor = system.actorOf(Props[FibonacciPublisher])
    val publisher = ActorPublisher[BigInteger](publisherActor)

    Flow(publisher).map(_.multiply(BigInteger.valueOf(2L))).foreach { fib =>
      system.log.debug("Received Fibonacci Number: {}", fib)
      Thread.sleep(500)
    }
  }
}