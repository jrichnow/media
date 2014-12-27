package mongodb

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import com.mongodb.casbah.MongoClient
import dao.ActorDao
import play.api.test.FakeApplication
import dao.RequestsDao
import model.Request

class RequestDaoSpec extends PlaySpec with OneAppPerSuite {

  val testDbName = "mediaTest"

  val client = MongoClient("localhost", 27017)
  val db = client("mediaTest")
  val requestColl = db("requests")

  implicit override lazy val app: FakeApplication =
    FakeApplication(additionalConfiguration = Map("mongodb.media.db" -> testDbName))

  "RequestsDao" should {

    "return no request if collection is empty" in {
      requestColl.drop()
      val requestsOption = RequestsDao.findAll()

      requestsOption.size mustBe (0)
    }

    "add a new request" in {
      requestColl.drop()
      val dbRequest = RequestsDao.add(Request(None, "Feature", "Add something", None, None))

      dbRequest.id.get.length() must be > 6
      dbRequest.subject must be("Feature")
      dbRequest.topic must be("Add something")
      dbRequest.comment must be(None)
      dbRequest.imdbUrl must be(None)

      val request = RequestsDao.findById(dbRequest.id.get)
      dbRequest === request
      
      val requests = RequestsDao.findAll()
      requests.size must be (1)
      requests(0) === request
    }
  }
}