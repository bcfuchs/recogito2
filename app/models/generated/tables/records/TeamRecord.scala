/**
 * This class is generated by jOOQ
 */
package models.generated.tables.records


import java.lang.String
import java.time.OffsetDateTime

import javax.annotation.Generated

import models.generated.tables.Team

import org.jooq.Field
import org.jooq.Record1
import org.jooq.Record3
import org.jooq.Row3
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = Array(
		"http://www.jooq.org",
		"jOOQ version:3.7.2"
	),
	comments = "This class is generated by jOOQ"
)
class TeamRecord extends UpdatableRecordImpl[TeamRecord](Team.TEAM) with Record3[String, String, OffsetDateTime] {

	/**
	 * Setter for <code>team.title</code>.
	 */
	def setTitle(value : String) : Unit = {
		setValue(0, value)
	}

	/**
	 * Getter for <code>team.title</code>.
	 */
	def getTitle : String = {
		val r = getValue(0)
		if (r == null) null else r.asInstanceOf[String]
	}

	/**
	 * Setter for <code>team.created_by</code>.
	 */
	def setCreatedBy(value : String) : Unit = {
		setValue(1, value)
	}

	/**
	 * Getter for <code>team.created_by</code>.
	 */
	def getCreatedBy : String = {
		val r = getValue(1)
		if (r == null) null else r.asInstanceOf[String]
	}

	/**
	 * Setter for <code>team.created_at</code>.
	 */
	def setCreatedAt(value : OffsetDateTime) : Unit = {
		setValue(2, value)
	}

	/**
	 * Getter for <code>team.created_at</code>.
	 */
	def getCreatedAt : OffsetDateTime = {
		val r = getValue(2)
		if (r == null) null else r.asInstanceOf[OffsetDateTime]
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------
	override def key() : Record1[String] = {
		return super.key.asInstanceOf[ Record1[String] ]
	}

	// -------------------------------------------------------------------------
	// Record3 type implementation
	// -------------------------------------------------------------------------

	override def fieldsRow : Row3[String, String, OffsetDateTime] = {
		super.fieldsRow.asInstanceOf[ Row3[String, String, OffsetDateTime] ]
	}

	override def valuesRow : Row3[String, String, OffsetDateTime] = {
		super.valuesRow.asInstanceOf[ Row3[String, String, OffsetDateTime] ]
	}
	override def field1 : Field[String] = Team.TEAM.TITLE
	override def field2 : Field[String] = Team.TEAM.CREATED_BY
	override def field3 : Field[OffsetDateTime] = Team.TEAM.CREATED_AT
	override def value1 : String = getTitle
	override def value2 : String = getCreatedBy
	override def value3 : OffsetDateTime = getCreatedAt

	override def value1(value : String) : TeamRecord = {
		setTitle(value)
		this
	}

	override def value2(value : String) : TeamRecord = {
		setCreatedBy(value)
		this
	}

	override def value3(value : OffsetDateTime) : TeamRecord = {
		setCreatedAt(value)
		this
	}

	override def values(value1 : String, value2 : String, value3 : OffsetDateTime) : TeamRecord = {
		this.value1(value1)
		this.value2(value2)
		this.value3(value3)
		this
	}

	/**
	 * Create a detached, initialised TeamRecord
	 */
	def this(title : String, createdBy : String, createdAt : OffsetDateTime) = {
		this()

		setValue(0, title)
		setValue(1, createdBy)
		setValue(2, createdAt)
	}
}