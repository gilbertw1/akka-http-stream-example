package example

import java.io.File
import akka.util.ByteString
import akka.actor.{ActorSystem,ActorRefFactory}
import akka.stream.actor.{ActorPublisher}
import akka.stream.{MaterializerSettings,FlowMaterializer}
import akka.http.model._
import com.typesafe.config._
import org.reactivestreams.{Publisher,Subscriber}

import HttpMethods._
import HttpEntity._
import MainFunctions._

object Main extends App {
  
  implicit val system = ActorSystem()
  implicit val materializer = FlowMaterializer(MaterializerSettings())
  val config = ConfigFactory.parseFile(new File(args(0)))

  val publisher: Publisher[ChunkStreamPart] = config.getString("example.publisher") match {
    case "random" => RandomDataPublisher()
    case "client" => StreamClientPublisher(config.getInt("example.client.port"))
  }

  config.getString("example.subscriber") match {
    case "server" => startStreamServerWithPublisher(publisher, config.getInt("example.server.port"))
    case "print" => startPrintSubscriber(publisher, config.getLong("example.delay"))
  }  
}

object MainFunctions {

  def startStreamServerWithPublisher(publisher: Publisher[ChunkStreamPart], port: Int)(implicit system: ActorSystem, materializer: FlowMaterializer) = {
    HttpServer.bindServer(port) {
      case HttpRequest(GET, Uri.Path("/"), _, _, _) => 
        HttpResponse (
          entity = new Chunked(MediaTypes.`text/plain`, publisher)
        )
      case _: HttpRequest => HttpResponse(404, entity = "Unknown resource!")
    }
  }

  def startPrintSubscriber(publisher: Publisher[ChunkStreamPart], delay: Long)(implicit arf: ActorRefFactory) = {
    val subscriber = PrintDataSubscriber(delay)
    publisher.subscribe(subscriber)
  }
}