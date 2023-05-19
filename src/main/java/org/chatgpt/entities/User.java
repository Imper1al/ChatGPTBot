package org.chatgpt.entities;

import lombok.*;
import org.hibernate.annotations.GeneratorType;

import javax.persistence.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", schema = "\"chat-gpt\"")
@SequenceGenerator(name = "user_sequence", sequenceName = "hibernate_sequence", allocationSize = 1)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "nickname")
    private String nickname;
    @Column(name = "chat_id")
    private String chatId;
    @Column(name = "lang")
    private String lang;
    @Column(name = "role")
    private String role;
}
