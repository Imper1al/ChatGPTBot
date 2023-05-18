package org.chatgpt.repositories;

import org.chatgpt.entities.User;

import java.util.List;

public interface UserRepository {

    User selectUserByChatId(String chatId);
    void saveUser(User user);
    List<Long> selectAllChatIds();
}
