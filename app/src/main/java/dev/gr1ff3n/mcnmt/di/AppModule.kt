package dev.gr1ff3n.mcnmt.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Bindings land here as repositories, location clients, and the Room database
    // come online. Empty for now so Hilt has a singleton-scoped module to attach to.
}
