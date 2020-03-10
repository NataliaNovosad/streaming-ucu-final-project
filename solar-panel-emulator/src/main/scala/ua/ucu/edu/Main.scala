package ua.ucu.edu

import ua.ucu.edu.kafka.DummyDataProducer

object Main extends App {

  DummyDataProducer.pushTestData("/reddit_comments_data0.csv")
}
