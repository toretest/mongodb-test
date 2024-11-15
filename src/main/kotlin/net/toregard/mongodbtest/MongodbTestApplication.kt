package net.toregard.mongodbtest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MongodbTestApplication

/**
 * Entry point for the MongodbTestApplication.
 *
 * @param args Command-line arguments passed to the application.
 */
fun main(args: Array<String>) {
    runApplication<MongodbTestApplication>(*args)
}
