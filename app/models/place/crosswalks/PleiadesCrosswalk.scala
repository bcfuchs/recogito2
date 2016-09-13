package models.place.crosswalks

import com.vividsolutions.jts.geom.{ Coordinate, Geometry }
import models.{ HasGeometry, HasNullableSeq }
import models.place.{ Description, Gazetteer, GazetteerRecord, Name }
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object PleiadesCrosswalk {
  
  def fromJson(record: String)(implicit sourceGazetteer: String): Option[GazetteerRecord] =
    Json.fromJson[PleiadesRecord](Json.parse(record)) match {
    
      case s: JsSuccess[PleiadesRecord] =>
        Some(GazetteerRecord(
          s.get.uri,
          Gazetteer(sourceGazetteer),
          DateTime.now(),
          None, // TODO lastChangedAt
          s.get.title,
          s.get.description.map(d => Seq(new Description(d))).getOrElse(Seq.empty[Description]),
          s.get.names,
          s.get.features.headOption.map(_.geometry), // TODO compute union?
          s.get.representativePoint,
          None, // TODO temporalBounds
          s.get.placeTypes,
          Seq.empty[String], // TODO closeMatches
          Seq.empty[String]  // TODO exactMatches
        ))
        
      case e: JsError =>
        Logger.warn("Error parsing place")
        Logger.warn(record)
        Logger.warn(e.toString)      
        None
        
    }
  
}

case class Feature(geometry: Geometry)

object Feature extends HasGeometry {
  
  implicit val featureReads: Reads[Feature] =
    (JsPath \ "geometry").read[Geometry].map(Feature(_))
  
}

case class PleiadesRecord(

  uri: String,

  // TODO lastChange
  
  title: String,
  
  description: Option[String],
  
  names: Seq[Name],
  
  features: Seq[Feature],
  
  representativePoint: Option[Coordinate],
  
  // TODO temporal bounds
  
  placeTypes: Seq[String]
  
  // TODO close matches
  
  // TODO exact matches
  
  // TODO include modernCountry for "Pleiades-like" gazetteers?
    
)

object PleiadesRecord extends HasGeometry with HasNullableSeq {
  
  implicit val pleiadesRecordReads: Reads[PleiadesRecord] = (
    (JsPath \ "uri").read[String] and
    (JsPath \ "title").read[String] and
    (JsPath \ "description").readNullable[String] and
    (JsPath \ "names").read[Seq[Name]] and
    (JsPath \ "features").read[Seq[Feature]] and
    (JsPath \ "reprPoint").readNullable[Coordinate] and
    (JsPath \ "place_types").readNullable[Seq[String]].map(_.getOrElse(Seq.empty[String]))
  )(PleiadesRecord.apply _)
  
}
