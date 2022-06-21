package domain.manufactories

import cats.{Functor, Monad}
import cats.data.EitherT
import cats.implicits._
import domain.{ManufactoryAlreadyExistsError, ManufactoryNotFoundError}

class ManufactoryService[F[_]](repository: ManufactoryRepositoryAlgebra[F]) {
  def create(manufactory: Manufactory)(implicit M: Monad[F]): EitherT[F, ManufactoryAlreadyExistsError, Manufactory] =
    for {
      saved <- EitherT.liftF(repository.create(manufactory))
    } yield saved

  /* Could argue that we could make this idempotent on put and not check if the pet exists */
  def update(manufactory: Manufactory)(implicit M: Monad[F]): EitherT[F, ManufactoryNotFoundError.type, Manufactory] =
    for {
      saved <- EitherT.fromOptionF(repository.update(manufactory), ManufactoryNotFoundError)
    } yield saved

  def get(id: Long)(implicit F: Functor[F]): EitherT[F, ManufactoryNotFoundError.type, Manufactory] =
    EitherT.fromOptionF(repository.get(id), ManufactoryNotFoundError)

  /* In some circumstances we may care if we actually delete the pet; here we are idempotent and do not care */
  def delete(id: Long)(implicit F: Functor[F]): F[Unit] =
    repository.delete(id).as(())
}

object ManufactoryService {
  def apply[F[_]](repository: ManufactoryRepositoryAlgebra[F]): ManufactoryService[F] =
    new ManufactoryService[F](repository)
}
