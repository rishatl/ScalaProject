package infrastructure.repository.auth

import cats._
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.implicits.legacy.instant._
import tsec.authentication._
import tsec.common.SecureRandomId
import tsec.jws.JWSSerializer
import tsec.jws.mac._

import java.time.Instant

private object AuthSQL {
  implicit val secureRandomIdPut: Put[SecureRandomId] =
    Put[String].contramap((_: Id[SecureRandomId]).widen)

  def insert[A](jwt: AugmentedJWT[A, Long])(implicit hs: JWSSerializer[JWSMacHeader[A]]): Update0 = sql"""
        INSERT INTO JWT (ID, JWT, IDENTITY, EXPIRY, LAST_TOUCHED)
        VALUES (${jwt.id}, ${jwt.jwt.toEncodedString}, ${jwt.identity}, ${jwt.expiry}, ${jwt.lastTouched})
       """.update

  def update[A](jwt: AugmentedJWT[A, Long])(implicit hs: JWSSerializer[JWSMacHeader[A]]): Update0 = sql"""
        UPDATE JWT SET JWT = ${jwt.jwt.toEncodedString}, IDENTITY = ${jwt.identity}, EXPIRY = ${jwt.expiry}, LAST_TOUCHED = ${jwt.lastTouched}
        WHERE ID = ${jwt.id}
       """.stripMargin.update

  def delete(id: SecureRandomId): Update0 = sql"""
        DELETE FROM JWT WHERE ID = $id""".update

  def select(id: SecureRandomId): Query0[(String, Long, Instant, Option[Instant])] = sql"""
    SELECT JWT, IDENTITY, EXPIRY, LAST_TOUCHED
    FROM JWT
    WHERE ID = $id
      """.query[(String, Long, Instant, Option[Instant])]
}
