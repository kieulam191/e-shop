package com.dev.e_shop.user.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("SELECT p.address, p.phone FROM Profile p JOIN User u ON p.userId = u.id WHERE u.email = :email")
    Optional<Profile> findByEmail(@Param("email") String email);

    Optional<Profile> findByUserId(long UserId);
}
