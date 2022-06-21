package domain.users

import cats.Applicative
import tsec.authorization.AuthorizationInfo

case class User(
  userName: String,
  firstName: String,
  lastName: String,
  phone: String,
  hash: String,
  id: Option[Long] = None,
  role: Role
){
  def toUserWithoutHash: UserWithoutHash = {
    UserWithoutHash(userName, firstName, lastName, phone, id, role)
  }
}

case class UserWithoutHash(
  userName: String,
  firstName: String,
  lastName: String,
  phone: String,
  id: Option[Long] = None,
  role: Role,
)

object User {
  implicit def authRole[F[_]](implicit F: Applicative[F]): AuthorizationInfo[F, Role, User] =
    new AuthorizationInfo[F, Role, User] {
      def fetchInfo(u: User): F[Role] = F.pure(u.role)
    }
}
