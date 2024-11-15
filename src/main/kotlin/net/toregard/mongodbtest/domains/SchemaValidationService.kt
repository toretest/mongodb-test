package net.toregard.mongodbtest.domains

import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

/**
 * Service to validate JSON documents against provided JSON schemas.
 *
 * @param objectMapper Jackson ObjectMapper to convert documents to JSON nodes for validation.
 */
@Service
class SchemaValidationService(private val objectMapper: ObjectMapper) {

    fun validate(document: Map<String, Any>, schemaJson: String): List<String> {
        val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
        val schema = factory.getSchema(schemaJson)
        val jsonNode = objectMapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(document)
        val validationMessages = schema.validate(jsonNode)
        return validationMessages.map { it.message }
    }
}
