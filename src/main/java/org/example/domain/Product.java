package org.example.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "product_seq", allocationSize = 50)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 32)
    @NotBlank
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    @NotNull
    private String type;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_products_author"))
    private Author author;

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getType() {
        return type;
    }

    public Author getAuthor() {
        return author;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(getId(), product.getId()) && Objects.equals(getAccountNumber(), product.getAccountNumber()) && Objects.equals(getType(), product.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAccountNumber(), getType());
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

