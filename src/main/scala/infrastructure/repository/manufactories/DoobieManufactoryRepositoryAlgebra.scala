package infrastructure.repository.manufactories

import domain.manufactories.Manufactory

trait DoobieManufactoryRepositoryAlgebra[F[_]] {
  def create(manufactory: Manufactory): F[Manufactory]

  def update(manufactory: Manufactory): F[Option[Manufactory]]

  def get(id: Long): F[Option[Manufactory]]

  def delete(id: Long): F[Option[Manufactory]]
}
