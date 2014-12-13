package mongodb

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.FakeApplication
import com.mongodb.casbah.MongoClient
import dao.ActorDao
import model.Actor

class ActorDaoSpec extends PlaySpec with OneAppPerSuite {

  val testDbName = "mediaTest"

  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val actorColl = db("actor")

  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> testDbName))

  "ActorDao" should {

    "return no actor if collection is empty" in {
      actorColl.drop()
      val actorOption = ActorDao.getByFullName("Brad Pitt")

      actorOption mustBe None
    }

    "add a new actor" in {
      actorColl.drop()
      ActorDao.add(getActor())
      val actor = ActorDao.getByFullName("Brad Pitt").get

      actor.id.get.length() must be > 6
      actor.movieDbId must be (None)
      actor.name must be("Brad Pitt")
      actor.birthDay.get must be ("1999-99-99")
      actor.birthPlace.get must be ("Christchurch")
      actor.deathDay must be (None)
      actor.biography.get must be ("biography")
      actor.imdbUrl.get must be ("imdbUrl")
      actor.posterUrl.get must be ("posterUrl")
    }

  }

  private def getActor(): Actor = {
    Actor(Option(""), None, "Brad Pitt", Option("1999-99-99"), Option("Christchurch"), None, Option("biography"), Option("imdbUrl"), Option("posterUrl"))
  }
}