package com.assist_software.expenseappmvp.screens.mainScreen

import com.assist_software.expenseappmvp.application.builder.AppComponent
import com.assist_software.expenseappmvp.application.builder.RestServiceNotification
import com.assist_software.expenseappmvp.data.database.repositories.UserRepository
import com.assist_software.expenseappmvp.data.utils.rx.RxSchedulers
import com.assist_software.expenseappmvp.utils.SharedPrefUtils
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class HomeScope

@HomeScope
@Component(
    modules = [HomeModule::class],
    dependencies = [AppComponent::class]
)
interface HomeComponent {
    fun inject(activity: HomeActivity)
}

@Module
class HomeModule(private val activity: HomeActivity) {

    @Provides
    @HomeScope
    fun view(): HomeView {
        return HomeView(activity)
    }

    @Provides
    @HomeScope
    fun presenter(
        view: HomeView,
        sharedPref: SharedPrefUtils,
        userRepository: UserRepository,
        rxSchedulers: RxSchedulers
    ): HomePresenter {
        val compositeDisposable = CompositeDisposable()
        return HomePresenter(
            view,
            sharedPref,
            rxSchedulers,
            userRepository,
            compositeDisposable
        )
    }
}