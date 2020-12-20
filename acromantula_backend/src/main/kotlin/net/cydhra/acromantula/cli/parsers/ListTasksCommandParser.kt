package net.cydhra.acromantula.cli.parsers

import com.xenomachina.argparser.ArgParser
import net.cydhra.acromantula.cli.WorkspaceCommandParser
import net.cydhra.acromantula.commands.WorkspaceCommandInterpreter
import net.cydhra.acromantula.commands.interpreters.ListTasksCommandInterpreter
import net.cydhra.acromantula.pool.Task
import org.apache.logging.log4j.LogManager

class ListTasksCommandParser(parser: ArgParser) : WorkspaceCommandParser<List<Task<*>>> {

    companion object {
        private val logger = LogManager.getLogger()
    }

    override fun build(): WorkspaceCommandInterpreter<List<Task<*>>> = ListTasksCommandInterpreter()

    override fun report(result: Result<List<Task<*>>>) {
        result.onSuccess { taskList ->
            taskList.forEach { task ->
                println(task)
            }
        }
    }
}