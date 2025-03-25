package com.dario.ast.repository;

import javax.sql.DataSource;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.stereotype.Component;

@Component
public class CustomJdbcTokenRepository extends JdbcTokenRepositoryImpl {

  public CustomJdbcTokenRepository(DataSource dataSource) {
    setDataSource(dataSource);
  }

  @Override
  public void createNewToken(PersistentRememberMeToken token) {
    var rememberMeToken = new PersistentRememberMeToken(
        token.getUsername(),
        token.getSeries(),
        token.getTokenValue(),
        token.getDate()
    );
    super.createNewToken(rememberMeToken);
  }

}
