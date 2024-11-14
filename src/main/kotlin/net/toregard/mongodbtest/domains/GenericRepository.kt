package net.toregard.mongodbtest.domains

import org.bson.Document
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class GenericRepository(private val mongoTemplate: ReactiveMongoTemplate) {

    fun save(collectionName: String, document: Document): Mono<Document> =
        mongoTemplate.save(document, collectionName)

    fun findById(collectionName: String, id: String): Mono<Document> =
        mongoTemplate.findById(id, Document::class.java, collectionName)

    fun findAll(collectionName: String): Flux<Document> =
        mongoTemplate.findAll(Document::class.java, collectionName)

    fun deleteById(collectionName: String, id: String): Mono<Void> =
        mongoTemplate.remove(Document("_id", id), collectionName).then()
}

