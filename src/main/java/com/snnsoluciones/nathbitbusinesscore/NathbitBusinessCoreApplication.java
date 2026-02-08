package com.snnsoluciones.nathbitbusinesscore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.snnsoluciones.nathbitbusinesscore.repository")
@EnableMongoRepositories(basePackages = "com.snnsoluciones.nathbitbusinesscore.audit.repository")
public class NathbitBusinessCoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(NathbitBusinessCoreApplication.class, args);
  }

}