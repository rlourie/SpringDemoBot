package local.springdemobot.database.repository;

import local.springdemobot.database.entites.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
    @Query(nativeQuery = true, value = "SELECT * FROM bot.users_status WHERE user_id = :userId and status ='auth_done'")
    Optional<UserStatus> findAllByIdAndStatusAuthDone(@Param("userId") Long id);

    @Query(nativeQuery = true, value = "SELECT * FROM bot.users_status" +
            " WHERE user_id = :userId and status ='processing' and command ='/upload'")
    Optional<UserStatus> findByUserIdAndCommandUploadAndStatusProcessing(@Param("userId") Long id);

    @Query(nativeQuery = true, value = "SELECT * FROM bot.users_status" +
            " WHERE user_id = :userId and status ='processing' and command ='/delete'")
    Optional<UserStatus> findByUserIdAndCommandDeleteAndStatusProcessing(@Param("userId") Long id);

    @Query(nativeQuery = true, value = "SELECT * FROM bot.users_status" +
            " WHERE user_id = :userId and status ='processing' and command ='/delete@file_task_java_developer_bot'")
    Optional<UserStatus> findByUserIdAndCommandDeleteAdminAndStatusProcessing(@Param("userId") Long id);

    @Transactional
    void deleteByUserId(Long userId);
}
