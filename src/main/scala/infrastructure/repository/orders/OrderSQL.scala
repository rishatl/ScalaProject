package infrastructure.repository.orders

import doobie._
import doobie.implicits._
import doobie.implicits.legacy.instant._
import domain.orders.Order

private object OrderSQL {

  def select(orderId: Long): Query0[Order] = sql"""
    SELECT PRICE, MANUFACTORY_ID, CAR_ID, ORDER_DATE, COMPLETE, ID
    FROM ORDERS
    WHERE ID = $orderId
  """.query[Order]

  def insert(order: Order): Update0 = sql"""
    INSERT INTO ORDERS (PRICE, MANUFACTORY_ID, CAR_ID, ORDER_DATE, COMPLETE)
    VALUES (${order.price}, ${order.manufactoryId}, ${order.carId}, ${order.orderDate}, ${order.complete})
  """.update

  def delete(orderId: Long): Update0 = sql"""
    DELETE FROM ORDERS
    WHERE ID = $orderId
  """.update
}
