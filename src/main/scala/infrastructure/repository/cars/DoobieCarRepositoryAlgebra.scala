package infrastructure.repository.cars

import domain.cars.Car

trait DoobieCarRepositoryAlgebra[F[_]] {
  def create(car: Car): F[Car]

  def update(car: Car): F[Option[Car]]

  def get(id: Long): F[Option[Car]]

  def delete(id: Long): F[Option[Car]]

  def findByCarNumber(carNumber: String): F[Option[Car]]
}
