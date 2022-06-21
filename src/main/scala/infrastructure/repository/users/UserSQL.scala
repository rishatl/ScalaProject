package infrastructure.repository.users

import cats.syntax.all._
import domain.users._
import doobie._
import doobie.implicits._
import io.circe.parser.decode
import io.circe.syntax._

private object UserSQL {
  // H2 does not support JSON data type.
  implicit val roleMeta: Meta[Role] =
    Meta[String].imap(decode[Role](_).leftMap(throw _).merge)(_.asJson.toString)

  def insert(user: User): Update0 = sql"""
    INSERT INTO USERS (USER_NAME, FIRST_NAME, LAST_NAME, PHONE, HASH, ROLE)
    VALUES (${user.userName}, ${user.firstName}, ${user.lastName}, ${user.phone}, ${user.hash}, ${user.role})
  """.update

  def update(user: User, id: Long): Update0 = sql"""
    UPDATE USERS
    SET FIRST_NAME = ${user.firstName}, LAST_NAME = ${user.lastName},
        PHONE = ${user.phone}, HASH = ${user.hash}, ROLE = ${user.role}
    WHERE ID = $id
  """.update

  def select(userId: Long): Query0[User] = sql"""
    SELECT USER_NAME, FIRST_NAME, LAST_NAME, PHONE, HASH, ID, ROLE
    FROM USERS
    WHERE ID = $userId
  """.query

  def byUserName(userName: String): Query0[User] = sql"""
    SELECT USER_NAME, FIRST_NAME, LAST_NAME, PHONE, HASH, ID, ROLE
    FROM USERS
    WHERE USER_NAME = $userName
  """.query[User]

  def delete(userId: Long): Update0 = sql"""
    DELETE FROM USERS WHERE ID = $userId
  """.update

  val selectAll: Query0[User] = sql"""
    SELECT USER_NAME, FIRST_NAME, LAST_NAME, PHONE, HASH, ID, ROLE
    FROM USERS
  """.query
}
