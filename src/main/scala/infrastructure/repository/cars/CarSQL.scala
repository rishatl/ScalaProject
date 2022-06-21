package infrastructure.repository.cars

import domain.cars._
import doobie._
import doobie.implicits._

private object CarSQL {

  def insert(car: Car): Update0 = sql"""
    INSERT INTO CARS (MODEL, CAR_NUMBER, OWNER_ID)
    VALUES (${car.model}, ${car.carNumber}, ${car.ownerId})
  """.update

  def update(car: Car, id: Long): Update0 = sql"""
    UPDATE CARS
    SET MODEL = ${car.model}, CAR_NUMBER = ${car.carNumber}, OWNER_ID = ${car.ownerId}
    WHERE id = $id
  """.update

  def select(id: Long): Query0[Car] = sql"""
    SELECT MODEL, CAR_NUMBER, OWNER_ID, ID
    FROM CARS
    WHERE ID = $id
  """.query

  def delete(id: Long): Update0 = sql"""
    DELETE FROM CARS WHERE ID = $id
  """.update

  def selectByCarNumber(carNumber: String): Query0[Car] = sql"""
    SELECT MODEL, CAR_NUMBER, OWNER_ID, ID
    FROM CARS
    WHERE CAR_NUMBER = $carNumber
  """.query[Car]

  def selectAll: Query0[Car] = sql"""
    SELECT MODEL, CAR_NUMBER, OWNER_ID, ID
    FROM CARS
    ORDER BY MODEL
  """.query
}
