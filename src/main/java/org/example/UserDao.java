package org.example;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserDao {
    void create(User user);

    void update(User user);

    void delete(Long id);

    Optional<User> findById(Long id);

    List<User> findAll();

    List<User> searchUsers(String usernamePart, int limit, int offset);

    void updateUserNames(Map<Long, String> idToUsernameMap);
}

