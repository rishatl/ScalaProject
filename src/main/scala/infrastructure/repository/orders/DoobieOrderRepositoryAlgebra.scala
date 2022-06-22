package infrastructure.repository.orders

import domain.orders.Order

trait DoobieOrderRepositoryAlgebra[F[_]] {
  def create(order: Order): F[Order]

  def get(orderId: Long): F[Option[Order]]

  def delete(orderId: Long): F[Option[Order]]
}
