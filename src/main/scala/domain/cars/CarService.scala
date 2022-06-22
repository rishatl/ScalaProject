package domain.cars

import cats.{Functor, Monad}
import cats.syntax.all._
import cats.data._
import domain._

class CarService[F[_]](repository: CarRepositoryAlgebra[F]) {
  def create(car: Car)(implicit M: Monad[F]): EitherT[F, CarAlreadyExistsError, Car] =
    for {
      saved <- EitherT.liftF(repository.create(car))
    } yield saved

  /* Could argue that we could make this idempotent on put and not check if the pet exists */
  def update(car: Car)(implicit M: Monad[F]): EitherT[F, CarNotFoundError.type, Car] =
    for {
      saved <- EitherT.fromOptionF(repository.update(car), CarNotFoundError)
    } yield saved

  def get(id: Long)(implicit F: Functor[F]): EitherT[F, CarNotFoundError.type, Car] =
    EitherT.fromOptionF(repository.get(id), CarNotFoundError)

  /* In some circumstances we may care if we actually delete the pet; here we are idempotent and do not care */
  def delete(id: Long)(implicit F: Functor[F]): F[Unit] =
    repository.delete(id).as(())

  def findCarByCarNumber(carNumber: String)(implicit F: Functor[F]): EitherT[F, CarNotFoundError.type, Car] =
    EitherT.fromOptionF(repository.findByCarNumber(carNumber), CarNotFoundError)
}

object CarService {
  def apply[F[_]](repository: CarRepositoryAlgebra[F]): CarService[F] =
    new CarService[F](repository)
}
