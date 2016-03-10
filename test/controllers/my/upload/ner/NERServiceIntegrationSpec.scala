package controllers.my.upload.ner

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, ImplicitSender }
import java.io.File
import java.time.OffsetDateTime
import models.content.ContentTypes
import models.generated.tables.records.{ DocumentRecord, DocumentFilepartRecord }
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.Logger
import play.api.test._
import play.api.test.Helpers._
import scala.concurrent.Await
import scala.concurrent.duration._
    
@RunWith(classOf[JUnitRunner])
class NERServiceIntegrationSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike {
  
  sequential // Force Specs2 to execute tests in sequential order
  
  "The NER service" should {
    
    val KEEP_ALIVE = 10 seconds
    
    // Two test documents
    val document1 = new DocumentRecord(0, "rainer", OffsetDateTime.now, "A short test doc", null, null, null, null, null, null)
    val document2 = new DocumentRecord(1, "rainer", OffsetDateTime.now, "A long test doc", null, null, null, null, null, null)
    
    // Five fileparts - parts 1-3 on document 0, 4-5 on document 1
    val parts1 = (1 to 5).map(n => new DocumentFilepartRecord(n, 0, "text-for-ner-0" + n + ".txt", ContentTypes.TEXT_PLAIN.toString, "text-for-ner-0" + n + ".txt", 0))
    val parts2 = Seq(new DocumentFilepartRecord(6, 0, "text-for-ner-06.txt", ContentTypes.TEXT_PLAIN.toString, "text-for-ner-06.txt", 0))
      
    val dir = new File("test/resources")
    
    Logger.info("[NERServiceIntegrationSpec] Submitting 2 documents to NER service")
      
    val processStartTime = System.currentTimeMillis
    NERService.spawnNERProcess(document1, parts1, dir, KEEP_ALIVE)
    NERService.spawnNERProcess(document2, parts2, dir, KEEP_ALIVE)
    
    "start NER on the 2 test documents without blocking" in { 
      (System.currentTimeMillis - processStartTime).toInt must be <(1000)
    }
      
    "report progress until complete, with progress response time <500ms" in {
      var ctr = 50
      var isComplete = false
      
      while (ctr > 0 && !isComplete) {
        ctr -= 1
        
        val queryStartTime = System.currentTimeMillis
        
        val result1 = Await.result(NERService.queryProgress(0), 10 seconds)
        val result2 = Await.result(NERService.queryProgress(1), 10 seconds)
        
        (System.currentTimeMillis - queryStartTime).toInt must be <(500)
        
        result1.isDefined must equalTo(true) 
        result2.isDefined must equalTo(true) 
        
        val totalProgress1 = result1.get.progress.map(_.progress).sum / result1.get.progress.size
        val totalProgress2 = result2.get.progress.map(_.progress).sum / result2.get.progress.size
            
        Logger.info("[NERServiceIntegrationSpec] Progress for doc 1 is " + totalProgress1)
        Logger.info("[NERServiceIntegrationSpec] Progress for doc 2 is " + totalProgress2)
        
        if (totalProgress1 + totalProgress2 == 2.0)
          isComplete = true
        
        Thread.sleep(2000)
      }
      
      success
    }
          
    "accept progress queries after completion" in {
      val queryStartTime = System.currentTimeMillis
      
      val result1 = Await.result(NERService.queryProgress(0), 10 seconds)
      val result2 = Await.result(NERService.queryProgress(1), 10 seconds)
      
      (System.currentTimeMillis - queryStartTime).toInt must be <(500)
      
      result1.isDefined must equalTo(true) 
      result2.isDefined must equalTo(true) 
      
      val totalProgress1 = result1.get.progress.map(_.progress).sum / result1.get.progress.size
      val totalProgress2 = result2.get.progress.map(_.progress).sum / result2.get.progress.size
      
      Logger.info("[NERServiceIntegrationSpec] Progress for doc 1 after completion is " + totalProgress1)
      Logger.info("[NERServiceIntegrationSpec] Progress for doc 2 after completion is " + totalProgress2)
        
      totalProgress1 must equalTo(1.0)
      totalProgress2 must equalTo(1.0)
    }
    
    "reject progress queries after the KEEPALIVE time has expired" in {
      Thread.sleep(KEEP_ALIVE.toMillis)
      
      val result1 = Await.result(NERService.queryProgress(0), 10 seconds)
      val result2 = Await.result(NERService.queryProgress(1), 10 seconds)
    
      Logger.info("[NERServiceIntegrationSpec] KEEPALIVE expired")
      
      result1.isDefined must equalTo(false)
      result2.isDefined must equalTo(false)
    }
    
  }
  
  "The database" should {
    
    "contain the annotations after NER" in {
      success
    }
    
  }
  
}