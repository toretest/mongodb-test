package net.toregard.mongodbtest.domains

/*import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonSubTypes

@Repository
interface GenericEntityRepository : ReactiveMongoRepository<GenericEntity, String>

@Document(collection = "generic_entities")
data class GenericEntity(
    @Id val id: String? = null,
    @Field("data") val data: BaseData
)



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PersonData::class, name = "PersonData"),
    JsonSubTypes.Type(value = ProductData::class, name = "ProductData")
)
interface BaseData*/

