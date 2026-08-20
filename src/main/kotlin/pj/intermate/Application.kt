package pj.intermate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class IntermateApplication

fun main(args: Array<String>) {
    runApplication<IntermateApplication>(*args)
}
