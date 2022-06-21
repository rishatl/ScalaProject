package infrastructure.repository.cars

import cats.data._
import cats.effect.Bracket
import cats.implicits._
import domain.cars._
import doobie._
import doobie.implicits._

class DoobieCarRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends CarRepositoryAlgebra[F] {
  import infrastructure.repository.cars.CarSQL._

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
