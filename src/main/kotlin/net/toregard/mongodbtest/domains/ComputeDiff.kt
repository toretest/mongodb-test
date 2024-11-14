package net.toregard.mongodbtest.domains

import com.fasterxml.jackson.databind.ObjectMapper
import com.flipkart.zjsonpatch.JsonDiff
import com.fasterxml.jackson.databind.JsonNode
import org.bson.Document

class ComputeDiff {
    companion object    {
        fun computeDiff(existingDoc: Document, newDoc: Document): JsonNode {
            val objectMapper = ObjectMapper()
            val existingJsonNode = objectMapper.readTree(existingDoc.toJson())
            val newJsonNode = objectMapper.readTree(newDoc.toJson())
            return JsonDiff.asJson(existingJsonNode, newJsonNode)
        }
    }
}