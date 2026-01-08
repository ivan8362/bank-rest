package com.example.bankcards.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bankcards.entity.UserInfo;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long>  {

    Optional<UserInfo> findByUsername(String username);
}
