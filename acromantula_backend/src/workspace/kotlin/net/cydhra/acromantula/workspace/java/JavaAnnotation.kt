package net.cydhra.acromantula.workspace.java

import org.jetbrains.exposed.dao.IntIdTable

class JavaAnnotation

internal object JavaAnnotationTable : IntIdTable("JavaAnnotations") {

}

internal object ClassAnnotationsTable : IntIdTable("ClassAnnotations") {
    val javaClass = reference("class", JavaClassTable)
    val annotation = reference("annotation", JavaAnnotationTable)
}

internal object MethodAnnotationsTable : IntIdTable("MethodAnnotations") {
    val javaMethod = reference("method", JavaMethodTable)
    val annotation = reference("annotation", JavaAnnotationTable)
}

internal object FieldAnnotationsTable : IntIdTable("FieldAnnotations") {
    val javaField = reference("field", JavaFieldTable)
    val annotation = reference("annotation", JavaAnnotationTable)
}

internal object ParameterAnnotationsTable : IntIdTable("ParameterAnnotations") {
    val javaParameter = reference("parameter", JavaParameterTable)
    val annotation = reference("annotation", JavaAnnotationTable)
}