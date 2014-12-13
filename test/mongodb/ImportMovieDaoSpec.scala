package mongodb

import java.io.{File => JavaFile}

import scala.io.Source

import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec

import com.mongodb.casbah.MongoClient

import dao.Movie2Dao
import model.Movie2
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json
import play.api.test.FakeApplication


/**
 * TODO Finish testing the import of backup files.
 */
class ImportMovieDaoSpec extends PlaySpec with OneAppPerSuite {

  val testDbName = "mediaTestNew"

  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val movieColl = db("movie")

  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> testDbName))

  "MovieDao" should {

    "should import all movies" in {
      movieColl.drop()
      Movie2Dao.findAll.size === 0

      val json = Json.parse(Source.fromFile(new JavaFile("/tmp/movie_2014-08-03_08-46.json")).getLines.mkString(""))
      val moviesJsonResult = json.validate[Seq[Movie2]]
      val movieList: Option[Seq[Movie2]] = moviesJsonResult match {
        case s: JsSuccess[Seq[Movie2]] => Some(s.get)
        case e: JsError => None
      }

      if (!movieList.isEmpty) {
        movieList.get.foreach(Movie2Dao.add(_))
      }
    }
  }
}