package infrastructure.endpoint

import cats.implicits._
import cats.effect.Sync
import domain.{ManufactoryAlreadyExistsError, ManufactoryNotFoundError}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.authentication._
import tsec.jwt.algorithms._

import domain.authentification.Auth
import domain.manufactories._
import domain.users.User

class ManufactoryEndpoints[F[_] : Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {

  implicit val manufactoryDecoder: EntityDecoder[F, ManufactoryDto] = jsonOf[F, ManufactoryDto]

  private def createManufactoryEndpoint(manufactoryService: ManufactoryService[F]): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root asAuthed _ =>
      val action = for {
        manufactoryDto <- req.request.as[ManufactoryDto]
        result <- manufactoryService.create(manufactoryDto.asManufactory()).value
      } yield result

      action.flatMap {
        case Right(saved) =>
          Ok(saved.asJson)
        case Left(ManufactoryAlreadyExistsError(existing)) =>
          Conflict(s"The manufactory ${existing.name} already exists")
      }
  }

  private def updateManufactoryEndpoint(manufactoryService: ManufactoryService[F]): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / LongVar(_) asAuthed _ =>
      val action = for {
        car <- req.request.as[ManufactoryDto]
        result <- manufactoryService.update(car.asManufactory()).value
      } yield result

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(ManufactoryNotFoundError) => NotFound("The manufactory was not found")
      }
  }

  private def getManufactoryEndpoint(manufactoryService: ManufactoryService[F]): AuthEndpoint[F, Auth] = {
    case GET -> Root / LongVar(id) asAuthed _ =>
      manufactoryService.get(id).value.flatMap {
        case Right(found) => Ok(found.asJson)
        case Left(ManufactoryNotFoundError) => NotFound("The manufactory was not found")
      }
  }

  private def deleteManufactoryEndpoint(manufactoryService: ManufactoryService[F]): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      for {
        _ <- manufactoryService.delete(id)
        resp <- Ok()
      } yield resp
  }

  def endpoints(manufactoryService: ManufactoryService[F],
                auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]): HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] = {
      val allRoles =
        getManufactoryEndpoint(manufactoryService)

      val onlyAdmin = deleteManufactoryEndpoint(manufactoryService)
        .orElse(createManufactoryEndpoint(manufactoryService))
        .orElse(updateManufactoryEndpoint(manufactoryService))

      Auth.allRolesHandler(allRoles)(Auth.adminOnly(onlyAdmin))
    }

    auth.liftService(authEndpoints)
  }
}

object ManufactoryEndpoints {
  def endpoints[F[_] : Sync, Auth: JWTMacAlgo](manufactoryService: ManufactoryService[F],
                                               auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]): HttpRoutes[F] =
    new ManufactoryEndpoints[F, Auth].endpoints(manufactoryService, auth)
}
