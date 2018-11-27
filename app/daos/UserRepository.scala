package daos

import models._
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ Future, ExecutionContext }

@Singleton
class UserRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def password = column[String]("password")

    def * = (id, username, password) <> ((User.apply _).tupled, User.unapply)
  }

  private val users = TableQuery[UserTable]
 
  def create(username: String, password: String): Future[User] = db.run {
    (users.map(u => (u.username, u.password))
      returning users.map(_.id)
      into ((userInfo, id) => User(id, userInfo._1, userInfo._2))
    ) += (username, password)
  }

  def list(): Future[Seq[User]] = db.run {
    users.result
  }

  def exists(id: Long): Future[Boolean] = db.run {
    users.filter(_.id === id).exists.result
  }

  def find(id: Long): Future[Seq[User]] = db.run {
    users.filter(_.id === id).result
  }

  def delete(id: Long) = db.run {
    users.filter(_.id === id).delete
  }

  def update(id: Long, username: String, password: String) = db.run {
    val updatedUser = User(id, username, password)
    users.filter(_.id === id).update(updatedUser)
  }

  def usernames(): Future[Seq[String]] = db.run {
    users.map(_.username).result
  }
}
