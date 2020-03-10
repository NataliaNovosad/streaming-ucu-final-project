package ua.ucu.edu

import kafka.{DummyDataProducer}

class User(num: Int) extends Thread {
  override def run() {
    val producer = new DummyDataProducer()
    producer.pushTestData("/reddit_comments_data" + num + ".csv")
  }
}

object Main extends App {
  for (i <- 0 to 4) {
    val thread = new User(i)
    thread.start()
  }
}
