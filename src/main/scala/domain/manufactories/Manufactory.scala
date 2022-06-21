package domain.manufactories

case class Manufactory(
  name: String,
  status: ManufactoryStatus = ManufactoryStatus.Available,
  id: Option[Long] = None
)
