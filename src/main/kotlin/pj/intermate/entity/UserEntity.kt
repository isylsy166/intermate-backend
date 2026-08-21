package pj.intermate.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "users",
    uniqueConstraints = [UniqueConstraint(columnNames = ["provider","provider_id"])]
)
class UserEntity (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String,

    @Column(unique = true)
    val email: String,

    val password: String? = null,

    // 미지정 시 ORDINAL(0-based 정수)로 저장된다. 컬럼이 MySQL ENUM(1-based)이라 값이 한 칸씩 밀린다.
    @Enumerated(EnumType.STRING)
    val provider: Provider? = null,

    @Column(name = "provider_id")
    val providerId: String? = null,

    @Column(name = "profile_image_url")
    val profileImageUrl: String? = null,

): BaseEntity()

enum class Provider { LOCAL, GOOGLE, KAKAO, NAVER }