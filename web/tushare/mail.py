import smtplib
from email.mime.text import MIMEText

def send_mail():

    # socks.set_default_proxy(socks.SOCKS5, "127.0.0.1", 10808)
    # socket.socket = socks.socksocket

    port = 465  # SSL端口
    # 邮箱账号与授权码
    sender = "xxx@qq.com"
    auth_code = "xxxxx"

    # 收件人
    receiver = "xxx@qq.com"

    # 构造邮件内容（纯文本）
    msg = MIMEText("内容", "plain", "utf-8")
    msg["Subject"] = "推荐"
    msg["From"] = sender
    msg["To"] = receiver

    # QQ 邮箱 SMTP 服务器
    smtp_server = "smtp.qq.com"

    server = smtplib.SMTP_SSL(smtp_server, port)
    server.login(sender, auth_code)
    server.sendmail(sender, receiver, msg.as_string())

    print("邮件发送成功!")


if __name__ == '__main__':
    send_mail()