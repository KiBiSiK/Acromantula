package net.cydhra.acromantula.features.view

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.OutputStreamWriter

const val TABLE_CLASS = "table"
const val TABLE_HEADER_CLASS = "table_header"
const val TABLE_ROW_CLASS = "table_row"
const val TABLE_CELL_CLASS = "table_cell"
const val TABLE_FOOTER_CLASS = "table_footer"

/**
 * Generate HTML documents with the HTML layout abstracted.
 */
class DocumentGenerator(private val buffer: StringBuffer = StringBuffer(2048)) {

    fun append(body: TopLevelGenerator.() -> Unit) {
        this.buffer.appendHTML().div {
            TopLevelGenerator(this).apply(body)
        }
    }

    /**
     * Write the entire document to an [OutputStreamWriter]
     */
    fun writeHtml(writer: OutputStreamWriter) {
        writer.appendHTML().html {
            body {
                writer.write(buffer.toString())
                writer.flush()
            }
        }
    }

    inner class TopLevelGenerator(private val flowContent: FlowContent) {
        fun text(text: String) {
            flowContent.text(text)
        }

        fun collapsible(title: String, body: TopLevelGenerator.() -> Unit) {
            flowContent.details {
                summary {
                    +title
                }
                TopLevelGenerator(this).apply(body)
            }
        }

        /**
         * Generate a standard table
         */
        fun table(vararg columns: String, body: TableGenerator.() -> Unit) {
            flowContent.table(classes = TABLE_CLASS) {
                thead(classes = TABLE_HEADER_CLASS) {
                    tr(classes = TABLE_ROW_CLASS) {
                        for (c in columns) {
                            th(classes = TABLE_CELL_CLASS) {
                                +c
                            }
                        }
                    }
                }
                TableGenerator(columns, this).apply(body)
            }
        }

        fun br() {
            flowContent.br
        }
    }

    /**
     * Receiver class for table generation that offers generation of table entries
     */
    inner class TableGenerator(private val columns: Array<out String>, private val table: TABLE) {

        /**
         * Append a row with just text into the current table
         */
        fun row(cells: Map<String, String>) {
            with(table) {
                tr(classes = TABLE_ROW_CLASS) {
                    generateRow(this, cells)
                }
            }
        }

        /**
         * Append a row with complex components into the current table
         */
        fun row(body: RowGenerator.() -> Unit) {
            with(table) {
                tr(classes = TABLE_ROW_CLASS) {
                    RowGenerator(this).apply(body)
                }
            }
        }

        /**
         * Append a footer row into the current table
         */
        fun footer(cells: Map<String, String>) {
            with(table) {
                tfoot(classes = TABLE_FOOTER_CLASS) {
                    tr(classes = TABLE_ROW_CLASS) {
                        generateRow(this, cells)
                    }
                }
            }
        }

        private fun generateRow(tr: TR, cells: Map<String, String>) {
            for (c in columns) {
                tr.td(classes = TABLE_CELL_CLASS) {
                    +(cells[c] ?: "&nbsp;")
                }
            }
        }

        inner class RowGenerator(private val row: TR) {

            /**
             * Append a cell containing complex document constructs
             */
            fun cell(body: TopLevelGenerator.() -> Unit) {
                with(row) {
                    td {
                        TopLevelGenerator(this).apply(body)
                    }
                }
            }
        }
    }
}