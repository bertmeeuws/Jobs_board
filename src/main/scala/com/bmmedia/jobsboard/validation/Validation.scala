package com.bmmedia.jobsboard.validation

import cats.data.*
import com.bmmedia.jobsboard.domain.job.*
import cats.data.Validated.*
import cats.*
import cats.syntax.all.*

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

  def isEmailValid(email: String): ValidationResult[String] =
    // Could be improved with regex
    if (email.contains("@")) email.validNel
    else EmailFormatIsIncorrect(s"$email is not a valid email").invalidNel

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
}
