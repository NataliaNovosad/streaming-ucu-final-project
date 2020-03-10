package ua.ucu.edu.model

import java.util.Date

case class TweetUser(name: String, screenName: String, location: String, followersCount: Int) {}

case class Place(country: String, streetAddress: String, placeType: String) {}

case class General(createdAt: Date, tweetText: String, tweetTextLength: Int, source: String) {}

case class JsonObjectTwitter(user: TweetUser, places: Place, general: General) {}