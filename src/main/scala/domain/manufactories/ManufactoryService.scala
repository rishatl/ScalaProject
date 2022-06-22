package domain.manufactories

import cats.syntax.all._
import cats.{Functor, Monad}
import cats.data.EitherT
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

  def findByStatus(status: ManufactoryStatus): F[Option[Manufactory]] =
    repository.findByStatus(status)

  def updateStatus(status: ManufactoryStatus)(implicit M: Monad[F]): F[Unit] = {
    for {
      manufactory <- findByStatus(status).flatMap {
      case Some(x) => x.copy(status = ManufactoryStatus.NotAvailable).some.pure[F]
    }
      _ <- update(manufactory.get).pure[F]
    } yield()
  }

  def getIdByStatus(status: ManufactoryStatus)(implicit M: Monad[F]): F[Long] = {
    findByStatus(status).flatMap {
      case Some(manufactory) => manufactory.id.get.pure[F]
    }
  }
}

object ManufactoryService {
  def apply[F[_]](repository: ManufactoryRepositoryAlgebra[F]): ManufactoryService[F] =
    new ManufactoryService[F](repository)
}
