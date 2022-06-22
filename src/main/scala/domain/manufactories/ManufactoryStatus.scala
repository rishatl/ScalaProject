package domain.manufactories

import enumeratum._

sealed trait ManufactoryStatus extends EnumEntry

case object ManufactoryStatus extends Enum[ManufactoryStatus] with CirceEnum[ManufactoryStatus] {
  case object Available extends ManufactoryStatus
  case object NotAvailable extends ManufactoryStatus

  val values: IndexedSeq[ManufactoryStatus] = findValues
}
