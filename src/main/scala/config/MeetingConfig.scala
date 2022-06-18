package config

final case class ServerConfig(host: String, port: Int)
final case class MeetingConfig(db: DatabaseConfig, server: ServerConfig)
