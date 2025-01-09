package com.example.skilltracker.di

import android.content.Context
import androidx.room.Room
import com.example.skilltracker.data.AppDatabase
import com.example.skilltracker.repository.SkillProgressRepository
import com.example.skilltracker.skillDetail.SkillDetailViewModel
import com.example.skilltracker.skillList.SkillListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val appModule = module {
    /* Creating an instance of the Room database. */
    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, "skills_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    /* Creating DAO. */
    single { get<AppDatabase>().skillDao() }
    single { get<AppDatabase>().progressDao() }

    /* Creating repository. */
    factory { (context: Context) -> SkillProgressRepository(context) }

    /* Embedding dependencies to ViewModels. */
    viewModel { (context: Context) -> SkillDetailViewModel(get { parametersOf(context) }) }
    viewModel { (context: Context) -> SkillListViewModel(get { parametersOf(context) }) }
}