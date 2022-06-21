package domain.cars

trait CarRepositoryAlgebra[F[_]] {
  def create(team: Car): F[Car]

  def update(team: Car): F[Option[Car]]

  def get(id: Long): F[Option[Car]]

  def delete(id: Long): F[Option[Car]]

  def findByCarNumber(carNumber: String): F[Option[Car]]
}
