import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { BaseLayoutComponent } from './Layout/base-layout/base-layout.component';
import { PagesLayoutComponent } from './Layout/pages-layout/pages-layout.component';
import { ForgotPasswordBoxedComponent } from './DemoPages/UserPages/forgot-password-boxed/forgot-password-boxed.component';
import { LoginBoxedComponent } from './DemoPages/UserPages/login-boxed/login-boxed.component';
import { RegisterBoxedComponent } from './DemoPages/UserPages/register-boxed/register-boxed.component';
import { ResetPasswordBoxedComponent } from './DemoPages/UserPages/reset-password-boxed/reset-password-boxed.component';
import { PassageDuQuizComponent } from './DemoPages/Dashboards/passage-du-quiz/passage-du-quiz.component';
import { PosteComponent } from './DemoPages/Dashboards/poste/poste.component';
import { CandidatureComponent } from './feature/candidature/candidature.component';
import { DetaillePosteComponent } from './feature/poste/detaille-poste/detaille-poste.component';
import { ListePosteComponent } from './feature/poste/liste-poste/liste-poste.component';
import { QuizListComponent } from './feature/quiz/quiz-list/quiz-list.component';
import { UsersComponent } from './DemoPages/Dashboards/users/users.component';
import { AuthGuard } from './core/guards/auth.guard';
import { ResultTableCandidatComponent } from './feature/poste/result-table-candidat/result-table-candidat.component';
import { ProfileComponent } from './DemoPages/Dashboards/profile/profile/profile.component';
import { StatusComponent } from './DemoPages/Dashboards/status/status.component';
import { QuizDetailsComponent } from './feature/quiz/quiz-details/quiz-details.component';
import { UserDetailsComponent } from './DemoPages/Dashboards/user-details/user-details.component';
import { StatisticsComponent } from './DemoPages/Dashboards/statistics/statistics.component';

const routes: Routes = [
  {
    path: '',
    component: BaseLayoutComponent,
    children: [
      { path: '', canActivate: [AuthGuard], component: PosteComponent, data: { extraParameter: 'dashboardsMenu' } },
      { path: 'detaille-poste/:id', component: DetaillePosteComponent, data: { extraParameter: 'dashboardsMenu' } },
      { path: 'offres', component: ListePosteComponent, data: { extraParameter: 'dashboardsMenu' } },
      { path: 'postes', canActivate: [AuthGuard], component: ListePosteComponent, data: { extraParameter: 'dashboardsMenu' } },
      { path: 'quizzes/:id', component: QuizDetailsComponent },
      { path: 'quizList', canActivate: [AuthGuard], component: QuizListComponent, data: { extraParameter: 'dashboardsMenu' } },
      { path: 'candidats', component: ResultTableCandidatComponent, data: { extraParameter: 'elementsMenu' } },
      { path: 'users', canActivate: [AuthGuard], component: UsersComponent, data: { extraParameter: 'dashboardsMenu' } },
      { path: 'users/:id', canActivate: [AuthGuard], component: UserDetailsComponent, data: { extraParameter: 'dashboardsMenu' } },
      { path: 'statistics', canActivate: [AuthGuard], component: StatisticsComponent, data: { extraParameter: 'dashboardsMenu' } },
    ]
  },
  {
    path: '',
    component: PagesLayoutComponent,
    children: [
      { path: 'login', component: LoginBoxedComponent, data: { extraParameter: '' } },
      { path: 'register', component: RegisterBoxedComponent, data: { extraParameter: '' } },
      { path: 'pages/forgot-password-boxed', component: ForgotPasswordBoxedComponent, data: { extraParameter: '' } },
      { path: 'nouveau_mdp', component: ResetPasswordBoxedComponent, data: { extraParameter: '' } },
    ]
  },
  { path: 'profile', canActivate: [AuthGuard], component: ProfileComponent },
  { path: 'status', canActivate: [AuthGuard], component: StatusComponent },
  {path: 'quiz/:id',component: PassageDuQuizComponent,canActivate: [AuthGuard],},
  { path: 'candidat', component: ResultTableCandidatComponent},


  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    scrollPositionRestoration: 'enabled',
    anchorScrolling: 'enabled',
    relativeLinkResolution: 'legacy'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
