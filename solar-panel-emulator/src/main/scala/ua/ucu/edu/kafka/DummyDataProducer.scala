package ua.ucu.edu.kafka

import java.io.{FileNotFoundException, IOException}
import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source
import ua.ucu.edu.model.RedditComment

import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write

// delete_me - for testing purposes
object DummyDataProducer {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  // This is just for testing purposes
  def pushTestData(file:String): Unit = {
    val BrokerList: String = System.getenv(Config.KafkaBrokers)
    val Topic = "reddit-topic"

    val props = new Properties()
    props.put("bootstrap.servers", BrokerList)
    props.put("client.id", "solar-panel-1")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    logger.info("initializing producer")

    val producer = new KafkaProducer[String, String](props)

    try {
      val filename = getClass.getResourceAsStream(file) //"./src/main/resources/reddit_comments_data0.csv"
      val lines = Source.fromInputStream(filename).getLines()
      for (line <- lines) {
        val splited = line.split("&")
        Thread.sleep(1000)
        splited.length match {
          case 4 => {
            val comment = RedditComment(splited(1),splited(2),splited(3),file.slice(file.length - 9, file.length))
            implicit val formats = DefaultFormats
            val jsonString = write(comment)
            logger.info(s"[$Topic] $jsonString")
            val data = new ProducerRecord[String, String](Topic, splited(0), jsonString)
            producer.send(data)
          }
          case _ => {}
        }
      }
    } catch {
      case e: FileNotFoundException => println("Couldn't find that file.")
      case e: IOException => println("Got an IOException!")
    }
    producer.close()
  }
}

object Config {
  val KafkaBrokers = "KAFKA_BROKERS"
}