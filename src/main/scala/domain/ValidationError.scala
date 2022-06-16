package domain

import domain.users.User

sealed trait ValidationError extends Product with Serializable
case object UserNotFoundError extends ValidationError
case class UserAlreadyExistsError(user: User) extends ValidationError
case class UserAuthenticationFailedError(userName: String) extends ValidationError
