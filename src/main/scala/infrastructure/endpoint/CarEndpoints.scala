package infrastructure.endpoint

import cats.data.Validated.Valid
import cats.implicits._
import cats.effect.Sync
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.authentication._
import tsec.jwt.algorithms._
import domain.authentification.Auth
import domain.cars._
import domain.users.{User, UserService}
import domain.{CarAlreadyExistsError, CarNotFoundError, UserNotFoundError}

class CarEndpoints[F[_] : Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {

  object NameMatcher extends OptionalMultiQueryParamDecoderMatcher[String]("carNumber")

  implicit val carDtoDecoder: EntityDecoder[F, CarDto] = jsonOf[F, CarDto]
  implicit val carDecoder: EntityDecoder[F, Car] = jsonOf[F, Car]

  private def createCarEndpoint(carService: CarService[F]): AuthEndpoint[F, Auth] = {
    case req@POST -> Root asAuthed user =>
      val action = for {
        reqCar <- req.request.as[CarDto]
        car <- reqCar.asCar(user.id.get).pure[F]
        result <- carService.create(car).value
      } yield result

      action.flatMap {
        case Right(saved) =>
          Ok(saved.asJson)
        case Left(CarAlreadyExistsError(existing)) =>
          Conflict(s"The car ${existing.carNumber} already exists")
      }
  }

  private def updateCarEndpoint(carService: CarService[F]): AuthEndpoint[F, Auth] = {
    case req@POST -> Root / LongVar(_) asAuthed _ =>
      val action = for {
        car <- req.request.as[Car]
        result <- carService.update(car).value
      } yield result

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(CarNotFoundError) => NotFound("The car was not found")
      }
  }

  private def getCarEndpoint(carService: CarService[F]): AuthEndpoint[F, Auth] = {
    case GET -> Root / LongVar(id) asAuthed _ =>
      carService.get(id).value.flatMap {
        case Right(found) => Ok(found.asJson)
        case Left(CarNotFoundError) => NotFound("The car was not found")
      }
  }

  private def deleteCarEndpoint(carService: CarService[F]): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      for {
        _ <- carService.delete(id)
        resp <- Ok()
      } yield resp
  }

  private def findCarByCarNumberEndpoint(carService: CarService[F]): AuthEndpoint[F, Auth] = {
    case GET -> Root / "findByCarNumber" :? NameMatcher(Valid(carNumber)) asAuthed _ =>
      carService.findCarByCarNumber(carNumber.head).value.flatMap {
        case Right(found) => Ok(found.asJson)
        case Left(CarNotFoundError) => NotFound("The car was not found")
      }
  }

  private def findOwnerByCarId(carService: CarService[F], userService: UserService[F]): AuthEndpoint[F, Auth] = {
    case GET -> Root / "findOwnerByCarId" / LongVar(id) asAuthed _ =>
      carService.get(id).value.flatMap {
      case Right(found) => {
        userService.getUser(found.ownerId).value.flatMap {
          case Right(found) => Ok(found.asJson)
          case Left(UserNotFoundError) => NotFound("The user was not found")
        }
      }
      case Left(CarNotFoundError) => NotFound("The car was not found")
    }
  }

  def endpoints(carService: CarService[F],
                userService: UserService[F],
                auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]): HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] = {
      val allRoles =
        getCarEndpoint(carService)
          .orElse(createCarEndpoint(carService))
          .orElse(updateCarEndpoint(carService))
          .orElse(findCarByCarNumberEndpoint(carService))

      val onlyAdmin = deleteCarEndpoint(carService)
        .orElse(findOwnerByCarId(carService, userService))

      Auth.allRolesHandler(allRoles)(Auth.adminOnly(onlyAdmin))
    }

    auth.liftService(authEndpoints)
  }
}

object CarEndpoints {
  def endpoints[F[_] : Sync, Auth: JWTMacAlgo](carService: CarService[F],
                                               userService: UserService[F],
                                               auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]): HttpRoutes[F] =
    new CarEndpoints[F, Auth].endpoints(carService, userService, auth)
}
