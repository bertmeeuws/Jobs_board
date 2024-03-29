package com.bmmedia.jobsboard.validation

import cats.data.*
import cats.data.Validated.*
import cats.*
import cats.syntax.all.*
import com.bmmedia.jobsboard.domain.auth.*
import com.bmmedia.jobsboard.domain.job.*
import com.bmmedia.jobsboard.domain.user.*

object Validation {

  // Create a type alias for the validation result
  type ValidationResult[A] = ValidatedNel[ValidationError, A]

  // Create a type class for validation
  sealed trait ValidationError(val errorMessage: String)
  case class UrlFormatIsNotCorrect(val message: String)  extends ValidationError(message)
  case class FieldRequired(val message: String)          extends ValidationError(message)
  case class EmailFormatIsIncorrect(val message: String) extends ValidationError(message)

  // Create a type class for validation
  trait Validator[A] {
    def validate(a: A): ValidationResult[A]
  }

  // Create a type class instance for JobInfo
  def validationRequired[A](
      field: A
  )(required: A => Boolean): ValidationResult[A] =
    if (required(field)) field.validNel
    else FieldRequired(s"$field is required").invalidNel

  def validationUrl[A](field: String)(url: String => Boolean): ValidationResult[String] =
    if (url(field)) field.validNel
    else UrlFormatIsNotCorrect(s"$field is not a valid url").invalidNel

    // Todo - improve this
  def isEmailValid(email: String): ValidationResult[String] =
    // Could be improved with regex
    if (email.contains("@")) email.validNel
    else EmailFormatIsIncorrect(s"$email is not a valid email").invalidNel

    // Todo - improve this, turn into opaque type
  def validatePassword(password: String): ValidationResult[String] =
    if (password.length > 8) password.validNel
    else FieldRequired(s"$password is not a valid password").invalidNel

  given jobInfoValidator: Validator[JobInfo] = new Validator[JobInfo] {
    def validate(jobInfo: JobInfo): ValidationResult[JobInfo] = {
      val JobInfo(
        company,
        title,
        description,
        externalUrl,
        salaryLo,
        salaryHi,
        currency,
        remote,
        location,
        country,
        tags,
        image,
        seniority,
        other
      ) = jobInfo

      val validatedCompany = validationRequired(company)(_.nonEmpty)
      val validatedTitle   = validationRequired(title)(_.nonEmpty)
      val validatedDescription =
        validationRequired(description)(_.nonEmpty)

      (
        validatedCompany,
        validatedTitle,
        validatedDescription,
        externalUrl.validNel,
        salaryLo.validNel,
        salaryHi.validNel,
        currency.validNel,
        remote.validNel,
        location.validNel,
        country.validNel,
        tags.validNel,
        image.validNel,
        seniority.validNel,
        other.validNel
      ).mapN(JobInfo.apply)
    }
  }

  given userValidator: Validator[User] = new Validator[User] {

    def validate(user: User): ValidationResult[User] = {
      val User(
        email,
        firstName,
        lastName,
        password,
        role,
        company
      ) = user

      val validatedFirstname = validationRequired(firstName)(_.nonEmpty)
      val validatedLastName  = validationRequired(lastName)(_.nonEmpty)
      val validatedEmail     = isEmailValid(email)
      val validatedPassword  = validatePassword(password)

      (
        validatedEmail,
        validatedFirstname,
        validatedLastName,
        validatedPassword,
        role.validNel,
        company.validNel
      ).mapN(User.apply)
    }
  }

  given userRegisterValidator: Validator[UserRegister] = new Validator[UserRegister] {

    def validate(user: UserRegister): ValidationResult[UserRegister] = {
      val UserRegister(
        email,
        firstName,
        lastName,
        password,
        company,
        url
      ) = user

      val validatedFirstname = validationRequired(firstName)(_.nonEmpty)
      val validatedLastName  = validationRequired(lastName)(_.nonEmpty)
      val validatedEmail     = isEmailValid(email)
      val validatedPassword  = validatePassword(password)

      (
        validatedEmail,
        validatedFirstname,
        validatedLastName,
        validatedPassword,
        company.validNel,
        url.validNel
      ).mapN(UserRegister.apply)
    }
  }

  given credentialsValidator: Validator[Credentials] = new Validator[Credentials] {

    def validate(credentials: Credentials): ValidationResult[Credentials] = {
      val Credentials(email, password) = credentials

      val validatedEmail = isEmailValid(email)

      (
        validatedEmail,
        password.validNel
      ).mapN(Credentials.apply)
    }
  }

  given passwordChangeValidator: Validator[PasswordChange] = new Validator[PasswordChange] {

    def validate(passwordChange: PasswordChange): ValidationResult[PasswordChange] = {
      val PasswordChange(oldPassword, newPassword) = passwordChange

      (
        oldPassword.validNel,
        newPassword.validNel
      ).mapN(PasswordChange.apply)
    }
  }

}
