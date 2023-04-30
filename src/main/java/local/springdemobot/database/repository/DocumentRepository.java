package local.springdemobot.database.repository;

import local.springdemobot.database.entites.Document;
import local.springdemobot.database.entites.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {
    @Query(nativeQuery = true, value = "SELECT * FROM bot.document" +
            " WHERE user_id = :userId and name =:fileName")
    Optional<Document> findByUserIdAndFileName(@Param("userId") Long id, @Param("fileName") String name);

    @Query(nativeQuery = true, value = "SELECT * FROM bot.document WHERE user_id = :userId")
    List<Document> findByUserId(@Param("userId") Long id);
}
