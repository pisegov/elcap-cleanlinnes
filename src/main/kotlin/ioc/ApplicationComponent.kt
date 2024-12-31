package ioc

import dagger.BindsInstance
import dagger.Component
import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import domain.AdminManagedRepositoriesProvider
import domain.states.BotState
import handlers.ActionHandlers
import handlers.admin.PermissionsChecker

@Component(modules = [
    ApplicationModule::class,
    AdminsModule::class,
    ReceiversModule::class,
    GroupsModule::class,
    UsersModule::class,
])
@ApplicationScope
interface ApplicationComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance behaviourContext: DefaultBehaviourContextWithFSM<BotState>,
        ): ApplicationComponent
    }

    val actionHandlers: Set<ActionHandlers>

    val permissionsChecker: PermissionsChecker

    val repositoryProvider: AdminManagedRepositoriesProvider
}