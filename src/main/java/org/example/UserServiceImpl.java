package org.example;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void registerUser(String username) throws ValidationException {
        validateUsername(username);

        User user = new User();
        user.setUserName(username);

        userDao.create(user);

        System.out.println("User created: " + user.getUserName());
    }

    @Override
    public void renameUser(Long id, String newUserName) throws ValidationException {
        validateUsername(newUserName);
        Optional<User> optionalUser = userDao.findById(id);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUserName(newUserName);

            userDao.update(user);
            System.out.println("User updated: " + user.getUserName());
        }
    }

    @Override
    @Transactional
    public void updateUserNames(Map<Long, String> idToUserNameMap) {
        for (String userName : idToUserNameMap.values()) {
            validateUsername(userName);
        }

        userDao.updateUserNames(idToUserNameMap);
        System.out.println("UserNames updated in bulk: " + idToUserNameMap.size() + " users");
    }

    @Override
    public void deleteUser(Long id) {
        userDao.delete(id);

        System.out.println("User with id: " + id + " is removed");
    }

    @Override
    public Optional<User> getUser(Long id) {
        return userDao.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public List<User> searchUsers(String usernamePart, int limit, int offset) {
        return userDao.searchUsers(usernamePart, limit, offset);
    }

    private void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new ValidationException("Имя пользователя не должно быть пустым.");
        }

        if (username.length() < 3 || username.length() > 30) {
            throw new ValidationException("Имя пользователя должно быть не менее 3 символов и не более 30 символов.");
        }

        if (!Pattern.matches("^[a-zA-Z0-9_]+$", username)) {
            throw new ValidationException("Имя пользователя может содержать только буквы, цифры и подчеркивания.");
        }
    }
}
