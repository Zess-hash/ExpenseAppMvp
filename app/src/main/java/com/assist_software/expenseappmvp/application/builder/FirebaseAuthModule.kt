package com.assist_software.expenseappmvp.application.builder

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides

@Module
class FirebaseAuthModule {
    @AppScope
    @Provides
    fun getAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}