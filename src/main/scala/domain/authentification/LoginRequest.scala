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
                                email: String,
                                password: String,
                                role: Role
                              ) {
  def asUser[A](hashedPassword: PasswordHash[A]): User = User(
    userName,
    firstName,
    lastName,
    email,
    hashedPassword.toString,
    role = role,
  )
}
