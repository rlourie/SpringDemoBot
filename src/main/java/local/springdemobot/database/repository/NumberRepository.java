package local.springdemobot.database.repository;

import local.springdemobot.database.entites.Numbers;
import local.springdemobot.database.entites.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NumberRepository extends JpaRepository<Numbers, Long> {
    Optional<Numbers> findByNumber(String number);
}
