package org.example.bootstrap;

import org.example.domain.Article;
import org.example.domain.Author;
import org.example.domain.Product;
import org.example.repository.AuthorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DataLoader implements CommandLineRunner {

    private final AuthorRepository authors;

    public DataLoader(AuthorRepository authors) {
        this.authors = authors;
    }

    @Override
    @Transactional
    public void run(String[] args) {
        Author author1 = new Author();
        Author author2 = new Author();

        author1.setName("Fedor");
        author2.setName("Petr");

        Article article1 = new Article();
        Article article2 = new Article();
        Article article3 = new Article();
        Article article4 = new Article();
        Article article5 = new Article();

        article1.setContent("1 статья автора1 - Fedor");
        article2.setContent("2 статья автора1 - Fedor");
        article3.setContent("3 статья автора1 - Fedor");
        article4.setContent("4 статья автора2 - Petr");
        article5.setContent("5 статья автора2 - Petr");

        author1.addArticle(article1);
        author1.addArticle(article2);
        author1.addArticle(article3);

        author2.addArticle(article4);
        author2.addArticle(article5);

        Product product1 = new Product();
        Product product2 = new Product();
        Product product3 = new Product();

        product1.setAccountNumber("ACC-001");
        product1.setBalance(new BigDecimal("1234.56"));
        product1.setType("ACCOUNT");

        product2.setAccountNumber("CARD-4111");
        product2.setBalance(new BigDecimal("99.99"));
        product2.setType("CARD");

        product3.setAccountNumber("CARD-002");
        product3.setBalance(new BigDecimal("7890.11"));
        product3.setType("CARD");

        author1.addProduct(product1);
        author1.addProduct(product2);

        author2.addProduct(product3);

        authors.save(author1);
        authors.save(author2);

        authors.findByName("Fedor").ifPresent(loaded -> {
            System.out.println("Loaded author: " + loaded);
            System.out.println("Notes count: " + loaded.getArticles().size());
            System.out.println("Products count: " + loaded.getProducts().size());
        });

        authors.findByName("Petr").ifPresent(loaded -> {
            System.out.println("Loaded author: " + loaded);
            System.out.println("Notes count: " + loaded.getArticles().size());
            System.out.println("Products count: " + loaded.getProducts().size());
        });
    }
}

