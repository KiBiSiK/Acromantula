package net.cydhra.acromantula.features.view.document

@JvmInline
value class StyleClass(val cls: String)

val STYLE_KEYWORD: StyleClass = StyleClass("keyword")

val STYLE_IDENTIFIER: StyleClass = StyleClass("identifier")
val STYLE_TYPE: StyleClass = StyleClass("type")

val STYLE_LITERAL = StyleClass("literal")
val STYLE_LITERAL_STRING = STYLE_LITERAL + StyleClass("string")
val STYLE_LITERAL_NUMBER = STYLE_LITERAL + StyleClass("numeric")

val STYLE_ANNOTATION: StyleClass = StyleClass("annotation")

val STYLE_PUNCTUATION: StyleClass = StyleClass("punctuation")
val STYLE_PARENTHESIS: StyleClass = StyleClass("parenthesis")

operator fun StyleClass.plus(other: StyleClass): StyleClass {
    return StyleClass("${this.cls} ${other.cls}")
}