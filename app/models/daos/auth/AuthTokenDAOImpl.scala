package models.daos.auth

import java.util.UUID
import javax.inject.Inject

import models.AuthToken
import models.daos.auth.AuthTokenDAOImpl._
import models.tables.auth.DbAuthToken
import models.tables.auth.{ AuthTokenTable, DbAuthToken }
import org.joda.time.DateTime

import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcBackend
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.db.slick.DatabaseConfigProvider

/**
 * Give access to the [[AuthToken]] object.
 */
class AuthTokenDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends AuthTokenDAO {

  val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]
  val db: JdbcBackend#DatabaseDef = dbConfig.db

  import dbConfig.profile.api._

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID): Future[Option[AuthToken]] = {
    db.run(tokens.filter(_.id === id.toString).result.headOption).map { resultOption =>
      resultOption.map {
        dbToken => AuthToken(UUID.fromString(dbToken.id), UUID.fromString(dbToken.userID), DateTime.parse(dbToken.expiry))
      }
    }
  }

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken): Future[AuthToken] = {
    db.run((
      tokens returning tokens.map(_.id) into ((token, id) => token.copy(id))
    ) += DbAuthToken(token.id.toString, token.userID.toString, token.expiry.toString)).map { _ =>
      token
    }
  }

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID): Future[Int] = {
    db.run(tokens.filter(_.id === id.toString).delete)
  }
}

/**
 * The companion object.
 */
object AuthTokenDAOImpl {

  /**
   * The list of tokens.
   */
  private val tokens = TableQuery[AuthTokenTable]

}

//  /**
//   * Finds expired tokens.
//   *
//   * @param dateTime The current date time.
//   */
//  def findExpired(dateTime: DateTime): Future[Seq[AuthToken]] = {
//    val q = tokens.filter { sqlww: DbAuthToken =>
//      DateTime.parse(sqlww.expiry).isBefore(dateTime)
//    }
//    val action = q.result.map { futureSeq =>
//      futureSeq.map {
//        dbToken => AuthToken(UUID.fromString(dbToken.id), UUID.fromString(dbToken.userID), DateTime.parse(dbToken.expiry))
//      }
//    }
//    val result: Future[Seq[AuthToken]] = db.run(action)
//    result
//  }
