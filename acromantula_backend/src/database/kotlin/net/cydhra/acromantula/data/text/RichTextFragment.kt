package net.cydhra.acromantula.data.text

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * TypeAlias for string that is meant to be a css-style class for rich text. This will later be replaced by an inline
 * class, as soon as they can be used with kotlinx.serialization
 */
typealias TextClass = String

/**
 * Append another [TextClass] to a given one.
 */
infix fun TextClass.and(other: TextClass): TextClass {
    return "$this $other"
}

const val TEXT_CLASS_NONE: TextClass = "none"
const val TEXT_CLASS_KEYWORD: TextClass = "keyword"
const val TEXT_CLASS_ANNOTATION: TextClass = "annotation"
const val TEXT_CLASS_COMMENT: TextClass = "comment"
const val TEXT_CLASS_DOCUMENTATION: TextClass = "documentation"

@Serializable
sealed class RichTextFragment {
    abstract val textFragment: String
    abstract val textClass: TextClass
    abstract val children: List<RichTextFragment>

    @Serializable
    @SerialName("raw")
    class RawTextFragment(
        override val textFragment: String,
        override val textClass: TextClass = TEXT_CLASS_NONE,
        override val children: List<RichTextFragment>
    ) : RichTextFragment()

    @Serializable
    @SerialName("reference")
    class ReferenceTextFragment(
        override val textFragment: String,
        override val textClass: TextClass,
        override val children: List<RichTextFragment>,
        val referenceTargetIdentifier: String
    ) : RichTextFragment()

    @Serializable
    @SerialName("jump_target")
    class JumpTargetTextFragment(
        override val textFragment: String,
        override val textClass: TextClass,
        override val children: List<RichTextFragment>,
        val jumpTargetIdentifier: String
    ) : RichTextFragment()

    @Serializable
    @SerialName("foldable")
    class FoldableFragment(
        override val textFragment: String = "",
        override val textClass: TextClass = TEXT_CLASS_NONE,
        override val children: List<RichTextFragment>
    ) : RichTextFragment()
}