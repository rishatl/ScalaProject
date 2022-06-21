package infrastructure.repository

import cats.data._
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import domain.cars._

private object CarSQL {

  def insert(car: Car): Update0 = sql"""
    INSERT INTO CARS (MODEL, CAR_NUMBER, OWNER_ID)
    VALUES (${car.model}, ${car.carNumber}, ${car.ownerId})
  """.update

  def update(car: Car, id: Long): Update0 = sql"""
    UPDATE CARS
    SET MODEL = ${car.model}, CAR_NUMBER = ${car.carNumber}, OWNER_ID = ${car.ownerId}
    WHERE id = $id
  """.update

  def select(id: Long): Query0[Car] = sql"""
    SELECT MODEL, CAR_NUMBER, OWNER_ID, ID
    FROM CARS
    WHERE ID = $id
  """.query

  def delete(id: Long): Update0 = sql"""
    DELETE FROM CARS WHERE ID = $id
  """.update

  def selectByCarNumber(carNumber: String): Query0[Car] = sql"""
    SELECT MODEL, CAR_NUMBER, OWNER_ID, ID
    FROM CARS
    WHERE CAR_NUMBER = $carNumber
  """.query[Car]

  def selectAll: Query0[Car] = sql"""
    SELECT MODEL, CAR_NUMBER, OWNER_ID, ID
    FROM CARS
    ORDER BY MODEL
  """.query
}

class DoobieCarRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends CarRepositoryAlgebra[F] {
  import CarSQL._

  def create(car: Car): F[Car] =
    insert(car).withUniqueGeneratedKeys[Long]("id").map(id => car.copy(id = id.some)).transact(xa)

  def update(car: Car): F[Option[Car]] =
    OptionT
      .fromOption[ConnectionIO](car.id)
      .semiflatMap(id => CarSQL.update(car, id).run.as(car))
      .value
      .transact(xa)

  def get(id: Long): F[Option[Car]] = select(id).option.transact(xa)

  def delete(id: Long): F[Option[Car]] =
    OptionT(select(id).option).semiflatMap(pet => CarSQL.delete(id).run.as(pet)).value.transact(xa)

  def findByCarNumber(carNumber: String): F[Option[Car]] =
    selectByCarNumber(carNumber).option.transact(xa)
}

object DoobieCarRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): DoobieCarRepositoryInterpreter[F] =
    new DoobieCarRepositoryInterpreter(xa)
}
