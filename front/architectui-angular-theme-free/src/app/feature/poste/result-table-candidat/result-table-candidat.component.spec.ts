import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultTableCandidatComponent } from './result-table-candidat.component';

describe('ResultTableCandidatComponent', () => {
  let component: ResultTableCandidatComponent;
  let fixture: ComponentFixture<ResultTableCandidatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResultTableCandidatComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultTableCandidatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
