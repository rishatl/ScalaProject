package domain.meetings

case class Meeting (
  name: String,
  category: String,
  bio: String,
  tags: Set[String] = Set.empty,
  photoUrls: Set[String] = Set.empty,
  id: Option[Long] = None,
)
