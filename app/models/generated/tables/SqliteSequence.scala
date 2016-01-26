/**
 * This class is generated by jOOQ
 */
package models.generated.tables


import java.lang.Class
import java.lang.Object
import java.lang.String

import javax.annotation.Generated

import models.generated.DefaultSchema
import models.generated.tables.records.SqliteSequenceRecord

import org.jooq.Field
import org.jooq.Table
import org.jooq.TableField
import org.jooq.impl.TableImpl


object SqliteSequence {

	/**
	 * The reference instance of <code>sqlite_sequence</code>
	 */
	val SQLITE_SEQUENCE = new SqliteSequence
}

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
class SqliteSequence(alias : String, aliased : Table[SqliteSequenceRecord], parameters : Array[ Field[_] ]) extends TableImpl[SqliteSequenceRecord](alias, DefaultSchema.DEFAULT_SCHEMA, aliased, parameters, "") {

	/**
	 * The class holding records for this type
	 */
	override def getRecordType : Class[SqliteSequenceRecord] = {
		classOf[SqliteSequenceRecord]
	}

	/**
	 * The column <code>sqlite_sequence.name</code>.
	 */
	val NAME : TableField[SqliteSequenceRecord, Object] = createField("name", org.jooq.impl.DefaultDataType.getDefaultDataType(""), "")

	/**
	 * The column <code>sqlite_sequence.seq</code>.
	 */
	val SEQ : TableField[SqliteSequenceRecord, Object] = createField("seq", org.jooq.impl.DefaultDataType.getDefaultDataType(""), "")

	/**
	 * Create a <code>sqlite_sequence</code> table reference
	 */
	def this() = {
		this("sqlite_sequence", null, null)
	}

	/**
	 * Create an aliased <code>sqlite_sequence</code> table reference
	 */
	def this(alias : String) = {
		this(alias, models.generated.tables.SqliteSequence.SQLITE_SEQUENCE, null)
	}

	private def this(alias : String, aliased : Table[SqliteSequenceRecord]) = {
		this(alias, aliased, null)
	}

	override def as(alias : String) : SqliteSequence = {
		new SqliteSequence(alias, this)
	}

	/**
	 * Rename this table
	 */
	def rename(name : String) : SqliteSequence = {
		new SqliteSequence(name, null)
	}
}