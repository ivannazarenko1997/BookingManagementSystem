package com.example.bookstore.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;


@Data
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "username") // or delete this line
)public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String username;

  @Column(nullable = false, length = 100)
  private String passwordHash;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(length = 255)
  private String description;



}
