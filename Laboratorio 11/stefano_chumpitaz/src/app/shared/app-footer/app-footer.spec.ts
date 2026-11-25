import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppFooter } from './app-footer';

describe('AppFooter', () => {
  let component: AppFooter;
  let fixture: ComponentFixture<AppFooter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppFooter],
    }).compileComponents();

    fixture = TestBed.createComponent(AppFooter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
