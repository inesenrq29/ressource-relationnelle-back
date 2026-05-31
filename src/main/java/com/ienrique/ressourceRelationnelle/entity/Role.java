package com.ienrique.ressourceRelationnelle.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Role")
@Getter
@Setter
public class Role {

  @Id
  @GeneratedValue
  @Column(
      name = "roleId",
      nullable = false,
      updatable = false,
      length = 36,
      columnDefinition = "CHAR(36)")
  private UUID roleId;

  @Column(name = "roleName", nullable = false, unique = true, length = 50)
  private String roleName;
}
