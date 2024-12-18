import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CondidaturCardComponent } from './condidatur-card.component';

describe('CondidaturCardComponent', () => {
  let component: CondidaturCardComponent;
  let fixture: ComponentFixture<CondidaturCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CondidaturCardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CondidaturCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
