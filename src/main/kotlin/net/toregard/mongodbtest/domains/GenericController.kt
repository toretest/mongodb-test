package net.toregard.mongodbtest.domains

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.flipkart.zjsonpatch.JsonDiff
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/{collectionName}")
class GenericController(
    private val repository: GenericRepository,
    private val schemaValidationService: SchemaValidationService
) {

    private val logger = LoggerFactory.getLogger(GenericController::class.java)
    private val objectMapper = ObjectMapper()

    // Method to retrieve the schema for a collection
    private fun getSchemaForCollection(collectionName: String): String {
        // Retrieve the schema from your storage (database, file, etc.)
        // For demonstration, returning a hardcoded schema
        return """
        {
          "${'$'}schema": "http://json-schema.org/draft-07/schema#",
          "type": "object",
          "properties": {
            "_id": { "type": "string" },
            "firstName": { "type": "string" },
            "lastName": { "type": "string" },
            "email": { "type": "string", "format": "email" },
            "age": { "type": "integer" }
          },
          "required": ["firstName", "lastName", "email"]
        }
        """
    }

    @PutMapping("/{id}")
    fun createOrUpdate(
        @PathVariable collectionName: String,
        @PathVariable id: String,
        @RequestBody document: Map<String, Any>
    ): Mono<ResponseEntity<Any>> {
        val schemaJson = getSchemaForCollection(collectionName)
        val errors = schemaValidationService.validate(document, schemaJson)
        if (errors.isNotEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(mapOf("errors" to errors)))
        }

        val doc = Document(document).append("_id", id)

        return repository.findById(collectionName, id)
            .flatMap { existingDoc ->
                // Existing document found, compute and log the diff
                val diff = computeDiff(existingDoc, doc)
                logDiff(id, diff)
                // Update the document
                repository.save(collectionName, doc)
                    .map { ResponseEntity.ok(it as Any) }
            }
            .switchIfEmpty(
                // Document does not exist, create new one
                repository.save(collectionName, doc)
                    .map { ResponseEntity.ok(it as Any) }
            )
    }

    private fun computeDiff(existingDoc: Document, newDoc: Document): JsonNode {
        return try {
            val existingJsonNode = objectMapper.readTree(existingDoc.toJson())
            val newJsonNode = objectMapper.readTree(newDoc.toJson())
            JsonDiff.asJson(existingJsonNode, newJsonNode)
        } catch (e: Exception) {
            logger.error("Error computing diff: ${e.message}")
            objectMapper.createObjectNode()
        }
    }

    private fun logDiff(id: String, diff: JsonNode) {
        logger.info("Differences for document with id '$id': $diff")
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable collectionName: String,
        @PathVariable id: String
    ): Mono<ResponseEntity<Document>> =
        repository.findById(collectionName, id)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())

    @GetMapping
    fun getAll(@PathVariable collectionName: String): Flux<Document> =
        repository.findAll(collectionName)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable collectionName: String,
        @PathVariable id: String
    ): Mono<ResponseEntity<Void>> =
        repository.deleteById(collectionName, id)
            .then(Mono.just(ResponseEntity.noContent().build()))
}
