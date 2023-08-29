package domain

import javax.inject.Inject

class AdminManagedRepositoriesProvider @Inject constructor(
    private val adminsRepository: AdminsRepository,
    private val receiversRepository: ReceiversRepository,
) {
    fun provide(type: AdminManagedType): AdminManagedChatsRepository {
        return when (type) {
            AdminManagedType.Admin -> {
                adminsRepository
            }

            AdminManagedType.Receiver -> {
                receiversRepository
            }
        }
    }
}