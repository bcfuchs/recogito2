package models.document

import java.net.URI
import java.sql.Timestamp
import models.generated.tables.records.{ DocumentRecord, DocumentFilepartRecord, UserRecord }

case class DocumentInfo(document: DocumentRecord, fileparts: Seq[DocumentFilepartRecord], owner: UserRecord) {
  
  /** Document property shorthands for convenience & readability **/
  
  lazy val id: String = document.getId 
  
  lazy val ownerName: String = owner.getUsername
  
  lazy val uploadedAt: Timestamp = document.getUploadedAt
  
  lazy val title: String = document.getTitle  
  
  lazy val author: Option[String] = Option(document.getAuthor)
  
  lazy val dateNumeric: Option[Timestamp] = Option(document.getDateNumeric)
  
  lazy val dateFreeform: Option[String] = Option(document.getDateFreeform)
  
  lazy val description: Option[String] = Option(document.getDescription)
  
  lazy val language: Option[String] = Option(document.getLanguage)
  
  lazy val source: Option[String] = Option(document.getSource)
  
  lazy val edition: Option[String] = Option(document.getEdition)
  
  lazy val license: Option[License] = Option(document.getLicense).flatMap(License.fromAcronym(_))
  
  lazy val isPublic: Boolean = document.getIsPublic
  
  /** Convenience methods for filepart access **/
  
  lazy val textParts = fileparts.filter(_.getContentType.startsWith("TEXT"))
  
  lazy val hasTextParts = textParts.size > 0
  
  lazy val imageParts = fileparts.filter(_.getContentType.startsWith("TEXT"))
  
  lazy val hasImageParts = imageParts.size > 0
  
  lazy val dataParts = fileparts.filter(_.getContentType.startsWith("DATA"))
  
  lazy val hasDataParts = dataParts.size > 0
  
}