package zuk.utils

import javax.mail._
import javax.mail.internet._
import java.util.Properties

object SendMail {

  private val SMTP_HOST = "smtp.qq.com"
  private val SMTP_PORT = 465 // SSL端口

  private val SMTP_PROTOCOL = "smtps" // SSL协议

  def sendSimpleEmail(senderEmail: String, authCode: String, receiverEmail: String, subject: String, content: String): Boolean = {
    // 配置邮件服务器属性
    val props = new Properties()
    props.put("mail.smtp.host", SMTP_HOST)
    props.put("mail.smtp.port", SMTP_PORT)
    props.put("mail.smtp.auth", "true") // 需要认证

    props.put("mail.smtp.ssl.enable", "true") // 启用SSL

    props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3") // SSL协议版本

    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
    props.put("mail.smtp.socketFactory.port", String.valueOf(SMTP_PORT))
    props.put("mail.smtp.starttls.enable", "true") // 使用STARTTLS

    // 创建认证器
    val authenticator = new Authenticator() {
      override def getPasswordAuthentication: PasswordAuthentication = {
        new PasswordAuthentication(senderEmail, authCode)
      }
    }
    // 创建邮件会话
    val session = Session.getInstance(props, authenticator)
    session.setDebug(true) // 启用调试模式

    try {
      // 创建邮件
      val message = new MimeMessage(session)
      // 设置发件人
      message.setFrom(new InternetAddress(senderEmail))
      // 设置收件人
      message.setRecipient(Message.RecipientType.TO, new InternetAddress(receiverEmail))
      // 设置邮件主题
      message.setSubject(subject)
      // 设置邮件内容
      message.setText(content)
      // 发送邮件
      Transport.send(message)
      println("邮件发送成功！")
      true
    } catch {
      case e: Exception =>
        println("邮件发送失败: " + e.getMessage)
        e.printStackTrace
        false
    }
  }

  def main(args: Array[String]): Unit = {
    // 测试发送邮件
    val senderEmail = "513283439@qq.com" // 发件人QQ邮箱

    val authCode = "xutobxzlvwisbigc" // 授权码

    val receiverEmail = "513283439@qq.com" // 收件人邮箱

    val subject = "Java SMTP 测试邮件"
    val content = "这是一封通过Java发送的测试邮件！"

    val success = sendSimpleEmail(senderEmail, authCode, receiverEmail, subject, content)
    if (success) {
      System.out.println("邮件发送成功！")
    }
    else {
      System.out.println("邮件发送失败！")
    }
  }

}
