import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResetPasswordBoxedComponent } from './reset-password-boxed.component';

describe('ResetPasswordBoxedComponent', () => {
  let component: ResetPasswordBoxedComponent;
  let fixture: ComponentFixture<ResetPasswordBoxedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResetPasswordBoxedComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ResetPasswordBoxedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
