package net.cydhra.acromantula.features.view.document

import net.cydhra.acromantula.features.view.document.AcromantulaDocumentBlock.LineBuilder.FragmentBuilder
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * A block within an [AcromantulaDocument].
 *
 * @param document XML document entity, which can be used as a factory interface for XML elements
 */
class AcromantulaDocumentBlock internal constructor(
    private val document: Document
) : DocumentNode {

    private val header = this.document.createElement("header")
    private val body = this.document.createElement("body")
    private val footer = this.document.createElement("footer")

    /**
     * Append a block to this block's header.
     */
    fun appendToHeader(element: DocumentNode) {
        this.header.appendChild(element.generateXML())
    }

    /**
     * Append a block to this block's body.
     */
    fun appendToBody(element: DocumentNode) {
        this.body.appendChild(element.generateXML())
    }

    /**
     * Append a block to this block's footer.
     */
    internal fun appendToFooter(element: DocumentNode) {
        this.footer.appendChild(element.generateXML())
    }

    /**
     * DSL method to append a line to the body of a block
     */
    fun line(initializer: LineBuilder.() -> Unit) {
        val line = LineBuilder().apply(initializer)
        this.appendToBody(line)
    }

    /**
     * DSL method to append another block to this block's body.
     */
    fun block(initializer: AcromantulaDocumentBlock.() -> Unit) {
        val block = AcromantulaDocumentBlock(this.document).apply(initializer)
        this.appendToBody(block)
    }

    /**
     * DSL method to append content to the block's header. This method can be called more than once without
     * overwriting previous changes.
     */
    fun header(initializer: HeaderAppender.() -> Unit) {
        HeaderAppender().apply(initializer)
    }

    /**
     * DSL method to append content to the block's footer. This method can be called more than once without
     * overwriting previous changes.
     */
    fun footer(initializer: FooterAppender.() -> Unit) {
        FooterAppender().apply(initializer)
    }

    override fun generateXML(): Element {
        return this.document.createElement("block")
            .apply {
                appendChild(header)
                appendChild(body)
                appendChild(footer)
            }
    }

    inner class HeaderAppender {
        /**
         * DSL method to append a line to the header of a block
         */
        fun line(initializer: LineBuilder.() -> Unit) {
            val line = LineBuilder().apply(initializer)
            this@AcromantulaDocumentBlock.appendToHeader(line)
        }
    }

    inner class FooterAppender {
        /**
         * DSL method to append a line to the footer of a block
         */
        fun line(initializer: LineBuilder.() -> Unit) {
            val line = LineBuilder().apply(initializer)
            this@AcromantulaDocumentBlock.appendToFooter(line)
        }
    }

    /**
     * A singular line in an [AcromantulaDocument]. It consists of multiple [Fragments][FragmentBuilder] and makes up
     * part of a [AcromantulaDocumentBlock  ]
     */
    inner class LineBuilder : DocumentNode {
        private val lineElement = this@AcromantulaDocumentBlock.document.createElement("line")

        var designation: String
            get() = lineElement.getAttribute("designation")
            set(value) {
                lineElement.setAttribute("designation", value)
            }

        /**
         * Append a [Fragment][FragmentBuilder] to this line
         */
        fun f(initializer: FragmentBuilder.() -> Unit) {
            val f = FragmentBuilder().apply(initializer)
            lineElement.appendChild(f.generateXML())
        }

        /**
         * Append a [Fragment][FragmentBuilder] to this line with given [styleClass] and [designation] as shorthand
         * parameters
         */
        fun f(styleClass: StyleClass, designation: String = "", initializer: FragmentBuilder.() -> Unit) {
            val f = FragmentBuilder()
                .apply {
                    this.style = styleClass
                    this.designation = designation
                }
                .apply(initializer)
            lineElement.appendChild(f.generateXML())
        }

        override fun generateXML(): Element {
            return lineElement
        }

        /**
         * A fragment is a singular unit of uniformly formatted text with meta information for the back-end (see
         * [designation]) and optionally context actions
         */
        inner class FragmentBuilder : DocumentNode {
            private val fragmentElement = this@AcromantulaDocumentBlock.document.createElement("f")

            /**
             * Style classes. This is a space-separated list of css-like style classes.
             * TODO: define default classes
             */
            var style: StyleClass
                get() = StyleClass(fragmentElement.getAttribute("style"))
                set(value) {
                    fragmentElement.setAttribute("style", value.cls)
                }

            /**
             * A designation what this fragment encodes. This is not supposed to be interpreted by the front-end, but
             * required by the back-end to reconstruct the encoded complex file type from the document
             */
            var designation: String
                get() = fragmentElement.getAttribute("designation")
                set(value) {
                    fragmentElement.setAttribute("designation", value)
                }

            /**
             * Textual content of this fragment. Use [unaryPlus] to easily append text.
             */
            var content: String = ""

            /**
             * Append textual content to this node
             */
            operator fun String.unaryPlus() {
                content += this
            }

            override fun generateXML(): Element {
                fragmentElement.textContent = content
                return fragmentElement
            }
        }
    }
}