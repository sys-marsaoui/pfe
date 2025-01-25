import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { BaseLayoutComponent}  from './Layout/base-layout/base-layout.component';
import { PagesLayoutComponent } from './Layout/pages-layout/pages-layout.component';
import { ForgotPasswordBoxedComponent } from './DemoPages/UserPages/forgot-password-boxed/forgot-password-boxed.component';
import { LoginBoxedComponent } from './DemoPages/UserPages/login-boxed/login-boxed.component';
import { RegisterBoxedComponent } from './DemoPages/UserPages/register-boxed/register-boxed.component';
import { PassageDuQuizComponent } from './DemoPages/Dashboards/passage-du-quiz/passage-du-quiz.component';
import { PosteComponent } from './DemoPages/Dashboards/poste/poste.component';
import { CandidatureComponent } from './feature/candidature/candidature.component';
import { DetaillePosteComponent } from './feature/poste/detaille-poste/detaille-poste.component';
import { ListePosteComponent } from './feature/poste/liste-poste/liste-poste.component';
import { QuizListComponent } from './feature/quiz/quiz-list/quiz-list.component';
import { AuthGuard } from './core/guards/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: BaseLayoutComponent,
    children: [

      // Dashboads
      {path: '', canActivate: [AuthGuard] ,component: PosteComponent, data: {extraParameter: 'dashboardsMenu'}},
      // Post
      {path: 'detaille-poste/:id', canActivate: [AuthGuard] ,component: DetaillePosteComponent, data: {extraParameter: 'dashboardsMenu'}},
      {path: 'postes',canActivate: [AuthGuard] , component: ListePosteComponent, data: {extraParameter: 'dashboardsMenu'}},
      // Quiz
      {path: 'quiz/:id',canActivate: [AuthGuard] , component: PassageDuQuizComponent, data: {extraParameter: 'dashboardsMenu'}},
      {path: 'quizList',canActivate: [AuthGuard] , component: QuizListComponent, data: {extraParameter: 'dashboardsMenu'}},
      // Candidature
      {path: 'candidature', component: CandidatureComponent, data: {extraParameter: 'elementsMenu'}},
     ]

  },
  {
    path: '',
    component: PagesLayoutComponent,
    children: [

      // User Pages

      {path: 'login', component: LoginBoxedComponent, data: {extraParameter: ''}},
      {path: 'register', component: RegisterBoxedComponent, data: {extraParameter: ''}},
      {path: 'pages/forgot-password-boxed', component: ForgotPasswordBoxedComponent, data: {extraParameter: ''}},
    ]
  },
  {path: '**', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes,
    {
    scrollPositionRestoration: 'enabled',
    anchorScrolling: 'enabled',
    relativeLinkResolution: 'legacy'
})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
