import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListePosteComponent } from './liste-poste.component';

describe('ListePosteComponent', () => {
  let component: ListePosteComponent;
  let fixture: ComponentFixture<ListePosteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListePosteComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ListePosteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
