import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Timer}
import config.{MeetingConfig, DatabaseConfig}
import domain.authentification.Auth
import domain.users.{UserService, UserValidationInterpreter}
import doobie.util.ExecutionContexts
import infrastructure.endpoint.UserEndpoints
import infrastructure.repository.{DoobieAuthRepositoryInterpreter, DoobieUserRepositoryInterpreter}
import io.circe.config.parser
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.{Router, Server => H4Server}
import org.http4s.server.blaze.BlazeServerBuilder
import tsec.authentication.SecuredRequestHandler
import tsec.mac.jca.HMACSHA256
import tsec.passwordhashers.jca.BCrypt

object Server extends IOApp {
  def createServer[F[_]: ContextShift: ConcurrentEffect: Timer]: Resource[F, H4Server[F]] =
    for {
      conf <- Resource.eval(parser.decodePathF[F, MeetingConfig]("concerts"))
      serverEc <- ExecutionContexts.cachedThreadPool[F]
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.db.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DatabaseConfig.dbTransactor(conf.db, connEc, Blocker.liftExecutionContext(txnEc))
      key <- Resource.eval(HMACSHA256.generateKey[F])
      authRepo = DoobieAuthRepositoryInterpreter[F, HMACSHA256](key, xa)
//      meetingRepo = DoobieMeetingRepositoryInterpreter[F](xa)
//      teamRepo = DoobieOrderRepositoryInterpreter[F](xa)
      userRepo = DoobieUserRepositoryInterpreter[F](xa)
//      meetingValidation = MeetingValidationInterpreter[F](meetingRepo)
//      meetingService = MeetingService[F](meetingRepo, meetingValidation)
      userValidation = UserValidationInterpreter[F](userRepo)
//      teamService = TeamService[F](teamRepo)
      userService = UserService[F](userRepo, userValidation)
      authenticator = Auth.jwtAuthenticator[F, HMACSHA256](key, authRepo, userRepo)
      routeAuth = SecuredRequestHandler(authenticator)
      httpApp = Router(
        "/users" -> UserEndpoints
          .endpoints[F, BCrypt, HMACSHA256](userService, BCrypt.syncPasswordHasher[F], routeAuth),
//        "/meetings" -> MeetingEndpoints.endpoints[F, HMACSHA256](meetingService, routeAuth),
//        "/teams" -> TeamEndpoints.endpoints[F, HMACSHA256](teamService, routeAuth),
      ).orNotFound
      _ <- Resource.eval(DatabaseConfig.initializeDb(conf.db))
      server <- BlazeServerBuilder[F](serverEc)
        .bindHttp(conf.server.port, conf.server.host)
        .withHttpApp(httpApp)
        .resource
    } yield server

  def run(args: List[String]): IO[ExitCode] = createServer.use(_ => IO.never).as(ExitCode.Success)
}