package org.chatgpt.repositories;

import org.chatgpt.entities.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Collectors;

public class UserRepositoryImpl implements UserRepository {

    private final SessionFactory sessionFactory;

    public UserRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public User selectUserByChatId(String chatId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
            Root<User> root = query.from(User.class);
            query.select(root).where(criteriaBuilder.equal(root.get("chatId"), chatId));
            return session.createQuery(query).uniqueResult();
        }
    }

    @Override
    public void saveUser(User user) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(user);
            session.getTransaction().commit();
        }
    }

    @Override
    public void deleteUser(String chatId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaDelete<User> deleteQuery = criteriaBuilder.createCriteriaDelete(User.class);
            Root<User> root = deleteQuery.from(User.class);
            deleteQuery.where(criteriaBuilder.equal(root.get("chatId"), chatId));
            Transaction transaction = session.beginTransaction();
            session.createQuery(deleteQuery).executeUpdate();
            transaction.commit();
        }
    }

    @Override
    public List<Long> selectAllChatIds() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);
            Root<User> root = query.from(User.class);
            query.select(root.get("chatId"));

            List<String> chatIds = session.createQuery(query).getResultList();

            return chatIds.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
    }
}
