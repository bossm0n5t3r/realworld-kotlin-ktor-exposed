package me.bossm0n5t3r.users

import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import me.bossm0n5t3r.userId

fun Route.usersApi(usersService: UsersService) {
    post("/users") {
        val createUserDto = call.receive<UserWrapper<CreateUserDto>>()
        val userDto = usersService.register(createUserDto.user)
        call.respond(UserWrapper(userDto))
    }

    post("/users/login") {
        val loginUserDto = call.receive<UserWrapper<LoginUserDto>>()
        val userDto = usersService.login(loginUserDto.user)
        call.respond(UserWrapper(userDto))
    }

    get("/users") {
        val allUsers = usersService.getAllUsers()
        call.respond(allUsers)
    }

    authenticate {
        get("/user") {
            val userId = call.userId()
            val userDto = usersService.getUserById(userId)
            call.respond(UserWrapper(userDto))
        }

        put("/user") {
            val userId = call.userId()
            val updateUserDto = call.receive<UserWrapper<UpdateUserDto>>()
            val userDto = usersService.updateUser(userId, updateUserDto.user)
            call.respond(UserWrapper(userDto))
        }
    }
}
