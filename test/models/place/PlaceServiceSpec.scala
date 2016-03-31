package models.place

import com.vividsolutions.jts.geom.{ Coordinate, GeometryFactory }
import java.io.File
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.Logger
import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class PlaceServiceSpec extends Specification {
  
  // Force Specs2 to execute tests in sequential order
  sequential 
  
  private val DARE_RDF = "test/resources/gazetteer_sample_dare.ttl"
  
  private val PLEIADES_RDF = "test/resources/gazetteer_sample_pleiades.ttl"
  
  val mockStore = new MockPlaceStore()
  
  "After importing the DARE sample, the PlaceService" should {
    
     val dareRecords = Gazetteer.loadFromRDF(new File(DARE_RDF), "DARE")
    
    "contain 4 places" in {
      // This mostly tests the mock impl - but probably doesn't hurt & is consistent the integration spec
      PlaceService.importGazetteerRecords(dareRecords, mockStore)
      PlaceService.totalPlaces(mockStore) must equalTo(4)
    }
    
    "return DARE places based on their URI" in {
      // Except for URI normalization this mostly tests the mock impl - but see above
      val barcelona = PlaceService.findByURI("http://dare.ht.lu.se/places/6534", mockStore)
      barcelona.get.title must equalTo("Col. Barcino, Barcelona")
      
      val vindobona = PlaceService.findByURI("http://dare.ht.lu.se/places/10783", mockStore)
      vindobona.get.title must equalTo("Mun. Vindobona, Wien")
      
      val thessaloniki = PlaceService.findByURI("http://dare.ht.lu.se/places/17068", mockStore)
      thessaloniki.get.title must equalTo("Thessalonica, Thessaloniki")
          
      val lancaster = PlaceService.findByURI("http://dare.ht.lu.se/places/23712", mockStore)
      lancaster.get.title must equalTo("Calunium?, Lancaster")      
    }
    
  }
  
  "Based on the fictitious sample record, getAffectedPlaces" should {
    
    "return Vindobona, Thessalonica and Calunium" in {
      val fakeVindobona = GazetteerRecord(
        "http://www.wikidata.org/entity/Q871525", // This will cause DARE's Vindobona to match
        Gazetteer("DummyGazetteer"),
        "A fake place",
        Seq.empty[String],
        Seq.empty[Description],
        Seq.empty[Name],
        None,
        None,
        None,
        Seq("http://dare.ht.lu.se/places/17068"), // This will match DARE's Thessalonica
        Seq("http://www.trismegistos.org/place/15045")) // This is a common match with DARE's Calunium
      
      val expectedPlaceURIs = Seq(
        "http://dare.ht.lu.se/places/10783",
        "http://dare.ht.lu.se/places/17068",
        "http://dare.ht.lu.se/places/23712")
        
      val affectedPlaces = PlaceService.getAffectedPlaces(fakeVindobona, mockStore)
      affectedPlaces.size must equalTo(3)
      affectedPlaces.map(_.id) must containAllOf(expectedPlaceURIs)
    }
    
  }
  
  "After importing the Pleiades sample, the PlaceService" should {
    
    val pleiadesRecords = Gazetteer.loadFromRDF(new File(PLEIADES_RDF), "Pleiades")
      
    "contain 5 places" in {
      PlaceService.importGazetteerRecords(pleiadesRecords, mockStore)
      PlaceService.totalPlaces(mockStore) must equalTo(5)
    }
    
    "return the places by any URI - DARE or Pleiades" in { 
      val barcelonaDARE = PlaceService.findByURI("http://dare.ht.lu.se/places/6534", mockStore)
      val barcelonaPleiades = PlaceService.findByURI("http://pleiades.stoa.org/places/246343", mockStore)
      barcelonaDARE.get must equalTo(barcelonaPleiades.get)
      
      val vindobonaDARE = PlaceService.findByURI("http://dare.ht.lu.se/places/10783", mockStore)
      val vindobonaPleiades = PlaceService.findByURI("http://pleiades.stoa.org/places/128460", mockStore)
      vindobonaDARE.get must equalTo(vindobonaPleiades.get)
      
      val thessalonikiDARE = PlaceService.findByURI("http://dare.ht.lu.se/places/17068", mockStore)
      val thessalonikiPleiades = PlaceService.findByURI("http://pleiades.stoa.org/places/491741", mockStore)
      thessalonikiDARE.get must equalTo(thessalonikiPleiades.get)
          
      val lancasterDARE = PlaceService.findByURI("http://dare.ht.lu.se/places/23712", mockStore)
      val lancasterPleiades = PlaceService.findByURI("http://pleiades.stoa.org/places/89222", mockStore)
      lancasterDARE.get must equalTo(lancasterPleiades.get)
      
      // This record only exists in the Pleiades sample, not the DARE one
      val vindobonaFortPleiades = PlaceService.findByURI("http://pleiades.stoa.org/places/128537", mockStore)
      vindobonaFortPleiades.isDefined must equalTo(true)
    }
    
    "retain the original title from DARE" in { 
      val barcelonaPleiades = PlaceService.findByURI("http://pleiades.stoa.org/places/246343", mockStore)
      barcelonaPleiades.get.title must equalTo("Col. Barcino, Barcelona")
      
      val vindobonaPleiades = PlaceService.findByURI("http://pleiades.stoa.org/places/128460", mockStore)
      vindobonaPleiades.get.title must equalTo("Mun. Vindobona, Wien")
      
      val thessalonikiPleiades = PlaceService.findByURI("http://pleiades.stoa.org/places/491741", mockStore)
      thessalonikiPleiades.get.title must equalTo("Thessalonica, Thessaloniki")
          
      val lancasterPleiades = PlaceService.findByURI("http://pleiades.stoa.org/places/89222", mockStore)
      lancasterPleiades.get.title must equalTo("Calunium?, Lancaster")
    }
    
    "have properly conflated the sample place Vindobona (pleiades:128460)" in {
      val vindobona = PlaceService.findByURI("http://pleiades.stoa.org/places/128460", mockStore).get
     
      val coordDARE = new Coordinate(16.391128, 48.193161)
      val pointDARE = new GeometryFactory().createPoint(coordDARE)
            
      vindobona.geometry must equalTo(Some(pointDARE))
      vindobona.representativePoint must equalTo(Some(coordDARE))
      
      val expectedDescriptions = Seq(
        Description("An ancient place, cited: BAtlas 13 B4 Mun. Vindobona"),
        Description("Ancient city Mun. Vindobona, modern Wien. Gemeinde Wien, Bezirk Wien, AT"))
        
      vindobona.descriptions.size must equalTo(2)
      vindobona.descriptions.keys must containAllOf(expectedDescriptions)
      
      vindobona.placeTypes.keys must equalTo(Seq("SETTLEMENT"))      
      vindobona.temporalBounds must equalTo(Some(TemporalBounds.fromYears(-30, 640)))
      
      vindobona.names.size must equalTo(6)
            
      vindobona.names.get(Name("Mun. Vindobona")).get must equalTo(Seq(Gazetteer("Pleiades")))
      vindobona.names.get(Name("Mun. Vindobona", Some("la"))).get must equalTo(Seq(Gazetteer("DARE")))
      vindobona.names.get(Name("Wien")).get must containAllOf(Seq(Gazetteer("Pleiades"), Gazetteer("DARE")))
      vindobona.names.get(Name("Wien/Vienna AUS")).get must equalTo(Seq(Gazetteer("Pleiades")))
      vindobona.names.get(Name("Vienne", Some("fr"))).get must equalTo(Seq(Gazetteer("DARE")))
      vindobona.names.get(Name("Vienna", Some("en"))).get must equalTo(Seq(Gazetteer("DARE")))

      val expectedCloseMatches = Seq("http://www.wikidata.org/entity/Q871525")
      val expectedExactMatches = Seq(
          "http://pleiades.stoa.org/places/128460",
          "http://www.trismegistos.org/place/28821")

      vindobona.closeMatches must equalTo(expectedCloseMatches)
      vindobona.exactMatches must containAllOf(expectedExactMatches)
      
      vindobona.isConflationOf.size must equalTo(2)
      
      val pleiadesRecord = vindobona.isConflationOf.find(_.uri == "http://pleiades.stoa.org/places/128460").get
      
      val coordPleiades = new Coordinate(16.373064, 48.208982)
      val pointPleiades = new GeometryFactory().createPoint(coordPleiades)
      pleiadesRecord.geometry must equalTo(Some(pointPleiades))
      pleiadesRecord.representativePoint must equalTo(Some(coordPleiades))
    }
    
  }
  
  "After adding a 'bridiging' record that connects two places, the PlaceService" should {
    
    "contain one place less" in {
      // A fictitious record connecting the two Vindobonas
      val fakeMeidling = GazetteerRecord(
        "https://de.wikipedia.org/wiki/Meidling",
        Gazetteer("DummyGazetteer"),
        "A fake briding place",
        Seq.empty[String],
        Seq.empty[Description],
        Seq.empty[Name],
        None,
        None,
        None,
        Seq("http://pleiades.stoa.org/places/128460"), // Mun. Vindobona
        Seq("http://pleiades.stoa.org/places/128537")) // Vindobona
      
      PlaceService.importGazetteerRecords(Seq(fakeMeidling), mockStore)
      PlaceService.totalPlaces(mockStore) must equalTo(4)
    }
    
    "should contain a 'successor place' consisting of the properly conflated records" in {
      // TODO implement
      failure
    }
    
  }
  
  "After removing the bridging record, the PlaceService" should {
    
    "contain one place more" in {
      // TODO implement
      failure
    }
    
    "should contain two 'successor places' consisting of properly conflated records" in {
      // TODO implement
      failure
    }
    
  }
  
}