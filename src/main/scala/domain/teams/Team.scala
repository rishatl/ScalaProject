package domain.teams

import domain.meetings.Meeting
import domain.users.User

case class Team(
  name: String,
  meetings: Set[Meeting],
  users: Set[User],
  user: User,
  id: Option[Long] = None,
)
