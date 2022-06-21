package domain.cars

case class Car(
  model: String,
  carNumber: String,
  ownerId: Long,
  id: Option[Long] = None
)

case class CarDto(
  model: String,
  carNumber: String,
  id: Option[Long] = None
) {
  def asCar(userId: Long): Car = Car(
    model,
    carNumber,
    ownerId = userId,
    id
  )
}