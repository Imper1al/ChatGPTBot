package org.chatgpt.repositories;

import org.chatgpt.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User selectUserByChatId(String chatId);
}
