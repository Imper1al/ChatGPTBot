package org.chatgpt.entities;

import lombok.*;

import javax.persistence.Table;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", schema = "chat-gpt")
public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String nickname;
    private String chatId;
}
