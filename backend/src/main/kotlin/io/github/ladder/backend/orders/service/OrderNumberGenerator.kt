package io.github.ladder.backend.orders.service

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.time.LocalDate

// Short, human readable order references like BH-260917-K3F9QA
@Component
class OrderNumberGenerator {

    // no vowels and no look-alike characters, keeps it easy to read aloud
    private val alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    private val random = SecureRandom()

    fun next(): String {
        val date = LocalDate.now()
        val suffix = (1..6)
            .map { alphabet[random.nextInt(alphabet.length)] }
            .joinToString("")
        return "BH-%02d%02d%02d-%s".format(date.year % 100, date.monthValue, date.dayOfMonth, suffix)
    }
}
