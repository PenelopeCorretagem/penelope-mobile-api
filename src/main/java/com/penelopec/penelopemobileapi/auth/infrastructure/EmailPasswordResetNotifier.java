package com.penelopec.penelopemobileapi.auth.infrastructure;

import com.penelopec.penelopemobileapi.auth.domain.PasswordResetNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailPasswordResetNotifier implements PasswordResetNotifier {
  private final JavaMailSender mailSender;

  @Value("${app.frontend.url}")
  private String frontendUrl;

  @Override
  public void send(String email, String token) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setSubject("Redefinição de senha");
    message.setText("Código de verificação: " + token + "\nAcesse: "
      + frontendUrl + "/verificacao?token=" + token);
    mailSender.send(message);
  }
}
