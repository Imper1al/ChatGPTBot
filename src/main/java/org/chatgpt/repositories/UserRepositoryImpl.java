package org.chatgpt.repositories;

import org.chatgpt.entities.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {

    private final SessionFactory sessionFactory;

    public UserRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public User selectUserByChatId(String chatId) {
        System.out.println("Before Select User");
        try (Session session = sessionFactory.openSession()) {
            System.out.println("After session");
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            System.out.println("After builder");
            CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
            System.out.println("After query");
            Root<User> root = query.from(User.class);
            System.out.println("After root");
            query.select(root).where(criteriaBuilder.equal(root.get("chatId"), chatId));
            System.out.println("After Select User");
            return session.createQuery(query).uniqueResult();
        }
    }

    @Override
    public void saveUser(User user) {
        System.out.println("Before Save User");
        try (Session session = sessionFactory.openSession()) {
            System.out.println("Before transaction");
            session.beginTransaction();
            System.out.println("Before save");
            session.saveOrUpdate(user);
            System.out.println("Before commit");
            session.getTransaction().commit();
            System.out.println("After Save User");
        }
    }

    @Override
    public List<Long> selectAllChatIds() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
            Root<User> root = query.from(User.class);
            query.select(root.get("chatId"));

            return session.createQuery(query).getResultList();
        }
    }
}
