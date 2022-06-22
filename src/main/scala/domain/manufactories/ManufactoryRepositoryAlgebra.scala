package domain.manufactories

trait ManufactoryRepositoryAlgebra[F[_]] {
  def create(team: Manufactory): F[Manufactory]

  def update(team: Manufactory): F[Option[Manufactory]]

  def get(id: Long): F[Option[Manufactory]]

  def delete(id: Long): F[Option[Manufactory]]

  def findByStatus(status: ManufactoryStatus): F[Option[Manufactory]]
}
