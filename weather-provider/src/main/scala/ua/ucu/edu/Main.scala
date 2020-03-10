package ua.ucu.edu

//import ua.ucu.edu.kafka.DummyDataProducer
import ua.ucu.edu.provider._
import org.slf4j.LoggerFactory

import scala.concurrent.duration
import scala.language.postfixOps

object Main extends App {

  val logger = LoggerFactory.getLogger(getClass)

  logger.info("======== Weather Provider App Init ========")


  TwitterStream.run()
  import duration._

  //scheduler.schedule(5 seconds, 10 seconds,
/*  new Runnable {
    override def run(): Unit = {
      logger.debug("weather request")
      // ???
      // todo - ask weather api and send data to kafka topic - recommended format is json - or you can come up with simpler string-based protocol
    }
  }
  //)

  // for testing purposes only
  DummyDataProducer.pushTestData()*/
}
