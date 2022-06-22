package domain.orders

import java.time.{LocalDate}
import java.time.format.DateTimeFormatter

case class Order(
  price: Int,
  manufactoryId: Long,
  carId: Long,
  orderDate: String,
  complete: Boolean = false,
  id: Option[Long] = None
)

case class OrderDto(
  price: Int,
  id: Option[Long] = None
) {
  def asOrder(_carId: Long, _manufactoryId: Long): Order = {
    val dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    Order(price,
          _manufactoryId,
          _carId,
          orderDate = LocalDate.now().format(dtf),
          complete = false,
          id
    )
  }
}
