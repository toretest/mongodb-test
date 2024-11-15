package net.toregard.mongodbtest.domains

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.Document
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria

/**
 * GenericRepository provides methods to interact with MongoDB collections in a generic way using a dynamic
 * collection name. This repository uses a ReactiveMongoTemplate for non-blocking database operations.
 *
 * @param mongoTemplate The Spring ReactiveMongoTemplate used for performing database operations.
 */
@Repository
class GenericRepository(private val mongoTemplate: ReactiveMongoTemplate) {

    suspend fun save(collectionName: String, document: Document): Document =
        mongoTemplate.save(document, collectionName).awaitFirst()

    suspend fun findById(collectionName: String, id: String): Document? =
        mongoTemplate.findById(id, Document::class.java, collectionName).awaitFirstOrNull()

    /**
     * Stream with flow
     */
    fun findAll(collectionName: String): Flow<Document> =
        mongoTemplate.findAll(Document::class.java, collectionName).asFlow()

    suspend fun deleteById(collectionName: String, id: String) {
        mongoTemplate.remove(
            Query.query(Criteria.where("_id").`is`(id)),
            collectionName
        ).awaitFirstOrNull()
    }
}

