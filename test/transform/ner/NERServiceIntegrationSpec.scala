package transform.ner

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, ImplicitSender }
import java.io.File
import java.util.UUID
import java.sql.Timestamp
import models.ContentType
import models.task.TaskService
import models.generated.tables.records.{ DocumentRecord, DocumentFilepartRecord }
import org.apache.commons.io.FileUtils
import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.specification.AfterAll
import org.junit.runner._
import play.api.Logger
import play.api.test._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.test.Helpers._
import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class NERServiceIntegrationSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with AfterAll {

  // Force Specs2 to execute tests in sequential order
  sequential

  private val TMP_IDX_DIR = "test/resources/tmp-idx"

  override def afterAll = FileUtils.deleteDirectory(new File(TMP_IDX_DIR))

  "The NER service" should {

    val KEEPALIVE = 10.seconds

    val now = new Timestamp(System.currentTimeMillis)

    // Two test documents
    val document1 =
      new DocumentRecord("hcylkmacy4xgkb", "rainer", now, "A short test doc", null, null, null, null, null, null, null, null, false)

    val document2 =
      new DocumentRecord("98muze1cl3saib", "rainer", now, "A long test doc", null, null, null, null, null, null, null, null, false)

    val parts1 = (1 to 5).map(n =>
      new DocumentFilepartRecord(UUID.randomUUID, "hcylkmacy4xgkb", "text-for-ner-0" + n + ".txt", ContentType.TEXT_PLAIN.toString, "text-for-ner-0" + n + ".txt", 0, null))

    val parts2 =
      Seq(new DocumentFilepartRecord(UUID.randomUUID, "98muze1cl3saib", "text-for-ner-06.txt", ContentType.TEXT_PLAIN.toString, "text-for-ner-06.txt", 0, null))

    val dir = new File("test/resources/transform/ner")

    val app = new GuiceApplicationBuilder()
      .configure(Map(
        "recogito.index.dir" -> TMP_IDX_DIR,
        "db.default.driver" -> "org.postgresql.Driver",
        "db.default.url" -> "jdbc:postgresql://localhost/recogito2-test",
        "db.default.username" -> "postgres",
        "db.default.password" -> "postgres"
      ))
      .build();

    val nerService = app.injector.instanceOf(classOf[NERService])
    val taskService = app.injector.instanceOf(classOf[TaskService])

    Logger.info("[NERServiceIntegrationSpec] Submitting 2 documents to NER service")

    val processStartTime = System.currentTimeMillis
    nerService.spawnTask(document1, parts1, dir, Map.empty[String, String], KEEPALIVE)
    nerService.spawnTask(document2, parts2, dir, Map.empty[String, String], KEEPALIVE)

    "start NER on the 2 test documents without blocking" in {
      (System.currentTimeMillis - processStartTime).toInt must be <(1000)
    }

    "report progress until complete" in {

      var ctr = 50
      var isComplete = false

      while (ctr > 0 && !isComplete) {
        ctr -= 1

        val queryStartTime = System.currentTimeMillis

        val result1 = Await.result(taskService.findByDocument(document1.getId), 10 seconds)
        val result2 = Await.result(taskService.findByDocument(document2.getId), 10 seconds)

        result1.isDefined must equalTo(true)
        result2.isDefined must equalTo(true)

        val totalProgress1 = result1.get.progress
        val totalProgress2 = result2.get.progress

        Logger.info("[NERServiceIntegrationSpec] Progress for doc 1 is " + totalProgress1)
        Logger.info("[NERServiceIntegrationSpec] Progress for doc 2 is " + totalProgress2)

        if (totalProgress1 + totalProgress2 == 200)
          isComplete = true

        Thread.sleep(2000)
      }

      success
    }

    "accept progress queries after completion" in {
      val queryStartTime = System.currentTimeMillis

      val result1 = Await.result(taskService.findByDocument(document1.getId), 10.seconds)
      val result2 = Await.result(taskService.findByDocument(document2.getId), 10.seconds)

      (System.currentTimeMillis - queryStartTime).toInt must be <(500)

      result1.isDefined must equalTo(true)
      result2.isDefined must equalTo(true)

      val totalProgress1 = result1.get.progress
      val totalProgress2 = result2.get.progress

      Logger.info("[NERServiceIntegrationSpec] Progress for doc 1 after completion is " + totalProgress1)
      Logger.info("[NERServiceIntegrationSpec] Progress for doc 2 after completion is " + totalProgress2)

      totalProgress1 must equalTo(100)
      totalProgress2 must equalTo(100)
    }

    "reject progress queries after the KEEPALIVE time has expired" in {
      Thread.sleep(KEEPALIVE.toMillis)
      Logger.info("[NERServiceIntegrationSpec] KEEPALIVE expired")

      val result1 = Await.result(taskService.findByDocument(document1.getId), 10 seconds)
      val result2 = Await.result(taskService.findByDocument(document2.getId), 10 seconds)

      result1.isDefined must equalTo(false)
      result2.isDefined must equalTo(false)
    }

  }

}
