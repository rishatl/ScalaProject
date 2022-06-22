package infrastructure.repository.manufactories

import cats.data._
import cats.effect._
import cats.implicits._
import domain.manufactories._
import doobie._
import doobie.implicits._

class DoobieManufactoryRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends ManufactoryRepositoryAlgebra[F] with DoobieManufactoryRepositoryAlgebra[F] {
  import infrastructure.repository.manufactories.ManufactorySQL._

  def create(manufactory: Manufactory): F[Manufactory] =
    insert(manufactory)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => manufactory.copy(id = id.some)).transact(xa)

  def update(manufactory: Manufactory): F[Option[Manufactory]] =
    OptionT
      .fromOption[ConnectionIO](manufactory.id)
      .semiflatMap(id => ManufactorySQL.update(manufactory, id).run.as(manufactory))
      .value
      .transact(xa)

  def get(id: Long): F[Option[Manufactory]] = select(id).option.transact(xa)

  def delete(id: Long): F[Option[Manufactory]] =
    OptionT(select(id).option).semiflatMap(pet => ManufactorySQL.delete(id).run.as(pet)).value.transact(xa)

  def findByStatus(status: ManufactoryStatus): F[Option[Manufactory]] =
    selectByStatus(status).option.transact(xa)
}

object DoobieManufactoryRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): DoobieManufactoryRepositoryInterpreter[F] =
    new DoobieManufactoryRepositoryInterpreter(xa)
}
