package infrastructure.endpoint

import cats.effect.Sync
import cats.syntax.all._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import tsec.authentication.{AugmentedJWT, SecuredRequestHandler, asAuthed}
import tsec.jwt.algorithms.JWTMacAlgo
import domain.OrderNotFoundError
import domain.authentification._
import domain.manufactories.{ManufactoryService, ManufactoryStatus}
import domain.orders.{Order, OrderDto, OrderService}
import domain.users.User

class OrderEndpoints[F[_]: Sync, Auth: JWTMacAlgo] extends Http4sDsl[F] {
  /* Needed to decode entities */
  implicit val orderDecoder: EntityDecoder[F, OrderDto] = jsonOf

  private def createOrderEndpoint(orderService: OrderService[F], manufactoryService: ManufactoryService[F]): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root / "carId" / LongVar(id) asAuthed _ =>
      val action = for {
        order <- req.request.as[OrderDto]
        some <- manufactoryService.getIdByStatus(ManufactoryStatus.Available)
        _ <- manufactoryService.updateStatus(ManufactoryStatus.Available)
        saved <- orderService.createOrder(order.asOrder(id, some))
      } yield saved

      action.flatMap(saved => Ok(saved.asJson))
  }

  private def getOrderEndpoint(orderService: OrderService[F]): AuthEndpoint[F, Auth] = {
    case GET -> Root / LongVar(id) asAuthed _ =>
      orderService.get(id).value.flatMap {
        case Right(found) => Ok(found.asJson)
        case Left(OrderNotFoundError) => NotFound("The order was not found")
      }
  }

  private def deleteOrderEndpoint(orderService: OrderService[F]): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      orderService.get(id).value.flatMap {
        case Right(found) => for {
          _ <- orderService.delete(id)
          resp <- Ok(found.asJson)
        } yield resp
        case Left(OrderNotFoundError) => NotFound("The order was not found")
      }
  }

  def endpoints(orderService: OrderService[F],
                 manufactoryService: ManufactoryService[F],
                 auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
               ): HttpRoutes[F] = {
    val authEndpoints: AuthService[F, Auth] =
      Auth.allRolesHandler(
        getOrderEndpoint(orderService),
      ) {
        Auth.adminOnly(deleteOrderEndpoint(orderService)
        .orElse(createOrderEndpoint(orderService, manufactoryService)))
      }

    auth.liftService(authEndpoints)
  }
}

object OrderEndpoints {
  def endpoints[F[_]: Sync, Auth: JWTMacAlgo](orderService: OrderService[F],
                                               manufactoryService: ManufactoryService[F],
                                               auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
                                             ): HttpRoutes[F] =
    new OrderEndpoints[F, Auth].endpoints(orderService, manufactoryService, auth)
}
