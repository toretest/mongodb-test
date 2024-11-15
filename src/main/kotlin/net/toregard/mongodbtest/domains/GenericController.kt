package net.toregard.mongodbtest.domains

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.flipkart.zjsonpatch.JsonDiff
import kotlinx.coroutines.flow.Flow
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * This code is so general at it can be use for all collection and crud operation.
 * Bringing a schema as an input can extend the functionality to also validate on the schema
 *
 * Notice that the code use reactive programming and coroutines to handle the async operations.
 * Async mongodb is active here-  Flux/mono is replaced with coroutine..but it does the same
 */
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

    @PostMapping("/{id}")
    suspend fun createOrUpdate(
        @PathVariable collectionName: String,
        @PathVariable id: String,
        @RequestBody document: Map<String, Any>
    ): ResponseEntity<Any> {
        logger.info("Received request to create/update document with id '$id' in collection '$collectionName'")
        return try {
            val schemaJson = getSchemaForCollection(collectionName)
            val errors = schemaValidationService.validate(document, schemaJson)
            if (errors.isNotEmpty()) {
                logger.warn("Validation errors for document with id '$id': $errors")
                return ResponseEntity.badRequest().body(mapOf("errors" to errors))
            }

            val doc = Document(document).append("_id", id)

            val existingDoc = repository.findById(collectionName, id)
            if (existingDoc != null) {
                // Existing document found, compute and log the diff
                val diff = computeDiff(existingDoc, doc)
                logDiff(id, diff)
                // Update the document
                val savedDoc = repository.save(collectionName, doc)
                logger.info("Updated document with id '$id' in collection '$collectionName'")
                ResponseEntity.ok(savedDoc)
            } else {
                // Document does not exist, create new one
                val savedDoc = repository.save(collectionName, doc)
                logger.info("Created new document with id '$id' in collection '$collectionName'")
                ResponseEntity.ok(savedDoc)
            }
        } catch (e: Exception) {
            logger.error("Exception in createOrUpdate method for id '$id' in collection '$collectionName': ${e.message}", e)
            ResponseEntity.status(500).body(mapOf("error" to "Internal Server Error"))
        }
    }

    private fun computeDiff(existingDoc: Document, newDoc: Document): JsonNode {
        return try {
            val existingJsonNode = objectMapper.readTree(existingDoc.toJson())
            val newJsonNode = objectMapper.readTree(newDoc.toJson())
            JsonDiff.asJson(existingJsonNode, newJsonNode)
        } catch (e: Exception) {
            logger.error("Error computing diff: ${e.message}", e)
            objectMapper.createObjectNode()
        }
    }

    private fun logDiff(id: String, diff: JsonNode) {
        logger.info("Differences for document with id '$id': $diff")
    }

    @GetMapping("/{id}")
    suspend fun getById(
        @PathVariable collectionName: String,
        @PathVariable id: String
    ): ResponseEntity<Document> {
        logger.info("Received request to get document with id '$id' from collection '$collectionName'")
        return try {
            val doc = repository.findById(collectionName, id)
            if (doc != null) {
                logger.info("Found document with id '$id' in collection '$collectionName'")
                ResponseEntity.ok(doc)
            } else {
                logger.warn("Document with id '$id' not found in collection '$collectionName'")
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error retrieving document with id '$id' from collection '$collectionName': ${e.message}", e)
            ResponseEntity.status(500).build()
        }
    }

    @GetMapping
    fun getAll(@PathVariable collectionName: String): Flow<Document> {
        logger.info("Received request to get all documents from collection '$collectionName'")
        return repository.findAll(collectionName)
    }

    @DeleteMapping("/{id}")
    suspend fun delete(
        @PathVariable collectionName: String,
        @PathVariable id: String
    ): ResponseEntity<Void> {
        logger.info("Received request to delete document with id '$id' from collection '$collectionName'")
        return try {
            repository.deleteById(collectionName, id)
            logger.info("Deleted document with id '$id' from collection '$collectionName'")
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            logger.error("Error deleting document with id '$id' from collection '$collectionName': ${e.message}", e)
            ResponseEntity.status(500).build()
        }
    }
}
