package com.example.bookstore.repository;

import com.example.bookstore.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @Query(
            value = """
                    SELECT r.* 
                    FROM user_to_role ur
                    JOIN role r ON ur.role_id = r.id
                    WHERE ur.user_id = :userId
                    """,
            nativeQuery = true
    )
    List<Role> findRoleNamesByUserId(@Param("userId") Long userId);
}
