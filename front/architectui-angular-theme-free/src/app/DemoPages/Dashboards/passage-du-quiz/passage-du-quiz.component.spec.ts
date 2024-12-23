import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PassageDuQuizComponent } from './passage-du-quiz.component';

describe('PassageDuQuizComponent', () => {
  let component: PassageDuQuizComponent;
  let fixture: ComponentFixture<PassageDuQuizComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PassageDuQuizComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PassageDuQuizComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
