package local.springdemobot.database.repository;

import local.springdemobot.database.entites.User;
import local.springdemobot.database.entites.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
    @Query(nativeQuery = true, value = "SELECT * FROM bot.users_status WHERE user_id = :userId and status ='auth_done'")
    UserStatus findAllByIdAndStatusAuthDone(@Param("userId") Long id);
}
