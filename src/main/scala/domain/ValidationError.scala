package domain

import domain.cars.Car
import domain.manufactories.Manufactory
import domain.users.User

sealed trait ValidationError extends Product with Serializable

case object UserNotFoundError extends ValidationError
case class UserAlreadyExistsError(user: User) extends ValidationError
case class UserAuthenticationFailedError(userName: String) extends ValidationError

case class CarAlreadyExistsError(car: Car) extends ValidationError
case object CarNotFoundError extends ValidationError

case class ManufactoryAlreadyExistsError(manufactory: Manufactory) extends ValidationError
case object ManufactoryNotFoundError extends ValidationError

case object OrderNotFoundError extends ValidationError