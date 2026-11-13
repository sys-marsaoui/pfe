import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgReduxModule, NgRedux, DevToolsExtension } from '@angular-redux/store';
import { rootReducer, ArchitectUIState } from './ThemeOptions/store';
import { ConfigActions } from './ThemeOptions/store/config.actions';
import { AppRoutingModule } from './app-routing.module';
import { LoadingBarRouterModule } from '@ngx-loading-bar/router';
import { ToastrModule } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { AppComponent } from './app.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { PerfectScrollbarModule, PERFECT_SCROLLBAR_CONFIG, PerfectScrollbarConfigInterface } from 'ngx-perfect-scrollbar';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { BaseLayoutComponent } from './Layout/base-layout/base-layout.component';
import { PagesLayoutComponent } from './Layout/pages-layout/pages-layout.component';
import { PageTitleComponent } from './Layout/Components/page-title/page-title.component';
import { SearchBoxComponent } from './Layout/Components/header/elements/search-box/search-box.component';
import { UserBoxComponent } from './Layout/Components/header/elements/user-box/user-box.component';
import { SidebarComponent } from './Layout/Components/sidebar/sidebar.component';
import { LogoComponent } from './Layout/Components/sidebar/elements/logo/logo.component';
import { AnalyticsComponent } from './DemoPages/Dashboards/analytics/analytics.component';
import { ForgotPasswordBoxedComponent } from './DemoPages/UserPages/forgot-password-boxed/forgot-password-boxed.component';
import { LoginBoxedComponent } from './DemoPages/UserPages/login-boxed/login-boxed.component';
import { RegisterBoxedComponent } from './DemoPages/UserPages/register-boxed/register-boxed.component';
import { ResetPasswordBoxedComponent } from './DemoPages/UserPages/reset-password-boxed/reset-password-boxed.component';
import { StandardComponent } from './DemoPages/Elements/Buttons/standard/standard.component';
import { DropdownsComponent } from './DemoPages/Elements/dropdowns/dropdowns.component';
import { CardsComponent } from './DemoPages/Elements/cards/cards.component';
import { ListGroupsComponent } from './DemoPages/Elements/list-groups/list-groups.component';
import { TimelineComponent } from './DemoPages/Elements/timeline/timeline.component';
import { IconsComponent } from './DemoPages/Elements/icons/icons.component';
import { AccordionsComponent } from './DemoPages/Components/accordions/accordions.component';
import { TabsComponent } from './DemoPages/Components/tabs/tabs.component';
import { CarouselComponent } from './DemoPages/Components/carousel/carousel.component';
import { ModalsComponent } from './DemoPages/Components/modals/modals.component';
import { ProgressBarComponent } from './DemoPages/Components/progress-bar/progress-bar.component';
import { PaginationComponent } from './DemoPages/Components/pagination/pagination.component';
import { TooltipsPopoversComponent } from './DemoPages/Components/tooltips-popovers/tooltips-popovers.component';
import { RegularComponent } from './DemoPages/Tables/regular/regular.component';
import { TablesMainComponent } from './DemoPages/Tables/tables-main/tables-main.component';
import { ChartBoxes3Component } from './DemoPages/Widgets/chart-boxes3/chart-boxes3.component';
import { ControlsComponent } from './DemoPages/Forms/Elements/controls/controls.component';
import { ChartjsComponent } from './DemoPages/Charts/chartjs/chartjs.component';
import { LineChartComponent } from './DemoPages/Charts/chartjs/examples/line-chart/line-chart.component';
import { BarChartComponent } from './DemoPages/Charts/chartjs/examples/bar-chart/bar-chart.component';
import { ScatterChartComponent } from './DemoPages/Charts/chartjs/examples/scatter-chart/scatter-chart.component';
import { RadarChartComponent } from './DemoPages/Charts/chartjs/examples/radar-chart/radar-chart.component';
import { PolarAreaChartComponent } from './DemoPages/Charts/chartjs/examples/polar-area-chart/polar-area-chart.component';
import { BubbleChartComponent } from './DemoPages/Charts/chartjs/examples/bubble-chart/bubble-chart.component';
import { DynamicChartComponent } from './DemoPages/Charts/chartjs/examples/dynamic-chart/dynamic-chart.component';
import { DoughnutChartComponent } from './DemoPages/Charts/chartjs/examples/doughnut-chart/doughnut-chart.component';
import { PieChartComponent } from './DemoPages/Charts/chartjs/examples/pie-chart/pie-chart.component';
import { PosteComponent } from './DemoPages/Dashboards/poste/poste.component';
import { CandidatureFormComponent } from './DemoPages/Dashboards/candidature-form/candidature-form.component';
import { ResultTableCandidatComponent } from './feature/poste/result-table-candidat/result-table-candidat.component';
import { CondidaturCardComponent } from './DemoPages/Dashboards/condidatur-card/condidatur-card.component';
import { PassageDuQuizComponent } from './DemoPages/Dashboards/passage-du-quiz/passage-du-quiz.component';
import { QuizListComponent } from './feature/quiz/quiz-list/quiz-list.component';
import { CreateQuizComponent } from './feature/quiz/create-quiz/create-quiz.component';
import { CandidatureComponent } from './feature/candidature/candidature.component';
import { ListePosteComponent } from './feature/poste/liste-poste/liste-poste.component';
import { NewPosteComponent } from './feature/poste/new-poste/new-poste.component';
import { DetaillePosteComponent } from './feature/poste/detaille-poste/detaille-poste.component';
import { ProfileComponent } from './DemoPages/Dashboards/profile/profile/profile.component';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { HeaderComponent } from './Layout/Components/header/header.component';
import { StatusComponent } from './DemoPages/Dashboards/status/status.component';
import { SidebarUserComponent } from './DemoPages/Dashboards/sidebar-user/sidebar-user.component';
import { UsersComponent } from './DemoPages/Dashboards/users/users.component';
import { TakeQuizComponent } from './feature/quiz/take-quiz/take-quiz.component';
import { QuizDetailsComponent } from './feature/quiz/quiz-details/quiz-details.component';
import { StatisticsComponent } from './DemoPages/Dashboards/statistics/statistics.component';
import { ChartsModule } from 'ng2-charts';
import { UserDetailsComponent } from './DemoPages/Dashboards/user-details/user-details.component';
const DEFAULT_PERFECT_SCROLLBAR_CONFIG: PerfectScrollbarConfigInterface = {
  suppressScrollX: true
};

@NgModule({
  declarations: [
    AppComponent,
    BaseLayoutComponent,
    PagesLayoutComponent,
    PageTitleComponent,
    HeaderComponent,
    SearchBoxComponent,
    UserBoxComponent,
    SidebarComponent,
    LogoComponent,
    AnalyticsComponent,
    ForgotPasswordBoxedComponent,
    LoginBoxedComponent,
    RegisterBoxedComponent,
    ResetPasswordBoxedComponent,
    StandardComponent,
    IconsComponent,
    DropdownsComponent,
    CardsComponent,
    ListGroupsComponent,
    TimelineComponent,
    AccordionsComponent,
    TabsComponent,
    CarouselComponent,
    ModalsComponent,
    ProgressBarComponent,
    PaginationComponent,
    TooltipsPopoversComponent,
    RegularComponent,
    TablesMainComponent,
    ChartBoxes3Component,
    ControlsComponent,
    ChartjsComponent,
    LineChartComponent,
    BarChartComponent,
    DoughnutChartComponent,
    RadarChartComponent,
    PieChartComponent,
    PolarAreaChartComponent,
    DynamicChartComponent,
    BubbleChartComponent,
    ScatterChartComponent,
    PosteComponent,
    CandidatureFormComponent,
    ResultTableCandidatComponent,
    CondidaturCardComponent,
    PassageDuQuizComponent,
    QuizListComponent,
    CreateQuizComponent,
    CandidatureComponent,
    ListePosteComponent,
    NewPosteComponent,
    DetaillePosteComponent,
    ProfileComponent,
    StatusComponent,
    SidebarUserComponent,
    UsersComponent,
    UserDetailsComponent,
    TakeQuizComponent,
    QuizDetailsComponent,
    StatisticsComponent,
    
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    NgReduxModule,
    CommonModule,
    LoadingBarRouterModule,
    ToastrModule.forRoot({
      positionClass: 'toast-top-right',
      timeOut: 3000,
      closeButton: true,
      progressBar: true
    }),
    PerfectScrollbarModule,
    NgbModule,
    FontAwesomeModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ChartsModule
  ],
  providers: [
    {
      provide: PERFECT_SCROLLBAR_CONFIG,
      useValue: DEFAULT_PERFECT_SCROLLBAR_CONFIG
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    ConfigActions
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor(
    private ngRedux: NgRedux<ArchitectUIState>,
    private devTool: DevToolsExtension
  ) {
    this.ngRedux.configureStore(
      rootReducer,
      {} as ArchitectUIState,
      [],
      [devTool.isEnabled() ? devTool.enhancer() : f => f]
    );
  }
}