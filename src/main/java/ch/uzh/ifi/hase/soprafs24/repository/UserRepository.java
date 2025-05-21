package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {
  User findByUsername(String username);
  User findByToken(String token);
  Optional<User> findById(@NonNull Long id);
  @Query(value = "SELECT u FROM User u WHERE u.id != :userId ")
  List<User> getAllUsersExceptSelf(@Param("userId") long userId);
}
