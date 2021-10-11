package net.cydhra.acromantula.features.view.document

import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

/**
 * An XML document format that is used to generate rich text representations of complex data (like disassembled or
 * decompiled code) and possibly revert the textual representation back into them.
 */
class AcromantulaDocument : DocumentNode {

    internal val document: Document
    private val mainNode: Element
    private val metaNode: Element
    private val contentNode: Element

    init {
        val docBuilder = with(DocumentBuilderFactory.newInstance()) {
            isCoalescing = false
            isIgnoringComments = false
            isIgnoringElementContentWhitespace = false
            newDocumentBuilder()
        }

        this.document = docBuilder.newDocument()
        this.mainNode = document.createElement("document")
        this.metaNode = document.createElement("meta")
        this.contentNode = document.createElement("content")
    }

    fun appendMetadata() {

    }

    fun createBlock(): AcromantulaDocumentBlock {
        return AcromantulaDocumentBlock(this.document)
    }

    /**
     * Append a block into the document
     */
    fun appendNode(element: DocumentNode) {
        this.contentNode.appendChild(element.generateXML())
    }

    /**
     * DSL method to append blocks
     */
    fun block(initializer: AcromantulaDocumentBlock.() -> Unit) {
        appendNode(createBlock().apply(initializer))
    }

    override fun generateXML(): Element {
        return this.mainNode
    }

    fun finishDocument(): Document {
        mainNode.appendChild(metaNode)
        mainNode.appendChild(contentNode)
        this.document.appendChild(mainNode)

        return this.document
    }
}