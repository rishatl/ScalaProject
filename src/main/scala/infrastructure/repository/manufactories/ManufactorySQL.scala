package infrastructure.repository.manufactories

import domain.manufactories.{Manufactory, ManufactoryStatus}
import doobie._
import doobie.implicits._

private object ManufactorySQL {

  /* We require type StatusMeta to handle our ADT Status */
  implicit val StatusMeta: Meta[ManufactoryStatus] =
    Meta[String].imap(ManufactoryStatus.withName)(_.entryName)

  def insert(manufactory: Manufactory): Update0 = sql"""
    INSERT INTO MANUFACTORY (NAME, STATUS)
    VALUES (${manufactory.name}, ${manufactory.status})
  """.update

  def update(manufactory: Manufactory, id: Long): Update0 = sql"""
    UPDATE MANUFACTORY
    SET NAME = ${manufactory.name}, STATUS = ${manufactory.status}
    WHERE id = $id
  """.update

  def select(id: Long): Query0[Manufactory] = sql"""
    SELECT NAME, STATUS, ID
    FROM MANUFACTORY
    WHERE ID = $id
  """.query

  def delete(id: Long): Update0 = sql"""
    DELETE FROM MANUFACTORY WHERE ID = $id
  """.update

  def selectByStatus(status: ManufactoryStatus): Query0[Manufactory] = sql"""
    SELECT NAME, STATUS, ID
    FROM MANUFACTORY
    WHERE STATUS = $status
  """.query[Manufactory]

  def selectAll: Query0[Manufactory] = sql"""
    SELECT NAME, STATUS, ID
    FROM MANUFACTORY
    ORDER BY STATUS
  """.query
}
