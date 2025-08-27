package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        EntityManager em = JpaUtil.emf().createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

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

            em.persist(author1);
            em.persist(author2);

            em.flush();

            tx.commit();

            em.clear();

            tx.begin();

            Author loadedAuthor1 = em.createQuery(
                            "select a from Author a where a.name = :an", Author.class)
                    .setParameter("an", "Fedor")
                    .getSingleResult();

            Author loadedAuthor2 = em.createQuery(
                            "select a from Author a where a.name = :an", Author.class)
                    .setParameter("an", "Petr")
                    .getSingleResult();

            System.out.println("Loaded first author id=" + loadedAuthor1.getId() + ", name=" + loadedAuthor1.getName());
            System.out.println("Loaded second author id=" + loadedAuthor2.getId() + ", name=" + loadedAuthor2.getName());

            int articlesCountOfFirstAuthor = loadedAuthor1.getArticles().size();
            int articlesCountOfFSecondAuthor = loadedAuthor2.getArticles().size();

            System.out.println("Articles count of first author = " + articlesCountOfFirstAuthor);
            System.out.println("Articles count of second author = " + articlesCountOfFSecondAuthor);

            loadedAuthor1.removeArticle(article2);

            em.remove(loadedAuthor2);

            em.flush();
            tx.commit();

            em.clear();
        } finally {
            em.close();
            JpaUtil.close();
        }
    }

    private void checkHW4() {
        var context = new AnnotationConfigApplicationContext(Configurer.class);
        var dataSource = context.getBean(javax.sql.DataSource.class);

        var userDao = new UserDaoImpl(dataSource);
        var userService = new UserServiceImpl(userDao);


        userService.registerUser("1user1");

        List<User> users = userService.getAllUsers();
        users.forEach(System.out::println);

        if (!users.isEmpty()) {
            User firstUser = users.get(0);
            userService.renameUser(firstUser.getId(), "2user2");

            System.out.println("✅ После переименования:");

            userService.getAllUsers().forEach(System.out::println);
        }

        if (!users.isEmpty()) {
            User firstUser = users.get(0);
            userService.deleteUser(firstUser.getId());

            System.out.println("✅ После удаления:");

            userService.getAllUsers().forEach(System.out::println);
        }
    }
}