package pj.intermate.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import pj.intermate.entity.UserEntity

interface UserRepository : JpaRepository<UserEntity, Long> {
}