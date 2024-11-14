package net.toregard.mongodbtest.domains

import com.fasterxml.jackson.databind.JsonNode
import org.bson.Document
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

    @PostMapping("/{id}")
    fun createOrUpdate(
        @PathVariable collectionName: String,
        @PathVariable id: String,
        @RequestBody document: Map<String, Any>
    ): Mono<out ResponseEntity<out Map<String, Any>>> {
        val schemaJson = getSchemaForCollection(collectionName)
        val errors = schemaValidationService.validate(document, schemaJson)
        if (errors.isNotEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(mapOf("errors" to errors)))
        }

        val doc = Document(document).append("_id", id)

        return repository.findById(collectionName, id)
            .flatMap { existingDoc ->
                // Existing document found, compute and log the diff
                val diff = ComputeDiff.computeDiff(existingDoc, doc)
                logDiff(id, diff)
                // Update the document
                repository.save(collectionName, doc)
                    .map { ResponseEntity.ok(it) }
            }
            .switchIfEmpty(
                // Document does not exist, create new one
                repository.save(collectionName, doc)
                    .map { ResponseEntity.ok(it) }
            )
    }

    fun logDiff(id: String, diff: JsonNode) {
        // You can use your preferred logging framework; here we use println for simplicity
        println("Differences for document with id '$id': $diff")
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

    @PutMapping("/{id}")
    fun update(
        @PathVariable collectionName: String,
        @PathVariable id: String,
        @RequestBody document: Map<String, Any>
    ): Mono<ResponseEntity<Document>> {
        val doc = Document(document).append("_id", id)
        return repository.save(collectionName, doc)
            .map { ResponseEntity.ok(it) }
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable collectionName: String,
        @PathVariable id: String
    ): Mono<ResponseEntity<Void>> =
        repository.deleteById(collectionName, id)
            .then(Mono.just(ResponseEntity.noContent().build()))
}

