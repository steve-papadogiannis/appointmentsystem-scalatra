package com.example.app.db

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.TimeZone

import com.example.app.model._
import org.json4s.JsonAST
//import slick.jdbc.PostgresProfile.api._
//import slick.jdbc.MySQLProfile.api._
//import slick.jdbc.SQLServerProfile.api._
import slick.jdbc.H2Profile.api._

object Tables {

  // Definition of the USERS table
  class Users(tag: Tag) extends Table[User](tag, "USERS") {
    
    // This is the primary key column
    // This column is auto incremented.
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc) 
    
    def username = column[String]("USERNAME")
    def password = column[String]("PASSWORD")
    def firstName = column[String]("FIRST_NAME")
    def lastName = column[String]("LAST_NAME")
    def email = column[String]("EMAIL")
    def token = column[Option[String]]("TOKEN")
    def enabled = column[Boolean]("ENABLED")

    // Constraints
    // Email must be unique for every user.
    def unique_email = index(s"${tableName}__unique_email", email, unique = true)
    
    // Username must be unique for every user.
    def unique_username = index(s"${tableName}__unique_username", username, unique = true)

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id.?, username, password, firstName, lastName, email,
      token, enabled) <> (User.tupled, User.unapply)
    
  }

  // Table query for the USERS table, represents all tuples of that table
  val users = TableQuery[Users]

  /**
    * A function that searches the users table for a user with the given username.
    * 
    * @param username The username of the user we are searching for.
    * @return A query on users table returning upon invocation a Sequence of User entities.
    *         The Seq will be empty if none of the users have the given username.
    */
  def findUserByUsername(username: String): Query[Users, User, Seq] = {
    for {
      u <- users if u.username === username
    } yield u
  }

  /**
    * A function that searches the users table for a user with the given username.
    *
    * @param tokenVal The token of the user we are searching for.
    * @return A query on users table returning upon invocation a Sequence of User entities.
    *         The Seq will be empty if none of the users have the given token.
    */
  def findUserByToken(tokenVal: String): Query[Users, User, Seq] = {
    for {
      u <- users if u.token === tokenVal
    } yield u
  }

  /**
    * A function that searches the users table for a user with the given id.
    *
    * @param id The id of the user we are searching for.
    * @return A query on users table returning upon invocation a Sequence of tokens.
    *         The Seq will be empty if none of the users have the given id.
    */
  def findUserByIdProjectToken(id: Option[Long]): Query[Rep[Option[String]], Option[String], Seq] = {
    for {
      u <- users if (id match {
        case Some(idVal) => u.id === idVal
        case None        => u.id =!= u.id
      })
    } yield u.token
  }

  /**
    * A function that searches the users table for a user with the given id.
    *
    * @param id The id of the user we are searching for.
    * @return A query on users table returning upon invocation a Sequence of User entities.
    *         The Seq will be empty if none of the users have the given id
    */
  def findUserById(id: Long): Query[Users, User, Seq] = {
    for {
      u <- users if u.id === id
    } yield u
  }

  // Definition of the ROLES table
  class Roles(tag: Tag) extends Table[Role](tag, "ROLES") {

    // This is the primary key column
    // This column is auto incremented.
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def role = column[String]("ROLE")

    // Constraints
    // Role must be unique in the system.
    def unique_role = index(s"${tableName}__unique_role", role, unique = true)

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id.?, role) <> (Role.tupled, Role.unapply)

  }

  // Table query for the ROLES table, represents all tuples of that table
  val roles = TableQuery[Roles]

  /**
    * A function that searches the roles table for the role of the given name.
    *
    * @param roleName The name of the role we are searching for.
    * @return A query on roles table returning upon invocation a Sequence of roles.
    *         The Seq will be empty if none of the roles have the given roleName.
    */
  def findRoleByRoleName(roleName: String): Query[Roles, Role, Seq] = {
    for {
      r <- roles if r.role === roleName
    } yield r
  }

  // Definition of the USERS_ROLES table
  class UsersRoles(tag: Tag) extends Table[UserRole](tag, "USERS_ROLES") {

    // This is the primary key column
    // This column is auto incremented.
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("USER_ID")
    def roleId = column[Long]("ROLE_ID")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id.?, userId, roleId) <> (UserRole.tupled, UserRole.unapply)

    // Foreign key on Users table which constrains rows of this table to consist of
    // user ids that exist in the Users table.
    // On deletion of a user the rows corresponding to him on this table will be also deleted.
    def userFK = foreignKey("FK_USERS", userId,
      TableQuery[Users])(user => user.id, onDelete = ForeignKeyAction.Cascade)

    // Foreign key on Roles table which constrains rows of this table to consist of
    // role ids that exist in the Roles table.
    // On deletion of a role, the rows corresponding to it on this table will be also deleted.
    def roleFK = foreignKey("FK_ROLES", roleId,
      TableQuery[Roles])(role => role.id, onDelete = ForeignKeyAction.Cascade)
  }

  // Table query for the USERS_ROLES table, represents all tuples of that table
  val usersRoles = TableQuery[UsersRoles]

  /**
    * A function that searches the usersRoles table for the role of the user with given id.
    *
    * @param id The id of the user we are searching for.
    * @return A query on usersRoles table returning upon invocation a Sequence of roles.
    *         The Seq will be empty if none of the roles have the given user id.
    */
  def findRoleByUserId(id: Option[Long]): Query[Rep[String], String, Seq] = {
    for {
      u <- usersRoles if (id match {
        case Some(idVal) => u.id === idVal
        case None        => u.id =!= u.id
      })
      r <- u.roleFK
    } yield r.role
  }

  // Definition of the SPECIALTIES table
  class Specialties(tag: Tag) extends Table[Specialty](tag, "SPECIALTIES") {

    // This is the primary key column
    // This column is auto incremented.
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def specialty = column[String]("SPECIALTY")

    // Constraints
    // Specialty must be unique in the system.
    def unique_specialty = index(s"${tableName}__unique_specialty", specialty, unique = true)

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id.?, specialty) <> (Specialty.tupled, Specialty.unapply)
  }

  // Table query for the SPECIALTIES table, represents all tuples of that table
  val specialties = TableQuery[Specialties]

  // Definition of the USERS_SPECIALTIES table
  class UsersSpecialties(tag: Tag) extends Table[UserSpecialty](tag, "USERS_SPECIALTIES") {

    // This is the primary key column
    // This column is auto incremented.
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("USER_ID")
    def specialtyId = column[Long]("SPECIALTY_ID")

    // Foreign key on Users table which constrains rows of this table to consist of
    // user ids that exist in the Users table.
    // On deletion of a user the rows corresponding to him on this table will be also deleted.
    def userFK = foreignKey("FK_USERS2", userId,
      TableQuery[Users])(user => user.id, onDelete = ForeignKeyAction.Cascade)

    // Foreign key on Specialties table which constrains rows of this table to consist of
    // specialty ids that exist in the Specialties table.
    // On deletion of a role, the rows corresponding to it on this table will be also deleted.
    def specialtyFK = foreignKey("FK_SPECIALTIES", specialtyId,
      TableQuery[Specialties])(specialty => specialty.id, onDelete = ForeignKeyAction.Cascade)

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id.?, userId, specialtyId) <> (UserSpecialty.tupled, UserSpecialty.unapply)

  }

  // Table query for the USERS_SPECIALTIES table, represents all tuples of that table
  val usersSpecialties = TableQuery[UsersSpecialties]

  // Definition of the APPOINTMENTS table
  class Appointments(tag: Tag) extends Table[Appointment](tag, "APPOINTMENTS") {

    // This is the primary key column
    // This column is auto incremented.
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def doctorId = column[Long]("DOCTOR_ID")
    def patientId = column[Long]("PATIENT_ID")
    def startDateTime = column[Timestamp]("START_DATE_TIME")
    def endDateTime = column[Timestamp]("END_DATE_TIME")
    def creationDateTime = column[Timestamp]("CREATION_DATE_TIME")
    def description = column[String]("DESCRIPTION")

    // Foreign key on Users table which constrains rows of this table to consist of
    // user ids that exist in the Users table.
    // On deletion of a user the rows corresponding to him on this table will be also deleted.
    def doctorUserFK = foreignKey("FK_USERS3", doctorId,
      TableQuery[Users])(user => user.id, onDelete = ForeignKeyAction.Cascade)

    // Foreign key on Users table which constrains rows of this table to consist of
    // user ids that exist in the Users table.
    // On deletion of a user the rows corresponding to him on this table will be also deleted.
    def patientUserFK = foreignKey("FK_USERS4", patientId,
      TableQuery[Users])(user => user.id, onDelete = ForeignKeyAction.Cascade)

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id.?, doctorId, patientId, startDateTime, endDateTime, creationDateTime, description) <> (Appointment.tupled, Appointment.unapply)

  }

  // Table query for the APPOINTMENTS table, represents all tuples of that table
  val appointments = TableQuery[Appointments]

  var formatUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
  formatUTC.setTimeZone(TimeZone.getTimeZone("UTC"))

  /* This value stores a sequence of DB IO Actions.
   * One is the creation of the users table schema.
   * The other is the insertion of a user in that table.
   * This value is used in the bootstrapping of the application.
   */
  val setup = DBIO.seq(users.schema.create,
    roles.schema.create,
    usersRoles.schema.create,
    specialties.schema.create,
    usersSpecialties.schema.create,
    appointments.schema.create,
    users ++= Seq(
      User(None, "monad", "$2a$04$/wjGVfUGKYXQM1NpiO8EqeXnEb74GFHH0iXmbLl9W2M/oG3avZVPu", "Stefanos", "Papadogiannis",
        "wheniturnintoamartian@gmail.com", None, enabled = true),
      User(None, "functor", "$2a$04$IuhmbSY0avRg2lQEaOSqjebkGyDAYq.1A2HQZLA0nkoQoz3a1wuwi", "Alexandra", "Charatsi",
        "fjslkdfjs@fjasdlf.com", None, enabled = true)),
    roles ++= Seq(
      Role(None, "Doctor"),
      Role(None, "Patient")),
    usersRoles ++= Seq(
      UserRole(None, 1, 1),
      UserRole(None, 2, 2)),
    specialties ++= Seq(
      Specialty(None, "Neurologist"),
      Specialty(None, "Cardiologist"),
      Specialty(None, "Psychiatrist")),
    usersSpecialties ++= Seq(
      UserSpecialty(None, 1, 1)),
    appointments ++= Seq(
      Appointment(None, 1, 2, new Timestamp(formatUTC.parse("2018-05-05T13:17:00.00Z").getTime),
        new Timestamp(formatUTC.parse("2018-05-05T13:27:00.00Z").getTime),
        new Timestamp(formatUTC.parse("2018-05-05T13:17:00.00Z").getTime), "lalala")
    )
  )

}