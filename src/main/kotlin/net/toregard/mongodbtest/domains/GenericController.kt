package net.toregard.mongodbtest.domains

import org.bson.Document
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/{collectionName}")
class GenericController(private val repository: GenericRepository) {

    @PostMapping
    fun create(
        @PathVariable collectionName: String,
        @RequestBody document: Map<String, Any>
    ): Mono<ResponseEntity<Document>> {
        val doc = Document(document)
        return repository.save(collectionName, doc)
            .map { ResponseEntity.ok(it) }
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

