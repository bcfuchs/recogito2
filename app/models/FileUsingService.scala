package models

import java.io.File
import play.api.Play

/** Base functionality for services using files uploaded by users **/
private[models] trait FileUsingService {
  
  private val A_TO_Z = 
    (('a' to 'z') ++ ('A' to 'Z')).toSet
  
  private lazy val UPLOAD_BASE = {
    val dir = Play.current.configuration.getString("recogito.upload.dir") match {
      case Some(filename) => new File(filename)   
      case None => new File("uploads") // Default
    }
    
    if (!dir.exists)
      dir.mkdir()
    dir
  }
  
  protected lazy val USER_DATA_DIR = {
    val dir = new File(UPLOAD_BASE, "user_data")
    if (!dir.exists)
      dir.mkdir()
    dir
  }
    
  protected lazy val PENDING_UPLOADS_DIR = {
    val dir = new File(UPLOAD_BASE, "pending")
    if (!dir.exists)
      dir.mkdir()
    dir
  }
  
  def getUserDir(username: String, createIfNotExists: Boolean = false): Option[File] = {
    val alphabeticCharsOnly = username.filter(A_TO_Z.contains(_))
    
    // User folders are contained in common parent folder. The name of that folder is
    // the first two alphabetic characters of the user name. Example:
    // /user-data/ra/rainer/...
    val parentFolder = 
      if (alphabeticCharsOnly.size < 2)
        // Anyone funny enough to insist on a username with fewer than two alphabetic 
        // characters is kept in an extra parent folder
        new File(USER_DATA_DIR, "_anomalies")
      else
        new File(USER_DATA_DIR, alphabeticCharsOnly.substring(0, 2))

    val userFolder = new File(parentFolder, username)
    
    if (createIfNotExists) {
      if (!parentFolder.exists)
        parentFolder.mkdir()
        
      if (!userFolder.exists)
        userFolder.mkdir()
        
      Some(userFolder)
    } else if (userFolder.exists) {
      Some(userFolder)
    } else {
      None
    }
  }
  
  
}