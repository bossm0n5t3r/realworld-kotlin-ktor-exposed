package me.bossm0n5t3r.tags

class TagsService(
    private val tagsRepository: TagsRepository,
) {
    suspend fun getAllTags(): TagsDto {
        val tags = tagsRepository.getAllTags()
        return TagsDto(tags)
    }
}
