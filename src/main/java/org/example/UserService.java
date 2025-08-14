package org.example;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    void registerUser(String userName);

    void renameUser(Long id, String newUserName);

    void deleteUser(Long id);

    void updateUserNames(Map<Long, String> idToUsernameMap);

    Optional<User> getUser(Long id);

    List<User> getAllUsers();

    List<User> searchUsers(String usernamePart, int limit, int offset);
}

