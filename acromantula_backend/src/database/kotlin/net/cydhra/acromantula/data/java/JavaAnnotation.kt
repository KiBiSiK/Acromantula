package net.cydhra.acromantula.data.java

import org.jetbrains.exposed.dao.IntIdTable

class JavaAnnotation

object JavaAnnotationTable : IntIdTable("JavaAnnotations") {

}

object ClassAnnotationsTable : IntIdTable("ClassAnnotations") {
    val javaClass = reference("class", JavaClassTable)
    val annotation = reference("annotation", JavaAnnotationTable)
}

object MethodAnnotationsTable : IntIdTable("MethodAnnotations") {
    val javaMethod = reference("method", JavaMethodTable)
    val annotation = reference("annotation", JavaAnnotationTable)
}

object FieldAnnotationsTable : IntIdTable("FieldAnnotations") {
    val javaField = reference("field", JavaFieldTable)
    val annotation = reference("annotation", JavaAnnotationTable)
}

object ParameterAnnotationsTable : IntIdTable("ParameterAnnotations") {
    val javaParameter = reference("parameter", JavaParameterTable)
    val annotation = reference("annotation", JavaAnnotationTable)
}