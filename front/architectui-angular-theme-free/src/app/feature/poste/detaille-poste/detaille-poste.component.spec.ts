import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetaillePosteComponent } from './detaille-poste.component';

describe('DetaillePosteComponent', () => {
  let component: DetaillePosteComponent;
  let fixture: ComponentFixture<DetaillePosteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DetaillePosteComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DetaillePosteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
