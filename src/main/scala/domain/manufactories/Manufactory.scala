package domain.manufactories

case class Manufactory(
  name: String,
  status: ManufactoryStatus,
  id: Option[Long] = None
)

case class ManufactoryDto(
  name: String,
  id: Option[Long] = None
) {
  def asManufactory(): Manufactory = Manufactory(
    name,
    status = ManufactoryStatus.Available,
    id
  )
}
