package infrastructure.repository.orders

import cats.data.OptionT
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.implicits.legacy.instant._
import domain.orders.{Order, OrderRepositoryAlgebra}

class DoobieOrderRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends OrderRepositoryAlgebra[F] with DoobieOrderRepositoryAlgebra[F] {
  import infrastructure.repository.orders.OrderSQL._

  def create(order: Order): F[Order] =
    insert(order)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => order.copy(id = id.some))
      .transact(xa)

  def get(orderId: Long): F[Option[Order]] =
    OrderSQL.select(orderId).option.transact(xa)

  def delete(orderId: Long): F[Option[Order]] =
    OptionT(get(orderId))
      .semiflatMap(order => OrderSQL.delete(orderId).run.transact(xa).as(order))
      .value
}

object DoobieOrderRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](
                                             xa: Transactor[F],
                                           ): DoobieOrderRepositoryInterpreter[F] =
    new DoobieOrderRepositoryInterpreter(xa)
}