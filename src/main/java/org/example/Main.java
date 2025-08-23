package org.example;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public class Main {
    public static void main(String[] args) {
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