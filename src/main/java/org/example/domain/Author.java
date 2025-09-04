package org.example.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "authors",
        indexes = {
                @Index(name = "ax_authors_name", columnList = "name", unique = true)
        }
)
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "author_seq")
    @SequenceGenerator(name = "author_seq", sequenceName = "author_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    @Size(min = 3, max = 30)
    @Pattern(regexp = "^[A-Za-z0-9_]+$")
    private String name;

    @OneToMany(
            mappedBy = "author",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Article> articles = new ArrayList<>();

    @OneToMany(
            mappedBy = "author",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Product> products = new ArrayList<>();

    public void addArticle(Article article) {
        articles.add(article);
        article.setAuthor(this);
    }

    public void removeArticle(Article article) {
        articles.remove(article);
        article.setAuthor(null);
    }

    public void addProduct(Product product) {
        products.add(product);
        product.setAuthor(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setAuthor(null);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Author other = (Author) o;

        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "Author{id=" + id + ", name='" + name + "'}";
    }
}
