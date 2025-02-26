import eu.gryta.ktor.utils.ApiInstance


class ApiInstanceSecondary : ApiInstance() {
    companion object {
        init {
            register(ApiInstanceSecondary::class) { ApiInstanceSecondary() }
        }
    }
}