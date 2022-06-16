package config

final case class ServerConfig(host: String, port: Int)
final case class ConcertConfig(db: DatabaseConfig, server: ServerConfig)
