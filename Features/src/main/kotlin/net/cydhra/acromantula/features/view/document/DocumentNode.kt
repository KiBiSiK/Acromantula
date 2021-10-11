package net.cydhra.acromantula.features.view.document

import org.w3c.dom.Element

interface DocumentNode {

    /**
     * Generate an XML element from this tree node
     */
    fun generateXML(): Element
}