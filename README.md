# POC


## Installation of local MongoDB

```bash
docker run --name mongodb -d -p 27017:27017 -v $(pwd)/data:/data/db mongodb/mongodb-community-server:latest
```

## POC shows
- Generic crud operation where post creat and update in same methode. Validate against json schema
- diff is logged if update. Example differences for document with id '1012_15-11-25':
```json
 [{"op":"replace","path":"/lastName","value":"HelloA"},{"op":"replace","path":"/age","value":29}]
```

- reactive mongo db
- schema validation
- using coroutine with reactive mongo db, and coroutine simply code than Flux and Mongo


## How Coroutines Work with Asynchronous MongoDB
Kotlin coroutines will function with asynchronous MongoDB operations. By integrating coroutines with the reactive 
MongoDB driver provided by Spring Data, you can write asynchronous, non-blocking code in a more sequential 
and readable manner.

Spring Data provides the ReactiveMongoTemplate class for interacting with MongoDB in a reactive, non-blocking way. 
The operations return Flux and Mono types from Project Reactor. Kotlin coroutines can interoperate with these reactive 
types using the kotlinx-coroutines-reactor library, which provides extensions like awaitFirst() and awaitFirstOrNull().
By using these extensions, you can convert reactive streams into coroutine-friendly code. This allows you to use suspend 
functions and write code that looks sequential but executes asynchronously.

## Benefits of Using Coroutines with Asynchronous MongoDB
Improved Readability: Coroutines let you write asynchronous code that looks and feels like synchronous code, making it 
easier to read and maintain.
Simplified Error Handling: You can use standard try-catch blocks for exception handling, which can be more intuitive 
than reactive error handling operators.
Seamless Integration: The kotlinx-coroutines-reactor library bridges the gap between Reactor's types and Kotlin 
coroutines, allowing for smooth interoperability.
Structured Concurrency: Coroutines provide structured concurrency, which can help manage the lifecycle of 
asynchronous operations more effectively.


## examples

### Schema example
```json
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
```

### Post (inser and update...yes no put!) :
```bash
curl -X POST -H "Content-Type: application/json" -d '{"name":"John Doe", "age": 25}' http://localhost:8080/api/persons
```

### list : 
```bash
curl http://localhost:8080/api/persons
```
