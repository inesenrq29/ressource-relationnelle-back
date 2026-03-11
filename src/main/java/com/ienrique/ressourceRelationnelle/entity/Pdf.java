package com.ienrique.ressourceRelationnelle.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PDF")
@Getter
@Setter
public class Pdf {

  @Id
  @GeneratedValue
  @Column(name = "pdfId", nullable = false, updatable = false)
  private UUID pdfId;

  @Column(name = "pdfWeight", nullable = false)
  private Long pdfWeight;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resourceId", unique = true)
  private Resource resource;
}
