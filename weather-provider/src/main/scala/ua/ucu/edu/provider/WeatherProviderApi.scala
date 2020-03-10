package ua.ucu.edu.provider

//import ua.ucu.edu.model._
import ua.ucu.edu.kafka._

/*
trait WeatherProviderApi {

  def weatherAtLocation(location: Location): WeatherData
}
*/


import java.util.Date

import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write
import twitter4j._
import twitter4j.conf.Configuration

case class TweetUser(name: String, screenName: String, location: String, followersCount: Int) {}

case class Place(country: String, streetAddress: String, placeType: String) {}

case class General(createdAt: Date, tweetText: String, tweetTextLength: Int, source: String) {}

case class JsonObjectTwitter(user: TweetUser, places: Place, general: General) {}


object TwitterStream {
  def run() {
    val twitterStream = new TwitterStreamFactory(Util.config).getInstance
    twitterStream.addListener(Util.simpleStatusListener)
    twitterStream.sample()
    Thread.sleep(500)
    twitterStream.cleanUp()
    twitterStream.shutdown()
  }

  object Util {
    val producer = new Producer()
    var counter: Int = 0

    val config: Configuration = new twitter4j.conf.ConfigurationBuilder()
      .setOAuthConsumerKey(AccessTokens.APIkey)
      .setOAuthConsumerSecret(AccessTokens.APIkeysecret)
      .setOAuthAccessToken(AccessTokens.accesstoken)
      .setOAuthAccessTokenSecret(AccessTokens.accesstokensecret)
      .setDebugEnabled(false)
      .build

    def simpleStatusListener: StatusListener = new StatusListener() {
      def onStatus(status: Status) {
        val usr = new TweetUser(status.getUser.getName, status.getUser.getScreenName, status.getUser.getLocation, status.getUser.getFollowersCount)
        val place = new Place(status.getPlace.getCountry, status.getPlace.getStreetAddress, status.getPlace.getPlaceType)
        val general = new General(status.getCreatedAt, status.getText, status.getText.length, status.getSource)

        val jsonObjectTwitter: JsonObjectTwitter = JsonObjectTwitter(usr, place, general)

        implicit val formats: DefaultFormats.type = DefaultFormats
        val jsonString = write(jsonObjectTwitter)

        producer.writeToKafka("weather_data", jsonString)

        //        println("user:", "name: " + status.getUser.getName, "ScreenName: " + status.getUser.getScreenName, "Location: " + status.getUser.getLocation, "FollowersCount: " + status.getUser.getFollowersCount, "\n")
        //        println("places:", "country: " + status.getPlace.getCountry, "StreetAddress:" + status.getPlace.getStreetAddress, "PlaceType: " + status.getPlace.getPlaceType, "\n")
        //                println("general:", status.getCreatedAt, status.getText, status.getText.length, status.getSource, "\n")
        //        println("hashtags")
        //        status.getHashtagEntities.foreach(f => print(f.getText))
        //        println("===================================================================================================================================")
      }

      def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}

      def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}

      def onException(ex: Exception) {
        //        ex.printStackTrace()
      }

      def onScrubGeo(arg0: Long, arg1: Long) {}

      def onStallWarning(warning: StallWarning) {}
    }
  }


}
/*
object AustinStreamer {
  def main(args: Array[String]) {
    val twitterStream = new TwitterStreamFactory(Util.config).getInstance
    twitterStream.addListener(Util.simpleStatusListener)
    //    val lat: Double = 41.0136
    //    val longitude: Double = 28.9744
    //    val lat1: Double = lat - .50
    //    val longitude1: Double = longitude - .50
    //    val lat2: Double = lat + .50
    //    val longitude2: Double = longitude + .50
    //
    //    val bb: Array[Double] = new Array((lat1, longitude1), (lat2, longitude2))

    val austinBox = Array(Array(-97.8, 30.25), Array(-97.65, 30.35))

    //    val boundingBoxOfUS = Array(Array(-124.848974, 24.396308), Array(-66.885444, 49.384358))
    twitterStream.filter(new FilterQuery().locations(austinBox))
    println("Started " + this.getClass.getName)
    Thread.sleep(500)
    twitterStream.cleanUp()
    twitterStream.shutdown()

    //    val producer = new Producer()
    //    producer.writeToKafka("twitter", "testvalue")

  }


}

object UkraineStreamer {
  def main(args: Array[String]) {
    val twitterStream = new TwitterStreamFactory(Util.config).getInstance
    twitterStream.addListener(Util.simpleStatusListener)
    //    val lat: Double = 41.0136
    //    val longitude: Double = 28.9744
    val lat1: Double = 44.41886
    val longitude1: Double = 22.20555
    val lat2: Double = 52.18903
    val longitude2: Double = 40.13222

    //    val bb: Array[Double] = new Array((lat1, longitude1), (lat2, longitude2))

    val ukraineBox = Array(Array(longitude1, lat1), Array(longitude2, lat2))

    //    val boundingBoxOfUS = Array(Array(-124.848974, 24.396308), Array(-66.885444, 49.384358))
    twitterStream.filter(new FilterQuery().locations(ukraineBox))
    Thread.sleep(20000)
    twitterStream.cleanUp()
    twitterStream.shutdown()
  }
}*/


object AccessTokens {
  val accesstoken = "722330316912881664-1ylMiQbFH7TbqO64iXaAJCnStanWsPZ";
  val accesstokensecret = "7fi3YezupjOH1vyNYHt5stbPYOVcP1ilGR9mMIQYLJh8f"
  val APIkey = "TnmcbgSW5kqjKSGitp4ZnzWHL"
  val APIkeysecret = "qs021crbo0lC6ntxwRYb32MUTPz0ETnzatbfiY4qFS9ko2xVZy"

}