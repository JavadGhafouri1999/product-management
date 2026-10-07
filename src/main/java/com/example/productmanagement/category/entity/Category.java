package com.example.productmanagement.category.entity;

import jakarta.persistence.*;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor
public class Category {

  @Id
  @Generated
  @ColumnDefault("gen_random_uuid()")
  @Column(nullable = false)
  private UUID id;

  @Setter
  @Column(nullable = false, length = 100)
  private String name;

  // Meaning : This category has one parent. Find it by looking at my own parent_id column.
  // ManyToOne - Because many categories can point to the one same parent.
  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  // "Computers" and "Phones" both point to "Electronics". Many → One.
  @JoinColumn(name = "parent_id")
  private Category parent;

  // Meaning : This category has many children. Find them by looking at other rows whose parent_id points to me.
  // OneToMany - Because one parent can have many children. One → Many.
  // Without mappedBy, JPA would think @OneToMany needs its own join table or foreign key column.
  @OneToMany(mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL)
  private Set<Category> children = new HashSet<>();

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  // ┌──────────────────────────────────────────────────────────┐
  // │                       Helper Class                       │
  // └──────────────────────────────────────────────────────────┘
  // The key word "this" pointing to the current Parent Category that wants to add/remove a child category.
  public void addChild(Category child) {
	children.add(child);
	child.setParent(this);
  }

  public void removeChild(Category child) {
	children.remove(child);
	child.setParent(null);
  }
}
