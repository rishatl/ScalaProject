import cats.effect._
import config._
import doobie.util.ExecutionContexts
import io.circe.config.parser
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.{Router, Server => H4Server}
import org.http4s.server.blaze.BlazeServerBuilder

import domain.authentification.Auth
import domain.cars.CarService
import domain.manufactories.ManufactoryService
import domain.orders.OrderService
import domain.users._

import infrastructure.endpoint._
import infrastructure.repository._
import infrastructure.repository.auth.DoobieAuthRepositoryInterpreter
import infrastructure.repository.users.DoobieUserRepositoryInterpreter
import infrastructure.repository.cars.DoobieCarRepositoryInterpreter
import infrastructure.repository.manufactories.DoobieManufactoryRepositoryInterpreter
import infrastructure.repository.orders.DoobieOrderRepositoryInterpreter

import tsec.authentication.SecuredRequestHandler
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.jca.BCrypt

object Server extends IOApp {
  def createServer[F[_]: ContextShift: ConcurrentEffect: Timer]: Resource[F, H4Server[F]] =
    for {
      conf <- Resource.eval(parser.decodePathF[F, MeetingConfig]("manufactories"))
      serverEc <- ExecutionContexts.cachedThreadPool[F]
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.db.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor(conf.db, connEc, Blocker.liftExecutionContext(txnEc))
      key <- Resource.eval(HMACSHA256.generateKey[F])
      authRepo = DoobieAuthRepositoryInterpreter[F, HMACSHA256](key, xa)
      carRepo = DoobieCarRepositoryInterpreter[F](xa)
      manufactoryRepo = DoobieManufactoryRepositoryInterpreter[F](xa)
      orderRepo = DoobieOrderRepositoryInterpreter[F](xa)
      userRepo = DoobieUserRepositoryInterpreter[F](xa)
      carService = CarService[F](carRepo)
      manufactoryService = ManufactoryService[F](manufactoryRepo)
      orderService = OrderService[F](orderRepo)
      userValidation = UserValidationInterpreter[F](userRepo)
      userService = UserService[F](userRepo, userValidation)
      authenticator = Auth.jwtAuthenticator[F, HMACSHA256](key, authRepo, userRepo)
      routeAuth = SecuredRequestHandler(authenticator)
      httpApp = Router(
        "/users" -> UserEndpoints
          .endpoints[F, BCrypt, HMACSHA256](userService, BCrypt.syncPasswordHasher[F], routeAuth),
        "/cars" -> CarEndpoints.endpoints[F, HMACSHA256](carService, userService, routeAuth),
        "/manufactories" -> ManufactoryEndpoints.endpoints[F, HMACSHA256](manufactoryService, routeAuth),
        "/orders" -> OrderEndpoints.endpoints[F, HMACSHA256](orderService, manufactoryService, routeAuth),
      ).orNotFound
      _ <- Resource.eval(DatabaseConfig.initializeDb(conf.db))
      server <- BlazeServerBuilder[F](serverEc)
        .bindHttp(conf.server.port, conf.server.host)
        .withHttpApp(httpApp)
        .resource
    } yield server

  def run(args: List[String]): IO[ExitCode] = createServer.use(_ => IO.never).as(ExitCode.Success)
}