package net.cydhra.acromantula.rpc.service

/**
 * Thrown whenever an RPC request is missing a file or directory path
 */
class MissingTargetFileException : IllegalArgumentException("missing either target file id or target file path")