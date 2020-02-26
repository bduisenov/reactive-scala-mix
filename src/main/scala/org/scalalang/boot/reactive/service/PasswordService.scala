package org.scalalang.boot.reactive.service

import scala.util.Try

trait PasswordService {
  def encode(rawPassword: String): Try[String]
}

class PasswordServiceImpl extends PasswordService {
  override def encode(rawPassword: String): Try[String] =
    Try(rawPassword.reverse)
}
