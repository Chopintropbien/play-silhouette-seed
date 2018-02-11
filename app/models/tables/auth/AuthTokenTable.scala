package models.tables.auth

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

case class DbAuthToken(
  id: String,
  userID: String,
  expiry: String
)

class AuthTokenTable(tag: Tag) extends Table[DbAuthToken](tag, "auth_tokens") {

  def id: Rep[String] = column[String]("id", O.PrimaryKey)

  def userID: Rep[String] = column[String]("user_id")

  def expiry: Rep[String] = column[String]("expiry")

  override def * : ProvenShape[DbAuthToken] = (id, userID, expiry) <> (DbAuthToken.tupled, DbAuthToken.unapply)

}
