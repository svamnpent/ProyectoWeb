import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppHeader } from './app-header';

describe('AppHeader', () => {
  let component: AppHeader;
  let fixture: ComponentFixture<AppHeader>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppHeader],
    }).compileComponents();

    fixture = TestBed.createComponent(AppHeader);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
