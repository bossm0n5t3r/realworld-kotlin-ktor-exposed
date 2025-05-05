package me.bossm0n5t3r

import io.ktor.server.application.Application
import io.ktor.server.application.install
import me.bossm0n5t3r.articles.ArticlesRepository
import me.bossm0n5t3r.articles.ArticlesService
import me.bossm0n5t3r.articles.CommentsRepository
import me.bossm0n5t3r.articles.FavoriteArticlesRepository
import me.bossm0n5t3r.configurations.DatabaseManager
import me.bossm0n5t3r.configurations.DatabaseManagerImpl
import me.bossm0n5t3r.profiles.FollowingsRepository
import me.bossm0n5t3r.profiles.ProfilesService
import me.bossm0n5t3r.tags.TagsRepository
import me.bossm0n5t3r.tags.TagsService
import me.bossm0n5t3r.uitilities.PasswordEncoder
import me.bossm0n5t3r.uitilities.PasswordEncoderImpl
import me.bossm0n5t3r.users.UsersRepository
import me.bossm0n5t3r.users.UsersService
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single<DatabaseManager> { DatabaseManagerImpl() }
            },
            module {
                single { UsersRepository(get()) }
                single { FollowingsRepository(get()) }
                single { TagsRepository(get()) }
                single { FavoriteArticlesRepository(get()) }
                single { ArticlesRepository(get()) }
                single { CommentsRepository(get()) }
                single<PasswordEncoder> { PasswordEncoderImpl() }
            },
            module {
                single { UsersService(get(), get()) }
                single { ProfilesService(get(), get()) }
                single { TagsService(get()) }
                single { ArticlesService(get(), get(), get(), get(), get(), get()) }
            },
        )
    }
}
