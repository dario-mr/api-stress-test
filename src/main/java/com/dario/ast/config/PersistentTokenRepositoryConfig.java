package com.dario.ast.config;

import com.dario.ast.repository.CustomJdbcTokenRepository;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@Configuration
public class PersistentTokenRepositoryConfig {

  @Bean
  public PersistentTokenRepository persistentTokenRepository(@Autowired DataSource dataSource) {
    var tokenRepository = new CustomJdbcTokenRepository(dataSource);
    tokenRepository.setDataSource(dataSource);

    try (var conn = dataSource.getConnection();
        var statement = conn.createStatement()) {
      statement.execute("SET SCHEMA 'MY_SCHEMA'");
    } catch (SQLException e) {
      throw new RuntimeException("Failed to set schema for persistent_logins", e);
    }

    return tokenRepository;
  }

}
