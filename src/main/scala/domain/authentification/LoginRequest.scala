package domain.authentification

import domain.users.{Role, User}
import tsec.passwordhashers.PasswordHash

final case class LoginRequest(
  userName: String,
  password: String,
)

final case class SignupRequest(
  userName: String,
  firstName: String,
  lastName: String,
  phone: String,
  password: String
){
  def asUser[A](hashedPassword: PasswordHash[A]): User = User(
    userName,
    firstName,
    lastName,
    phone,
    hashedPassword.toString,
    role = Role("Customer")
  )
}
