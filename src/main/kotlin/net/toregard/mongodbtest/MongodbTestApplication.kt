package net.toregard.mongodbtest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MongodbTestApplication

fun main(args: Array<String>) {
    runApplication<MongodbTestApplication>(*args)
}
